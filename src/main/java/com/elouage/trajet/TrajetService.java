package com.elouage.trajet;

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
import java.util.ArrayList;
import java.util.List;

@Service
public class TrajetService {
    private final TrajetRepository trajetRepository;
    private final ReservationRepository reservationRepository;
    private final boolean verifierDates;

    public TrajetService(TrajetRepository trajetRepository, ReservationRepository reservationRepository) {
        this.trajetRepository = trajetRepository;
        this.reservationRepository = reservationRepository;
        this.verifierDates = true;
    }

    // utilise par LigneService (US11)
    public List<Trajet> getTrajets(Long ligneId, LocalDate date) {
        return trajetRepository.findByLigneIdAndDate(ligneId, date);
    }

    public Trajet getTrajet(Long trajetId) {
        return trajetRepository.findById(trajetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trajet introuvable : " + trajetId));
    }

    @Transactional(readOnly = true)
    public TrajetResponse getTrajetResponse(Long trajetId) {
        Trajet trajet = getTrajet(trajetId);
        return TrajetResponse.from(trajet, getPlacesLibres(trajetId).size());
    }

    // US12 : numeros des places libres (1 a 8) = 8 places moins les reservations non annulees
    // utilise par LigneService
    public List<Integer> getPlacesLibres(Long trajetId) {
        List<Integer> occupees = reservationRepository.findNumerosOccupes(trajetId, StatutReservation.ANNULEE);
        List<Integer> libres = new ArrayList<>();
        for (int numero = 1; numero <= Trajet.NB_PLACES; numero++) {
            if (!occupees.contains(numero)) {
                libres.add(numero);
            }
        }
        return libres;
    }

    // US18 : les passagers presents, a cocher dans la checklist du louagiste
    @Transactional(readOnly = true)
    public List<ReservationResponse> getReservationsPresentes(Long trajetId, Long louagisteId) {
        Trajet trajet = getTrajet(trajetId);
        verifierLouagiste(trajet, louagisteId);
        return reservationRepository.findByTrajetIdAndStatutOrderByNumeroPlaceAsc(trajetId, StatutReservation.PRESENTE)
                .stream().map(ReservationResponse::from).toList();
    }

    // US15 + US16 : check-in du louagiste, le rang suit l'ordre d'arrivee.
    // On verrouille tous les trajets de la ligne pour ce jour : deux check-in simultanes n'ont jamais le meme rang.
    @Transactional
    public CheckInResponse faireCheckIn(Long trajetId, Long louagisteId) {
        if (louagisteId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "louagisteId est obligatoire.");
        }
        Long ligneId = trajetRepository.findLigneIdById(trajetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trajet introuvable : " + trajetId));
        LocalDate jour = trajetRepository.findDateById(trajetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trajet introuvable : " + trajetId));

        List<Trajet> duJour = trajetRepository.findAllByLigneAndDateForUpdate(ligneId, jour);
        Trajet trajet = duJour.stream().filter(t -> t.getId().equals(trajetId)).findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trajet introuvable : " + trajetId));

        verifierLouagiste(trajet, louagisteId);
        if (verifierDates && !jour.equals(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Le check-in n'est possible que le jour du trajet (" + jour + ").");
        }
        if (trajet.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Le check-in a deja ete effectue (rang " + trajet.getRang() + ").");
        }

        // calcul du rang : dernier rang de la ligne ce jour-la + 1
        int dernierRang = 0;
        for (Trajet t : duJour) {
            if (t.isPresent() && t.getRang() > dernierRang) {
                dernierRang = t.getRang();
            }
        }
        int rang = dernierRang + 1;

        trajet.setPresent(true);
        trajet.setRang(rang);
        trajet.setHeureCheckIn(LocalDateTime.now());
        return new CheckInResponse(trajet.getId(), rang, rang - 1, trajet.getHeureCheckIn());
    }

    // US16 : la file de depart d'une ligne, dans l'ordre d'arrivee
    @Transactional(readOnly = true)
    public List<FileDepartResponse> getFile(Long ligneId, LocalDate date) {
        return trajetRepository.findByLigneIdAndDateAndPresentTrueOrderByRangAsc(ligneId, date).stream()
                .map(FileDepartResponse::from).toList();
    }

    private void verifierLouagiste(Trajet trajet, Long louagisteId) {
        if (!trajet.getLouagiste().getId().equals(louagisteId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ce trajet n'appartient pas a ce louagiste.");
        }
    }
}
