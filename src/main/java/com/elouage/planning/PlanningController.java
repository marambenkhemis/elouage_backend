package com.elouage.planning;

import com.elouage.trajet.TrajetResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/plannings")
public class PlanningController {

    private final PlanningService planningService;

    public PlanningController(PlanningService planningService) {
        this.planningService = planningService;
    }

    // US10  POST http://localhost:8081/api/plannings
    // {"louagisteId": 1, "dateDebutSemaine": "2026-10-19",
    //  "lignesParJour": {"2026-10-19": 1, "2026-10-20": 2, "2026-10-21": 1}}
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlanningResponse remplirPlan(@RequestBody PlanningRequest request) {
        return planningService.remplirPlan(request);
    }

    // GET http://localhost:8081/api/plannings?louagisteId=1&semaine=2026-10-21   (n'importe quelle date de la semaine)
    @GetMapping
    public PlanningResponse getPlanning(
            @RequestParam Long louagisteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate semaine) {
        return planningService.getPlanning(louagisteId, semaine);
    }

    // GET http://localhost:8081/api/plannings/1/trajet?date=2026-10-20
    @GetMapping("/{planningId}/trajet")
    public TrajetResponse getTrajet(
            @PathVariable Long planningId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return planningService.getTrajet(planningId, date);
    }

    // US22 / US23 / US24  PUT http://localhost:8081/api/plannings/trajets/2/ligne
    // {"louagisteId": 1, "ligneId": 2}
    @PutMapping("/trajets/{trajetId}/ligne")
    public TrajetResponse changerLigne(@PathVariable Long trajetId, @RequestBody ChangerLigneRequest request) {
        return planningService.changerLigne(trajetId, request);
    }
}
