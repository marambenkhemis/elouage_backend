package com.elouage.trajet;

import com.elouage.ligne.Ligne;
import com.elouage.planning.PlanningHebdomadaire;
import com.elouage.utilisateur.Louagiste;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_trajet_louagiste_jour",
        columnNames = {"louagiste_id", "date_trajet"}))
public class Trajet {
    public static final int NB_PLACES = 8;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_trajet")
    private LocalDate date;

    // true apres le check-in du louagiste dans la station
    private boolean present;

    // position dans la file de depart (1 = premier). 0 tant que le check-in n'est pas fait.
    private int rang;

    private LocalDateTime heureCheckIn;

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

    public int getRang() {
        return rang;
    }

    public void setRang(int rang) {
        this.rang = rang;
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
}
