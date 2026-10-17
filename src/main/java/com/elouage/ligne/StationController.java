package com.elouage.ligne;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class StationController {
    private final StationService stationService;

    public StationController(StationService stationService) {
        this.stationService = stationService;
    }
    // post http://localhost:8081/api/stations { "nom": "Station Sousse", "ville": "SOUSSE", "delegation": "SOUSSE VILLE" } { "nom": "Station Ariana", "ville": "ARIANA", "delegation": "ARIANA VILLE" }

    @PostMapping("/stations")
    public Station ajouter(@RequestBody Station station){
        return stationService.ajouterStation(station);
    }
    @GetMapping("/stations")
    public List<Station> lister() {
        return stationService.getStations();
    }
    // get http://localhost:8081/api/localisations
    @GetMapping("/localisations")
    public List<LocalisationResponse> localisations() {
        return stationService.getLocalisations();
    }
}
