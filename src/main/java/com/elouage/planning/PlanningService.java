package com.elouage.planning;

import com.elouage.ligne.Ligne;
import com.elouage.ligne.LigneRepository;
import com.elouage.reservation.ReservationRepository;
import com.elouage.reservation.StatutReservation;
import com.elouage.trajet.Trajet;
import com.elouage.trajet.TrajetRepository;
import com.elouage.trajet.TrajetResponse;
import com.elouage.trajet.TrajetService;
import com.elouage.utilisateur.Louagiste;
import com.elouage.utilisateur.UtilisateurService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
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
    private final TrajetService trajetService;
    private static final int DELAI_CLOTURE_JOURS = 6;

    public PlanningService(PlanningRepository planningRepository, TrajetRepository trajetRepository,
                           ReservationRepository reservationRepository, LigneRepository ligneRepository,
                           UtilisateurService utilisateurService,TrajetService trajetService) {
        this.planningRepository = planningRepository;
        this.trajetRepository = trajetRepository;
        this.reservationRepository = reservationRepository;
        this.ligneRepository = ligneRepository;
        this.utilisateurService = utilisateurService;
        this.trajetService = trajetService;

    }
    // le choix d'un jour : la ligne, aller simple ou aller-retour, et l'heure d'arrivee a la station
    public record ChoixJour(Ligne ligne, boolean allerRetour, LocalTime heureArrivee) { }
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

        Map<LocalDate, ChoixJour> choix = new TreeMap<>();
        for (Map.Entry<LocalDate,  PlanningRequest.JourRequest> e : request.lignesParJour().entrySet()) {
            PlanningRequest.JourRequest jour = e.getValue();
            if (jour == null || jour.ligneId() == null) {
                throw new RuntimeException("Aucune ligne choisie pour le " + e.getKey() + ".");
            }
            // l'heure d'arrivee est obligatoire (US10)
            if (jour.heureArrivee() == null) {
                throw new RuntimeException("Indiquez votre heure d'arrivee a la station pour le " + e.getKey() + ".");
            }
            Ligne ligne = ligneRepository.findById(jour.ligneId())
                    .orElseThrow(() -> new RuntimeException("Ligne introuvable : " + jour.ligneId()));
            choix.put(e.getKey(), new ChoixJour(ligne, jour.allerRetour(), jour.heureArrivee()));
        }

        planning.getTrajets().addAll(valider(planning, choix));
        //les passagers qui attendaient ces lignes ce jour-la recoivent une place automatiquement
        PlanningHebdomadaire sauve = planningRepository.save(planning);
        for (Trajet t : new ArrayList<>(sauve.getTrajets())) {
            trajetService.remplirDepuisAttente(t);
        }
        return PlanningResponse.from(sauve);
    }

    // valider(choix) du diagramme : controle les regles et cree les Trajets de chaque jour choisi
    // un trajet aller, et si "aller-retour" est coche, un 2e trajet sur la ligne inverse (retour = true)
    public List<Trajet> valider(PlanningHebdomadaire planning, Map<LocalDate, ChoixJour> choix) {
        List<Trajet> trajets = new ArrayList<>();
        for (Map.Entry<LocalDate, ChoixJour> e : choix.entrySet()) {
            LocalDate jour = e.getKey();
            ChoixJour c = e.getValue();
            Ligne ligne = c.ligne();
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
            //Trajet aller
            Trajet aller = new Trajet(planning, planning.getLouagiste(), ligne, jour);
            aller.setRetour(false);
            aller.setHeureArrivee(c.heureArrivee());
            trajets.add(aller);

            // NEW : trajet retour sur la ligne inverse
            if (c.allerRetour()) {
                Trajet retour = new Trajet(planning, planning.getLouagiste(), ligneInverse(ligne), jour);
                retour.setRetour(true);
                retour.setTrajetPrecedent(aller);
                if (ligne.getDureeTrajet() == null) {
                    throw new RuntimeException("La ligne " + ligne.getId()
                            + " n'a pas de duree de trajet : impossible de planifier le retour.");
                }
                // heure estimee du retour = heure d'arrivee + duree du trajet de la ligne
                if (ligne.getDureeTrajet() != null) {
                    LocalTime heureRetour = c.heureArrivee().plusMinutes(ligne.getDureeTrajet());
                    if (heureRetour.isBefore(c.heureArrivee())) {
                        throw new RuntimeException("Le retour du " + jour + " depasserait minuit.");
                    }
                    retour.setHeureArrivee(heureRetour);
                }
                trajets.add(retour);
            }
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

    // getTrajet(date) du diagramme : le trajet d'un jour du planning (l'aller si le jour est aller-retour)
    @Transactional(readOnly = true)
    public TrajetResponse getTrajet(Long planningId, LocalDate jour) {
        PlanningHebdomadaire planning = planningRepository.findById(planningId)
                .orElseThrow(() -> new RuntimeException("Planning introuvable : " + planningId));
        Trajet trajet = planning.getTrajets().stream().filter(t -> t.getDate().equals(jour)).findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "Aucun trajet le " + jour + " dans ce planning."));
        int placesLibres = Trajet.NB_PLACES - reservationRepository
                .findNumerosOccupes(trajet.getId(), StatutReservation.INACTIFS).size();
        return TrajetResponse.from(trajet, placesLibres);
    }

    // US22 / US23 / US24 : changer la ligne d'un jour, avant la cloture du planning et avant le check-in
    // Si le jour est aller-retour, le trajet retour suit automatiquement (ligne inverse de la nouvelle ligne).

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
        if (trajet.isRetour() || trajet.getTrajetPrecedent() != null) {
            throw new RuntimeException("Seul le trajet aller du planning peut changer de ligne.");
        }
        if (LocalDate.now().isAfter(trajet.getPlanning().getDateCloture())) {
            throw new RuntimeException(
                    "Le planning est cloture : le changement de ligne n'est plus possible.");
        }
        if (trajet.isPresent()) {
            throw new RuntimeException("Impossible de changer de ligne apres le check-in.");
        }
        if (reservationRepository.existsByTrajetIdAndStatutNotIn(trajetId, StatutReservation.INACTIFS)) {
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
        // jour aller-retour : le retour doit suivre, s'il est encore intact
        Trajet retourLie = null;
        var suivant = trajetRepository.findFirstByTrajetPrecedentId(trajetId);
        if (suivant.isPresent()) {
            Trajet retour = trajetRepository.findByIdForUpdate(suivant.get().getId())
                    .orElseThrow(() -> new RuntimeException("Trajet retour introuvable."));
            if (retour.isPresent() || trajetRepository.existsByTrajetPrecedentId(retour.getId())
                    || reservationRepository.existsByTrajetIdAndStatutNotIn(retour.getId(), StatutReservation.INACTIFS)) {
                throw new RuntimeException(
                        "Impossible de changer de ligne : le trajet retour est deja utilise (check-in ou reservations).");
            }
            retour.setLigne(ligneInverse(ligne));
            retourLie = retour;
        }
        trajet.setLigne(ligne);
        // les passagers qui attendaient la nouvelle ligne ce jour-la recoivent une place
        trajetService.remplirDepuisAttente(trajet);
        if (retourLie != null) {
            trajetService.remplirDepuisAttente(retourLie);
        }
        return TrajetResponse.from(trajet, trajetService.getPlacesLibres(trajet.getId()).size());
    }

    // le louagiste ne peut choisir que parmi ses lignes autorisees
    private boolean ligneAutorisee(Louagiste louagiste, Ligne ligne) {
        return louagiste.getLignesAutorisees().stream().anyMatch(l -> l.getId().equals(ligne.getId()));
    }

    // la ligne inverse (retour) : memes stations, sens contraire. Elle n'a pas besoin d'etre dans les lignes
    // autorisees : le retour est la suite logique de l'aller.
    private Ligne ligneInverse(Ligne ligne) {
        if (ligne.getStationDepart() == null || ligne.getStationArrivee() == null) {
            throw new RuntimeException("La ligne " + ligne.getId() + " n'a pas de stations definies.");
        }
        return ligneRepository.findFirstByStationDepartIdAndStationArriveeIdOrderByIdAsc(
                        ligne.getStationArrivee().getId(), ligne.getStationDepart().getId())
                .orElseThrow(() -> new RuntimeException(
                        "Aucune ligne inverse pour la ligne " + ligne.getId() + " : ajoutez-la d'abord."));
    }
}
