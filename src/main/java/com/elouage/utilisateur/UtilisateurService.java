package com.elouage.utilisateur;

import org.springframework.stereotype.Service;

@Service
public class UtilisateurService {
    private final PassagerRepository passagerRepository;
    private final LouagisteRepository louagisteRepository;

    public UtilisateurService(PassagerRepository passagerRepository, LouagisteRepository louagisteRepository) {
        this.passagerRepository = passagerRepository;
        this.louagisteRepository=louagisteRepository;
    }
    public Passager getPassager(Long id){
        return passagerRepository.findById(id).orElseThrow(()-> new RuntimeException("Passager introuvable: "+id ));
    }
    public Louagiste getLouagiste(Long id){
        return louagisteRepository.findById(id).orElseThrow(()->new RuntimeException("Louagiste introuvable: "+id));
    }
}
