package com.elouage.ligne;

import com.elouage.trajet.Trajet;
import com.elouage.trajet.TrajetService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class LigneService {
    private static final int NB_PLACES=8;

    private final LigneRepository ligneRepository;
    private final StationRepository stationRepository;
    private final TrajetService trajetService;

    public LigneService(LigneRepository ligneRepository, StationRepository stationRepository, TrajetService trajetService) {
        this.ligneRepository = ligneRepository;
        this.stationRepository = stationRepository;
        this.trajetService = trajetService;
    }

    //US09
    public Ligne ajouterLigne(LigneRequest request) {
        Station depart = stationRepository.findById(request.stationDepartId())
                .orElseThrow(() -> new RuntimeException("Station de départ introuvable"));
        Station arrivee = stationRepository.findById(request.stationArriveeId())
                .orElseThrow(() -> new RuntimeException("Station d'arrivée introuvable"));

        Ligne ligne = new Ligne(depart, arrivee, request.minimumLouagistes(),
                request.villeDestination(), request.delegationDestination(), request.prix());
        return ligneRepository.save(ligne);
    }

    public List<Ligne> getLignes() {
        return ligneRepository.findAll();
    }
    //US11
    public List<TrajetResultatResponse> rechercher(String villeDepart, String villeDestination, LocalDate date) {
        List<Ligne> lignes = ligneRepository
                .findByStationDepartVilleIgnoreCaseAndVilleDestinationIgnoreCase(villeDepart, villeDestination);

        List<TrajetResultatResponse> resultats = new ArrayList<>();
        for (Ligne ligne : lignes) {
            for (Trajet t : trajetService.getTrajets(ligne.getId(), date)) {
                resultats.add(new TrajetResultatResponse(
                        t.getId(), t.getDate(),
                        ligne.getStationDepart().getVille(), ligne.getVilleDestination(),
                        ligne.getPrix(),
                        t.getLouagiste().getNom(), t.getLouagiste().getMatricule(),
                        trajetService.getPlacesLibres(t.getId()).size()));
            }
        }
        return resultats;
    }

    // US12
    public EtatPlacesResponse getEtatPlaces(Long trajetId) {
        List<Integer> libres = trajetService.getPlacesLibres(trajetId);
        List<EtatPlacesResponse.PlaceEtat> places = new ArrayList<>();
        for (int n = 1; n <= NB_PLACES; n++) {
            places.add(new EtatPlacesResponse.PlaceEtat(n, libres.contains(n)));
        }
        return new EtatPlacesResponse(trajetId, libres.size(), places);
    }



}
