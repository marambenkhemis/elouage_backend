package com.elouage.trajet;

import com.elouage.ligne.Ligne;
import com.elouage.planning.PlanningHebdomadaire;
import com.elouage.utilisateur.Louagiste;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity

public class Trajet {
    public static final int NB_PLACES = 8;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_trajet")
    private LocalDate date;

    // true apres le check-in du louagiste dans la station
    private boolean present;

    private LocalDateTime heureCheckIn;

    // US10/US27: true = trajet retour (cree sur la ligne inverse du trajet precedent)
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean retour;

    // US10 : heure d'arrivee du louagiste a la station de depart
    private LocalTime heureArrivee;

    // US27: trajet qui precede celui-ci (aller -> retour, retour -> nouvel aller). null pour un trajet du planning.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trajet_precedent_id")
    private Trajet trajetPrecedent;

    @ManyToOne
    @JoinColumn(name = "ligne_id")
    private Ligne ligne;

    @ManyToOne
    @JoinColumn(name = "louagiste_id")
    private Louagiste louagiste;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "planning_id")
    private PlanningHebdomadaire planning;

    public Trajet() { }

    public Trajet(PlanningHebdomadaire planning, Louagiste louagiste, Ligne ligne, LocalDate date) {
        this.planning = planning;
        this.louagiste = louagiste;
        this.ligne = ligne;
        this.date = date;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public boolean isPresent() {
        return present;
    }

    public void setPresent(boolean present) {
        this.present = present;
    }


    public LocalDateTime getHeureCheckIn() {
        return heureCheckIn;
    }

    public void setHeureCheckIn(LocalDateTime heureCheckIn) {
        this.heureCheckIn = heureCheckIn;
    }

    public Ligne getLigne() {
        return ligne;
    }

    public void setLigne(Ligne ligne) {
        this.ligne = ligne;
    }

    public Louagiste getLouagiste() {
        return louagiste;
    }

    public void setLouagiste(Louagiste louagiste) {
        this.louagiste = louagiste;
    }

    public PlanningHebdomadaire getPlanning() {
        return planning;
    }

    public void setPlanning(PlanningHebdomadaire planning) {
        this.planning = planning;
    }
    public boolean isRetour() { return retour; }
    public void setRetour(boolean retour) { this.retour = retour; }

    public LocalTime getHeureArrivee() { return heureArrivee; }
    public void setHeureArrivee(LocalTime heureArrivee) { this.heureArrivee = heureArrivee; }

    public Trajet getTrajetPrecedent() { return trajetPrecedent; }
    public void setTrajetPrecedent(Trajet trajetPrecedent) { this.trajetPrecedent = trajetPrecedent; }
}
