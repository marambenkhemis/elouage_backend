package com.elouage.reservation;

import com.elouage.trajet.Trajet;
import com.elouage.trajet.TrajetRepository;
import com.elouage.utilisateur.Passager;
import com.elouage.utilisateur.UtilisateurService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final TrajetRepository trajetRepository;
    private final UtilisateurService utilisateurService;
    private final boolean verifierDates;

    public ReservationService(ReservationRepository reservationRepository, TrajetRepository trajetRepository,
                              UtilisateurService utilisateurService) {
        this.reservationRepository = reservationRepository;
        this.trajetRepository = trajetRepository;
        this.utilisateurService = utilisateurService;
        this.verifierDates = true;
    }

    private Reservation getReservation(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation introuvable : " + id));
    }

    // US13 : reserver une place precise.
    // Le trajet est verrouille en base : deux passagers ne peuvent pas prendre la meme place en meme temps.
    @Transactional
    public ReservationResponse reserver(ReservationRequest request) {
        if (request.trajetId() == null || request.passagerId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "trajetId et passagerId sont obligatoires.");
        }
        if (request.numeroPlace() < 1 || request.numeroPlace() > Trajet.NB_PLACES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Le numero de place doit etre compris entre 1 et " + Trajet.NB_PLACES + ".");
        }
        Passager passager = utilisateurService.getPassager(request.passagerId());
        Trajet trajet = trajetRepository.findByIdForUpdate(request.trajetId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Trajet introuvable : " + request.trajetId()));

        if (trajet.getDate().isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ce trajet est deja passe.");
        }
        if (existeReservation(request.numeroPlace(), trajet.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La place " + request.numeroPlace() + " vient d'etre prise, choisissez une autre place.");
        }
        if (reservationRepository.existsByTrajetIdAndPassagerIdAndStatutNot(
                trajet.getId(), passager.getId(), StatutReservation.ANNULEE)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Vous avez deja une reservation sur ce trajet.");
        }
        Reservation reservation = reservationRepository.save(new Reservation(passager, trajet, request.numeroPlace()));
        return ReservationResponse.from(reservation);
    }

    // existeReservation(numeroPlace, trajet) du diagramme
    @Transactional(readOnly = true)
    public boolean existeReservation(int numeroPlace, Long trajetId) {
        return reservationRepository.existsByTrajetIdAndNumeroPlaceAndStatutNot(
                trajetId, numeroPlace, StatutReservation.ANNULEE);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getReservations(Long passagerId) {
        return reservationRepository.findByPassagerIdOrderByDateReservationDescIdDesc(passagerId).stream()
                .map(ReservationResponse::from).toList();
    }

    // US14 : annuler, la place redevient libre
    @Transactional
    public ReservationResponse annuler(Long reservationId, Long passagerId) {
        Reservation reservation = getReservation(reservationId);
        verifierPassager(reservation, passagerId);
        if (reservation.getStatut() == StatutReservation.ANNULEE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cette reservation est deja annulee.");
        }
        if (reservation.getStatut() == StatutReservation.EMBARQUEE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Impossible d'annuler : le passager est deja embarque.");
        }
        reservation.setStatut(StatutReservation.ANNULEE);
        return ReservationResponse.from(reservation);
    }

    // US17 : le passager signale sa presence dans la station
    @Transactional
    public ReservationResponse signalerPresence(Long reservationId, Long passagerId) {
        Reservation reservation = getReservation(reservationId);
        verifierPassager(reservation, passagerId);
        LocalDate jour = reservation.getTrajet().getDate();
        if (verifierDates && !jour.equals(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La presence ne peut etre signalee que le jour du trajet (" + jour + ").");
        }
        if (reservation.getStatut() != StatutReservation.CONFIRMEE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Seule une reservation confirmee peut etre marquee presente (statut : " + reservation.getStatut() + ").");
        }
        reservation.setStatut(StatutReservation.PRESENTE);
        return ReservationResponse.from(reservation);
    }

    // US18 : le louagiste coche un passager present
    @Transactional
    public ReservationResponse confirmerEmbarquement(Long reservationId, Long louagisteId) {
        Reservation reservation = getReservation(reservationId);
        verifierLouagiste(reservation.getTrajet(), louagisteId);
        embarquer(reservation);
        return ReservationResponse.from(reservation);
    }

    // US18 : valider toute la checklist (tout ou rien : une erreur annule la validation entiere)
    @Transactional
    public List<ReservationResponse> validerChecklist(ChecklistRequest request) {
        if (request.trajetId() == null || request.louagisteId() == null
                || request.reservationIds() == null || request.reservationIds().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "trajetId, louagisteId et au moins une reservation sont obligatoires.");
        }
        Trajet trajet = trajetRepository.findById(request.trajetId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Trajet introuvable : " + request.trajetId()));
        verifierLouagiste(trajet, request.louagisteId());

        List<ReservationResponse> resultat = new ArrayList<>();
        for (Long id : request.reservationIds()) {
            Reservation reservation = getReservation(id);
            if (!reservation.getTrajet().getId().equals(trajet.getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "La reservation " + id + " n'appartient pas au trajet " + trajet.getId() + ".");
            }
            embarquer(reservation);
            resultat.add(ReservationResponse.from(reservation));
        }
        return resultat;
    }

    private void embarquer(Reservation reservation) {
        if (reservation.getStatut() != StatutReservation.PRESENTE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Le passager n'a pas signale sa presence (statut : " + reservation.getStatut() + ").");
        }
        reservation.setStatut(StatutReservation.EMBARQUEE);
    }

    private void verifierPassager(Reservation reservation, Long passagerId) {
        if (!reservation.getPassager().getId().equals(passagerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cette reservation n'appartient pas a ce passager.");
        }
    }

    private void verifierLouagiste(Trajet trajet, Long louagisteId) {
        if (!trajet.getLouagiste().getId().equals(louagisteId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ce trajet n'appartient pas a ce louagiste.");
        }
    }
}
