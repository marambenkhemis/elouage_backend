package com.elouage.utilisateur;

import jakarta.persistence.Entity;

@Entity
public class Admin extends Utilisateur {
    public Admin() {
    }

    public Admin(String nom, String prenom, String email, String telephone, String motDePasse) {
        super(nom, prenom, email, telephone, motDePasse);
    }
}
