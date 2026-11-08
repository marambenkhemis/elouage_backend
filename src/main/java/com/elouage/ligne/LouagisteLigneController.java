package com.elouage.ligne;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/louagistes")
public class LouagisteLigneController {

    private final LouagisteLigneService service;

    public LouagisteLigneController(LouagisteLigneService service) {
        this.service = service;
    }

    // GET http://localhost:8081/api/louagistes/1/lignes
    @GetMapping("/{louagisteId}/lignes")
    public List<LigneResponse> getLignesAutorisees(@PathVariable Long louagisteId) {
        return service.getLignesAutorisees(louagisteId);
    }
    // US21  GET http://localhost:8081/api/louagistes/1/lignes/inscrits?date=2026-11-10
    // => [{ "id": 1, "depart": "TUNIS", "arrivee": "SOUSSE", "minimumLouagistes": 3,
    //       "nbLouagistesInscrits": 2, "sousMinimum": true }, ...]
    @GetMapping("/{louagisteId}/lignes/inscrits")
    public List<LigneEtatResponse> getLignesAvecInscrits(
            @PathVariable Long louagisteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return service.getLignesAvecInscrits(louagisteId, date);
    }
}
