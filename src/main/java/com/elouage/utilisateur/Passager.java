package com.elouage.utilisateur;

import jakarta.persistence.Entity;

@Entity
public class Passager extends Utilisateur{
    public Passager() {
    }

    public Passager(String nom, String prenom, String email, String telephone, String motDePasse) {
        super(nom, prenom, email, telephone, motDePasse);
    }
}
