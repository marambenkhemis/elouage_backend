package com.elouage.ligne;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;


@RestController
@RequestMapping("/api/lignes")
public class LigneController {

    private final LigneService ligneService;

    public LigneController(LigneService ligneService) {
        this.ligneService = ligneService;
    }
    /* http://localhost:8081/api/lignes   {
  "stationDepartId": 1,
  "stationArriveeId": 2,
  "villeDestination": "SOUSSE",
  "delegationDestination": "SOUSSE VILLE",
  "prix": 12.5,
  "minimumLouagistes": 3
}*/
    @PostMapping
    public Ligne ajouter(@RequestBody LigneRequest request) {
        return ligneService.ajouterLigne(request);
    }

    @GetMapping
    public List<Ligne> lister() {
        return ligneService.getLignes();
    }
    //http://localhost:8081/api/lignes/recherche?depart=ARIANA&destination=SOUSSE&date=2026-10-20
    @GetMapping("/recherche")
    public List<TrajetResultatResponse> rechercher(
            @RequestParam String depart,
            @RequestParam String destination,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ligneService.rechercher(depart, destination, date);
    }
    // http://localhost:8081/api/lignes/trajets/1/places
    @GetMapping("/trajets/{trajetId}/places")
    public EtatPlacesResponse etatPlaces(@PathVariable Long trajetId) {
        return ligneService.getEtatPlaces(trajetId);
    }
}