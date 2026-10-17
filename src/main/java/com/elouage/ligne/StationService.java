package com.elouage.ligne;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class StationService {
    private static final String API_URL = "https://tn-municipality-api.vercel.app/api/municipalities";

    private final StationRepository stationRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private List<LocalisationResponse> localisationsCache;

    public StationService(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }
    public Station ajouterStation(Station station){
        return stationRepository.save(station);
    }
    public List<Station> getStations(){
        return stationRepository.findAll();
    }

    public List<LocalisationResponse> getLocalisations() {

        if (localisationsCache == null) {

            VilleApi[] villes = restTemplate.getForObject(API_URL, VilleApi[].class);
            localisationsCache = new ArrayList<>();

            if (villes != null) {
                for (VilleApi ville : villes) {

                    List<String> delegations = new ArrayList<>();

                    if (ville.delegations() != null) {
                        for (DelegationApi delegation : ville.delegations()) {
                            String nom = delegation.value();

                            if (!delegations.contains(nom)) {
                                delegations.add(nom);
                            }
                        }
                    }
                    LocalisationResponse response =
                            new LocalisationResponse(ville.value(), delegations);
                    localisationsCache.add(response);
                }
            }
        }

        return localisationsCache;
    }

}
