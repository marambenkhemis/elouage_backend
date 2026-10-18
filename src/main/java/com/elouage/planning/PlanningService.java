package com.elouage.planning;

import com.elouage.ligne.Ligne;
import com.elouage.ligne.LigneRepository;
import com.elouage.reservation.ReservationRepository;
import com.elouage.reservation.StatutReservation;
import com.elouage.trajet.Trajet;
import com.elouage.trajet.TrajetRepository;
import com.elouage.trajet.TrajetResponse;
import com.elouage.utilisateur.Louagiste;
import com.elouage.utilisateur.UtilisateurService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class PlanningService {
    private final PlanningRepository planningRepository;
    private final TrajetRepository trajetRepository;
    private final ReservationRepository reservationRepository;
    private final LigneRepository ligneRepository;
    private final UtilisateurService utilisateurService;
    private static final int DELAI_CLOTURE_JOURS = 0;

    public PlanningService(PlanningRepository planningRepository, TrajetRepository trajetRepository,
                           ReservationRepository reservationRepository, LigneRepository ligneRepository,
                           UtilisateurService utilisateurService) {
        this.planningRepository = planningRepository;
        this.trajetRepository = trajetRepository;
        this.reservationRepository = reservationRepository;
        this.ligneRepository = ligneRepository;
        this.utilisateurService = utilisateurService;
    }

    // US10 : remplir le planning de la semaine. La presence est confirmee tout de suite, sans validation de l'admin.
    @Transactional
    public PlanningResponse remplirPlan(PlanningRequest request) {
        if (request.louagisteId() == null || request.dateDebutSemaine() == null
                || request.lignesParJour() == null || request.lignesParJour().isEmpty()) {
            throw new RuntimeException("louagisteId, dateDebutSemaine et au moins une ligne sont obligatoires.");
        }
        Louagiste louagiste = utilisateurService.getLouagiste(request.louagisteId());
        LocalDate debut = request.dateDebutSemaine().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        if (planningRepository.existsByLouagisteIdAndDateDebutSemaine(louagiste.getId(), debut)) {
            throw new RuntimeException("Le planning de la semaine du " + debut
                    + " existe deja. Utilisez le changement de ligne pour le modifier.");
        }
        PlanningHebdomadaire planning = new PlanningHebdomadaire(louagiste, debut, debut.plusDays(DELAI_CLOTURE_JOURS));
        if (LocalDate.now().isAfter(planning.getDateCloture())) {
            throw new RuntimeException("Le planning de la semaine du " + debut
                    + " est cloture (depuis le " + planning.getDateCloture() + ").");
        }

        Map<LocalDate, Ligne> choix = new TreeMap<>();
        for (Map.Entry<LocalDate, Long> e : request.lignesParJour().entrySet()) {
            if (e.getValue() == null) {
                throw new RuntimeException("Aucune ligne choisie pour le " + e.getKey() + ".");
            }
            Ligne ligne = ligneRepository.findById(e.getValue())
                    .orElseThrow(() -> new RuntimeException("Ligne introuvable : " + e.getValue()));
            choix.put(e.getKey(), ligne);
        }

        planning.getTrajets().addAll(valider(planning, choix));
        return PlanningResponse.from(planningRepository.save(planning));
    }

    // valider(choix) du diagramme : controle les regles et cree un Trajet par jour choisi
    public List<Trajet> valider(PlanningHebdomadaire planning, Map<LocalDate, Ligne> choix) {
        List<Trajet> trajets = new ArrayList<>();
        for (Map.Entry<LocalDate, Ligne> e : choix.entrySet()) {
            LocalDate jour = e.getKey();
            Ligne ligne = e.getValue();
            if (jour.isBefore(planning.getDateDebutSemaine()) || jour.isAfter(planning.getDateDebutSemaine().plusDays(6))) {
                throw new RuntimeException("Le jour " + jour + " n'appartient pas a la semaine du "
                        + planning.getDateDebutSemaine() + ".");
            }
            if (jour.isBefore(LocalDate.now())) {
                throw new RuntimeException("Le jour " + jour + " est deja passe.");
            }
            if (!ligneAutorisee(planning.getLouagiste(), ligne)) {
                throw new RuntimeException("Ligne " + ligne.getId() + " non autorisee pour ce louagiste (jour " + jour + ").");
            }
            trajets.add(new Trajet(planning, planning.getLouagiste(), ligne, jour));
        }
        return trajets;
    }

    @Transactional(readOnly = true)
    public PlanningResponse getPlanning(Long louagisteId, LocalDate semaine) {
        LocalDate debut = semaine.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        PlanningHebdomadaire planning = planningRepository.findByLouagisteIdAndDateDebutSemaine(louagisteId, debut)
                .orElseThrow(() -> new RuntimeException(
                        "Aucun planning pour la semaine du " + debut + "."));
        return PlanningResponse.from(planning);
    }

    // getTrajet(date) du diagramme : le trajet d'un jour du planning
    @Transactional(readOnly = true)
    public TrajetResponse getTrajet(Long planningId, LocalDate jour) {
        PlanningHebdomadaire planning = planningRepository.findById(planningId)
                .orElseThrow(() -> new RuntimeException("Planning introuvable : " + planningId));
        Trajet trajet = planning.getTrajets().stream().filter(t -> t.getDate().equals(jour)).findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "Aucun trajet le " + jour + " dans ce planning."));
        int placesLibres = Trajet.NB_PLACES - reservationRepository
                .findNumerosOccupes(trajet.getId(), StatutReservation.ANNULEE).size();
        return TrajetResponse.from(trajet, placesLibres);
    }

    // US22 / US23 / US24 : changer la ligne d'un jour, avant la cloture du planning et avant le check-in
    @Transactional
    public TrajetResponse changerLigne(Long trajetId, ChangerLigneRequest request) {
        if (request.louagisteId() == null || request.ligneId() == null) {
            throw new RuntimeException("louagisteId et ligneId sont obligatoires.");
        }
        Trajet trajet = trajetRepository.findByIdForUpdate(trajetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trajet introuvable : " + trajetId));
        if (!trajet.getLouagiste().getId().equals(request.louagisteId())) {
            throw new RuntimeException("Ce trajet n'appartient pas a ce louagiste.");
        }
        if (LocalDate.now().isAfter(trajet.getPlanning().getDateCloture())) {
            throw new RuntimeException(
                    "Le planning est cloture : le changement de ligne n'est plus possible.");
        }
        if (trajet.isPresent()) {
            throw new RuntimeException("Impossible de changer de ligne apres le check-in.");
        }
        if (reservationRepository.existsByTrajetIdAndStatutNot(trajetId, StatutReservation.ANNULEE)) {
            throw new RuntimeException(
                    "Impossible de changer de ligne : des passagers ont deja reserve ce trajet.");
        }
        Ligne ligne = ligneRepository.findById(request.ligneId())
                .orElseThrow(() -> new RuntimeException(
                        "Ligne introuvable : " + request.ligneId()));
        if (ligne.getId().equals(trajet.getLigne().getId())) {
            throw new RuntimeException("Le trajet est deja sur cette ligne.");
        }
        if (!ligneAutorisee(trajet.getLouagiste(), ligne)) {
            throw new RuntimeException("Le louagiste n'est pas autorise sur cette ligne.");
        }
        trajet.setLigne(ligne);
        return TrajetResponse.from(trajet, Trajet.NB_PLACES);
    }

    // le louagiste ne peut choisir que parmi ses lignes autorisees
    private boolean ligneAutorisee(Louagiste louagiste, Ligne ligne) {
        return louagiste.getLignesAutorisees().stream().anyMatch(l -> l.getId().equals(ligne.getId()));
    }
}
