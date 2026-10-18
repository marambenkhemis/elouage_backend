package com.elouage.ligne;

import com.elouage.utilisateur.Louagiste;
import com.elouage.utilisateur.LouagisteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class LouagisteLigneService {

    private final LouagisteRepository louagisteRepository;

    public LouagisteLigneService(LouagisteRepository louagisteRepository) {
        this.louagisteRepository = louagisteRepository;
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
}