package com.elouage.utilisateur;

import com.elouage.ligne.Ligne;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Louagiste extends Utilisateur{
    private String matricule;
    private String modele;
    @ElementCollection
    private List<String> photosVehicule = new ArrayList<>();

    @ManyToMany
    private List<Ligne> lignesAutorisees = new ArrayList<>();

    public Louagiste() {
    }

    public Louagiste(String nom, String prenom, String email, String telephone, String motDePasse, String matricule, String modele) {
        super(nom, prenom, email, telephone, motDePasse);
        this.matricule = matricule;
        this.modele = modele;
    }

    public List<String> getPhotosVehicule() {
        return photosVehicule;
    }

    public void setPhotosVehicule(List<String> photosVehicule) {
        this.photosVehicule = photosVehicule;
    }

    public String getMatricule() {
        return matricule;
    }

    public void setMatricule(String matricule) {
        this.matricule = matricule;
    }

    public String getModele() {
        return modele;
    }

    public void setModele(String modele) {
        this.modele = modele;
    }

    public List<Ligne> getLignesAutorisees() {
        return lignesAutorisees;
    }

    public void setLignesAutorisees(List<Ligne> lignesAutorisees) {
        this.lignesAutorisees = lignesAutorisees;
    }
}
