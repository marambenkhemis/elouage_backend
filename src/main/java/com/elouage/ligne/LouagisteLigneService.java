package com.elouage.ligne;

import com.elouage.trajet.TrajetRepository;
import com.elouage.utilisateur.Louagiste;
import com.elouage.utilisateur.LouagisteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class LouagisteLigneService {

    private final LouagisteRepository louagisteRepository;
    private final TrajetRepository trajetRepository;

    public LouagisteLigneService(LouagisteRepository louagisteRepository,TrajetRepository trajetRepository) {
        this.louagisteRepository = louagisteRepository;
        this.trajetRepository = trajetRepository;


    }
    // @Transactional : la liste des lignes est chargée en LAZY
    @Transactional(readOnly = true)
    public List<LigneResponse> getLignesAutorisees(Long louagisteId) {
        Louagiste louagiste = louagisteRepository.findById(louagisteId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Louagiste introuvable"));
        return louagiste.getLignesAutorisees().stream()
                .map(LigneResponse::from)
                .toList();
    }

    // US21 : pour chaque ligne autorisee du louagiste, le nombre de louagistes deja inscrits ce jour-la.
    @Transactional(readOnly = true)
    public List<LigneEtatResponse> getLignesAvecInscrits(Long louagisteId, LocalDate date) {
        Louagiste louagiste = getLouagiste(louagisteId);
        return louagiste.getLignesAutorisees().stream()
                .map(l -> LigneEtatResponse.from(l, (int) trajetRepository.countLouagistesInscrits(l.getId(), date)))
                .toList();
    }

    private Louagiste getLouagiste(Long louagisteId) {
        return louagisteRepository.findById(louagisteId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Louagiste introuvable"));
    }

}