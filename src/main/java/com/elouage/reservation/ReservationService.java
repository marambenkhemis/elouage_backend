package com.elouage.reservation;

import com.elouage.ligne.Ligne;
import com.elouage.ligne.LigneRepository;
import com.elouage.trajet.Trajet;
import com.elouage.trajet.TrajetRepository;
import com.elouage.trajet.TrajetService;
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
    private final LigneRepository ligneRepository;
    private final UtilisateurService utilisateurService;
    private final TrajetService trajetService;



    public ReservationService(ReservationRepository reservationRepository, TrajetRepository trajetRepository,
                              LigneRepository ligneRepository, UtilisateurService utilisateurService,
                              TrajetService trajetService) {
        this.reservationRepository = reservationRepository;
        this.trajetRepository = trajetRepository;
        this.ligneRepository = ligneRepository;
        this.utilisateurService = utilisateurService;
        this.trajetService = trajetService;
    }

    private Reservation getReservation(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation introuvable : " + id));
    }

    // US13 : reserver une place precise.
    // Le trajet est verrouille en base : deux passagers ne peuvent pas prendre la meme place en meme temps.
    @Transactional
    public ReservationResponse reserver(ReservationRequest request) {
        if (request.trajetId() == null || request.passagerId() == null) {
            throw new RuntimeException("trajetId et passagerId sont obligatoires.");
        }
        if (request.numeroPlace() < 1 || request.numeroPlace() > Trajet.NB_PLACES) {
            throw new RuntimeException(
                    "Le numero de place doit etre compris entre 1 et " + Trajet.NB_PLACES + ".");
        }
        Passager passager = utilisateurService.getPassager(request.passagerId());
        Trajet trajet = trajetRepository.findByIdForUpdate(request.trajetId())
                .orElseThrow(() -> new RuntimeException(
                        "Trajet introuvable : " + request.trajetId()));

        if (trajet.getDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Ce trajet est deja passe.");
        }
        if (existeReservation(request.numeroPlace(), trajet.getId())) {
            throw new RuntimeException(
                    "La place " + request.numeroPlace() + " vient d'etre prise, choisissez une autre place.");
        }
        // une reservation ANNULEE ou ABSENTE ne compte plus
        if (reservationRepository.existsByTrajetIdAndPassagerIdAndStatutNotIn(
                trajet.getId(), passager.getId(), StatutReservation.INACTIFS)) {
            throw new RuntimeException("Vous avez deja une reservation sur ce trajet.");
        }
        Reservation reservation = reservationRepository.save(new Reservation(passager, trajet, request.numeroPlace()));
        return ReservationResponse.from(reservation);
    }

    // existeReservation(numeroPlace, trajet)
    @Transactional(readOnly = true)
    public boolean existeReservation(int numeroPlace, Long trajetId) {
        return reservationRepository.existsByTrajetIdAndNumeroPlaceAndStatutNotIn(
                trajetId, numeroPlace, StatutReservation.INACTIFS);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getReservations(Long passagerId) {
        return reservationRepository.findByPassagerIdOrderByDateReservationDescIdDesc(passagerId).stream()
                .map(ReservationResponse::from).toList();
    }
    // US19 : rejoindre la liste d'attente d'une ligne pour un jour, en indiquant ses plages horaires.
    // Le passager sera propose comme remplacant quand une place compatible se libere (voir TrajetService).
    @Transactional
    public ReservationResponse rejoindreAttente(AttenteRequest request) {
        if (request.passagerId() == null || request.ligneId() == null || request.date() == null
                || request.plages() == null || request.plages().isEmpty()) {
            throw new RuntimeException("passagerId, ligneId, date et au moins une plage horaire sont obligatoires.");
        }
        if (request.date().isBefore(LocalDate.now())) {
            throw new RuntimeException("Cette date est deja passee.");
        }
        Passager passager = utilisateurService.getPassager(request.passagerId());
        Ligne ligne = ligneRepository.findById(request.ligneId())
                .orElseThrow(() -> new RuntimeException("Ligne introuvable : " + request.ligneId()));
        if (reservationRepository.existsByLigneIdAndDateSouhaiteeAndPassagerIdAndStatut(
                ligne.getId(), request.date(), passager.getId(), StatutReservation.EN_ATTENTE)) {
            throw new RuntimeException("Vous etes deja en liste d'attente sur cette ligne pour ce jour.");
        }
        Reservation attente = reservationRepository.save(
                new Reservation(passager, ligne, request.date(), request.plages()));
        return ReservationResponse.from(attente);
    }

    // US14 : annuler, la place redevient libre(marche aussi pour quitter la liste d'attente)
    @Transactional
    public ReservationResponse annuler(Long reservationId, Long passagerId) {
        Reservation reservation = getReservation(reservationId);
        verifierPassager(reservation, passagerId);
        if (reservation.getStatut() == StatutReservation.ANNULEE) {
            throw new RuntimeException("Cette reservation est deja annulee.");
        }
        if (reservation.getStatut() == StatutReservation.ABSENT) {
            throw new RuntimeException("Ce passager a ete marque absent..");
        }
        if (reservation.getStatut() == StatutReservation.EMBARQUEE) {
            throw new RuntimeException("Impossible d'annuler : le passager est deja embarque.");
        }
        reservation.setStatut(StatutReservation.ANNULEE);
        // US20: la place liberee est donnee automatiquement au premier passager en attente compatible
        if (reservation.getTrajet() != null) {      // une reservation en attente n'a pas de place a liberer
            trajetService.promouvoirRemplacant(reservation.getTrajet(), reservation.getNumeroPlace());
        }
        return ReservationResponse.from(reservation);
    }

    // US16 : le passager signale sa presence dans la station
    //verrou sur la reservation (pas de conflit avec "marquer absent" du louagiste, US18)

    @Transactional
    public ReservationResponse signalerPresence(Long reservationId, Long passagerId) {
        Reservation reservation = reservationRepository.findByIdForUpdate(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation introuvable : " + reservationId));
        verifierPassager(reservation, passagerId);
        if (reservation.getStatut() != StatutReservation.CONFIRMEE) {
            throw new RuntimeException(
                    "Seule une reservation confirmee peut etre marquee presente (statut : " + reservation.getStatut() + ").");
        }
        reservation.setStatut(StatutReservation.PRESENTE);
        return ReservationResponse.from(reservation);
    }

    // US17 : le louagiste coche un passager present
    @Transactional
    public ReservationResponse confirmerEmbarquement(Long reservationId, Long louagisteId) {
        Reservation reservation = getReservation(reservationId);
        //  une reservation en attente n'a pas de trajet
        if (reservation.getTrajet() == null) {
            throw new RuntimeException("Ce passager est encore en liste d'attente, il n'a pas de trajet.");
        }
        verifierLouagiste(reservation.getTrajet(), louagisteId);
        embarquer(reservation);
        return ReservationResponse.from(reservation);
    }

    // US17 : valider toute la checklist (tout ou rien : une erreur annule la validation entiere)
    @Transactional
    public List<ReservationResponse> validerChecklist(ChecklistRequest request) {
        if (request.trajetId() == null || request.louagisteId() == null
                || request.reservationIds() == null || request.reservationIds().isEmpty()) {
            throw new RuntimeException(
                    "trajetId, louagisteId et au moins une reservation sont obligatoires.");
        }
        Trajet trajet = trajetRepository.findById(request.trajetId())
                .orElseThrow(() -> new RuntimeException(
                        "Trajet introuvable : " + request.trajetId()));
        verifierLouagiste(trajet, request.louagisteId());

        List<ReservationResponse> resultat = new ArrayList<>();
        for (Long id : request.reservationIds()) {
            Reservation reservation = getReservation(id);
            //reservation.getTrajet() == null (en attente) refuse aussi
            if (reservation.getTrajet() == null || !reservation.getTrajet().getId().equals(trajet.getId())) {
                throw new RuntimeException(
                        "La reservation " + id + " n'appartient pas au trajet " + trajet.getId() + ".");
            }
            embarquer(reservation);
            resultat.add(ReservationResponse.from(reservation));
        }
        return resultat;
    }

    private void embarquer(Reservation reservation) {
        if (reservation.getStatut() != StatutReservation.PRESENTE) {
            throw new RuntimeException(
                    "Le passager n'a pas signale sa presence (statut : " + reservation.getStatut() + ").");
        }
        reservation.setStatut(StatutReservation.EMBARQUEE);
    }

    private void verifierPassager(Reservation reservation, Long passagerId) {
        if (!reservation.getPassager().getId().equals(passagerId)) {
            throw new RuntimeException("Cette reservation n'appartient pas a ce passager.");
        }
    }

    private void verifierLouagiste(Trajet trajet, Long louagisteId) {
        if (!trajet.getLouagiste().getId().equals(louagisteId)) {
            throw new RuntimeException("Ce trajet n'appartient pas a ce louagiste.");
        }
    }
}
