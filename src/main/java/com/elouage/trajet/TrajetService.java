package com.elouage.trajet;

import com.elouage.ligne.Ligne;
import com.elouage.ligne.LigneRepository;
import com.elouage.reservation.Reservation;
import com.elouage.reservation.ReservationRepository;
import com.elouage.reservation.ReservationResponse;
import com.elouage.reservation.StatutReservation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class TrajetService {
    private final TrajetRepository trajetRepository;
    private final ReservationRepository reservationRepository;
    private final LigneRepository ligneRepository;

    public TrajetService(TrajetRepository trajetRepository, ReservationRepository reservationRepository,LigneRepository ligneRepository) {
        this.trajetRepository = trajetRepository;
        this.reservationRepository = reservationRepository;
        this.ligneRepository = ligneRepository;

    }

    // utilise par LigneService (US11)
    public List<Trajet> getTrajets(Long ligneId, LocalDate date) {
        return trajetRepository.findByLigneIdAndDate(ligneId, date);
    }

    public Trajet getTrajet(Long trajetId) {
        return trajetRepository.findById(trajetId)
                .orElseThrow(() -> new RuntimeException("Trajet introuvable : " + trajetId));
    }

    @Transactional(readOnly = true)
    public TrajetResponse getTrajetResponse(Long trajetId) {
        Trajet trajet = getTrajet(trajetId);
        return TrajetResponse.from(trajet, getPlacesLibres(trajetId).size());
    }

    // US12 : numeros des places libres (1 a 8) = 8 places moins celles bloquees par une reservation active
    //une reservation ANNULEE ou ABSENTE ne bloque plus la place (INACTIFS)
    public List<Integer> getPlacesLibres(Long trajetId) {
        List<Integer> occupees = reservationRepository.findNumerosOccupes(trajetId, StatutReservation.INACTIFS);
        List<Integer> libres = new ArrayList<>();
        for (int numero = 1; numero <= Trajet.NB_PLACES; numero++) {
            if (!occupees.contains(numero)) {
                libres.add(numero);
            }
        }
        return libres;
    }

    // US17 : les passagers presents, a cocher dans la checklist du louagiste
    @Transactional(readOnly = true)
    public List<ReservationResponse> getReservationsPresentes(Long trajetId, Long louagisteId) {
        Trajet trajet = getTrajet(trajetId);
        verifierLouagiste(trajet, louagisteId);
        return reservationRepository.findByTrajetIdAndStatutOrderByNumeroPlaceAsc(trajetId, StatutReservation.PRESENTE)
                .stream().map(ReservationResponse::from).toList();
    }
    //  toutes les reservations actives du trajet (pour que le louagiste voie ses passagers)
    @Transactional(readOnly = true)
    public List<ReservationResponse> getReservationsActives(Long trajetId, Long louagisteId) {
        Trajet trajet = getTrajet(trajetId);
        verifierLouagiste(trajet, louagisteId);
        return reservationRepository.findByTrajetIdAndStatutNotInOrderByNumeroPlaceAsc(trajetId, StatutReservation.INACTIFS)
                .stream().map(ReservationResponse::from).toList();
    }

    // US15 : le louagiste declare sa presence dans la station (pas de file ni de rang)
    @Transactional
    public TrajetResponse faireCheckIn(Long trajetId, Long louagisteId) {
        if (louagisteId == null) {
            throw new RuntimeException("louagisteId est obligatoire.");
        }
        Trajet trajet = trajetRepository.findByIdForUpdate(trajetId)
                .orElseThrow(() -> new RuntimeException("Trajet introuvable : " + trajetId));
        verifierLouagiste(trajet, louagisteId);
        if (trajet.isPresent()) {
            throw new RuntimeException("Votre presence est deja declaree.");
        }
        trajet.setPresent(true);
        return TrajetResponse.from(trajet, getPlacesLibres(trajetId).size());
    }

    // US27 : cree le trajet suivant sur la ligne inverse (apres Tunis->Sousse : Sousse->Tunis ;
    //apres le retour : Tunis->Sousse a nouveau). Son heure = heure estimee d'arrivee du trajet en cours
    //(heure du trajet + duree de la ligne). La liste d'attente de la ligne est servie tout de suite.
    @Transactional
    public TrajetResponse declarerRetour(Long trajetId, Long louagisteId) {
        if (louagisteId == null) {
            throw new RuntimeException("louagisteId est obligatoire.");
        }
        Trajet trajet = trajetRepository.findByIdForUpdate(trajetId)
                .orElseThrow(() -> new RuntimeException("Trajet introuvable : " + trajetId));
        verifierLouagiste(trajet, louagisteId);
        if (trajetRepository.existsByTrajetPrecedentId(trajetId)) {
            throw new RuntimeException("Le trajet suivant existe deja.");
        }
        Ligne inverse = trouverLigneInverse(trajet.getLigne());

        LocalTime heure;
        if (trajet.getHeureArrivee() != null && trajet.getLigne().getDureeTrajet() != null) {
            heure = trajet.getHeureArrivee().plusMinutes(trajet.getLigne().getDureeTrajet());
            if (heure.isBefore(trajet.getHeureArrivee())) {
                throw new RuntimeException("Le trajet suivant depasserait minuit.");
            }
        } else {
            heure = LocalTime.now().withSecond(0).withNano(0);     // pas de duree connue : on prend l'heure actuelle
        }

        Trajet suivant = new Trajet(trajet.getPlanning(), trajet.getLouagiste(), inverse, trajet.getDate());
        suivant.setRetour(!trajet.isRetour());
        suivant.setTrajetPrecedent(trajet);
        suivant.setHeureArrivee(heure);
        trajetRepository.save(suivant);

        remplirDepuisAttente(suivant);
        return TrajetResponse.from(suivant, getPlacesLibres(suivant.getId()).size());
    }

    private Ligne trouverLigneInverse(Ligne ligne) {
        if (ligne.getStationDepart() == null || ligne.getStationArrivee() == null) {
            throw new RuntimeException("La ligne " + ligne.getId() + " n'a pas de stations definies.");
        }
        return ligneRepository.findFirstByStationDepartIdAndStationArriveeIdOrderByIdAsc(
                        ligne.getStationArrivee().getId(), ligne.getStationDepart().getId())
                .orElseThrow(() -> new RuntimeException(
                        "Aucune ligne inverse pour la ligne " + ligne.getId() + " : ajoutez-la d'abord (US09)."));
    }


    // US18: les passagers qui n'ont pas encore signale leur presence (CONFIRMEE).
    @Transactional(readOnly = true)
    public List<ReservationResponse> getNonPresents(Long trajetId, Long louagisteId) {
        Trajet trajet = getTrajet(trajetId);
        verifierLouagiste(trajet, louagisteId);
        return reservationRepository.findByTrajetIdAndStatutOrderByNumeroPlaceAsc(trajetId, StatutReservation.CONFIRMEE)
                .stream().map(ReservationResponse::from).toList();
    }

    // US18:libererPlacesAbsentes() du diagramme : le backend enregistre seulement ABSENTE, puis les places
    // liberees sont donnees automatiquement a la liste d'attente (US19 / US20)
    @Transactional
    public LiberationResponse marquerAbsents(Long trajetId, MarquerAbsentsRequest request) {
        if (request == null || request.louagisteId() == null
                || request.reservationIds() == null || request.reservationIds().isEmpty()) {
            throw new RuntimeException("louagisteId et au moins une reservation sont obligatoires.");
        }
        Trajet trajet = trajetRepository.findByIdForUpdate(trajetId)
                .orElseThrow(() -> new RuntimeException("Trajet introuvable : " + trajetId));
        verifierLouagiste(trajet, request.louagisteId());

        Set<Long> ids = new HashSet<>(request.reservationIds());
        // verrou : un passager ne peut pas signaler sa presence pendant qu'on le marque absent
        List<Reservation> aMarquer = reservationRepository
                .findByTrajetAndStatutForUpdate(trajetId, StatutReservation.CONFIRMEE).stream()
                .filter(r -> ids.contains(r.getId())).toList();
        if (aMarquer.size() != ids.size()) {
            throw new RuntimeException("Seules les reservations CONFIRMEE de ce trajet peuvent etre marquees absentes "
                    + "(un passager a peut-etre deja signale sa presence).");
        }
        List<Integer> places = new ArrayList<>();
        for (Reservation r : aMarquer) {
            r.libererPlace();                       // statut ABSENTE
            places.add(r.getNumeroPlace());
        }
        List<ReservationResponse> remplacants = new ArrayList<>();
        for (int place : places) {
            promouvoirRemplacant(trajet, place).ifPresent(r -> remplacants.add(ReservationResponse.from(r)));
        }
        return new LiberationResponse(trajetId, places, remplacants);
    }

    //US19 / US20 : liste d'attente

    // passagers en attente sur la ligne et le jour du trajet, dans l'ordre d'inscription,
    // dont une plage horaire contient l'heure du trajet
    private List<Reservation> candidats(Trajet trajet) {
        return reservationRepository.findByLigneIdAndDateSouhaiteeAndStatutAndTrajetIsNullOrderByIdAsc(
                        trajet.getLigne().getId(), trajet.getDate(), StatutReservation.EN_ATTENTE).stream()
                .filter(r -> r.estCompatibleAvec(trajet.getHeureArrivee()))
                .filter(r -> !reservationRepository.existsByTrajetIdAndPassagerIdAndStatutNotIn(
                        trajet.getId(), r.getPassager().getId(), StatutReservation.INACTIFS))
                .toList();
    }

    // US20:proposerRemplacant() du diagramme, automatique : le premier passager en attente compatible
    // recoit la place liberee (CONFIRMEE)
    @Transactional
    public Optional<Reservation> promouvoirRemplacant(Trajet trajet, int numeroPlace) {
        trajetRepository.findByIdForUpdate(trajet.getId());      // meme verrou que reserver()
        if (reservationRepository.existsByTrajetIdAndNumeroPlaceAndStatutNotIn(
                trajet.getId(), numeroPlace, StatutReservation.INACTIFS)) {
            return Optional.empty();                             // place deja reprise
        }
        List<Reservation> candidats = candidats(trajet);
        if (candidats.isEmpty()) {
            return Optional.empty();
        }
        Reservation r = candidats.get(0);
        r.setTrajet(trajet);
        r.setNumeroPlace(numeroPlace);
        r.setStatut(StatutReservation.CONFIRMEE);
        return Optional.of(r);
    }

    // nouveau trajet : on remplit ses places libres depuis la liste d'attente, dans l'ordre d'inscription
    @Transactional
    public int remplirDepuisAttente(Trajet trajet) {
        int promus = 0;
        for (int place = 1; place <= Trajet.NB_PLACES; place++) {
            if (reservationRepository.existsByTrajetIdAndNumeroPlaceAndStatutNotIn(
                    trajet.getId(), place, StatutReservation.INACTIFS)) {
                continue;
            }
            if (promouvoirRemplacant(trajet, place).isEmpty()) {
                break;                                           // plus personne de compatible
            }
            promus++;
        }
        return promus;
    }

    // US20 : le louagiste consulte les passagers en attente compatibles avec son trajet
    @Transactional(readOnly = true)
    public List<ReservationResponse> getAttente(Long trajetId, Long louagisteId) {
        Trajet trajet = getTrajet(trajetId);
        verifierLouagiste(trajet, louagisteId);
        return candidats(trajet).stream().map(ReservationResponse::from).toList();
    }

    private void verifierLouagiste(Trajet trajet, Long louagisteId) {
        if (!trajet.getLouagiste().getId().equals(louagisteId)) {
            throw new RuntimeException("Ce trajet n'appartient pas a ce louagiste.");
        }
    }
}
