package com.elouage.reservation;

import com.elouage.ligne.Ligne;
import com.elouage.trajet.Trajet;
import com.elouage.utilisateur.Passager;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int numeroPlace;
    private LocalDate dateReservation;

    @Enumerated(EnumType.STRING)
    private StatutReservation statut;

    @ManyToOne(fetch = FetchType.LAZY)
    private Passager passager;

    // null tant que le passager est en liste d'attente (US19)
    @ManyToOne(fetch = FetchType.LAZY)
    private Trajet trajet;

    // US19 : ligne et jour attendus (renseignes seulement pour une reservation EN_ATTENTE)
    @ManyToOne(fetch = FetchType.LAZY)
    private Ligne ligne;
    private LocalDate dateSouhaitee;

    // US19 : MargeDisponibilite du diagramme
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<PlageHoraire> margeDisponibilite = new HashSet<>();

    public Reservation() {
    }

    public Reservation(Passager passager, Trajet trajet, int numeroPlace) {
        this.passager = passager;
        this.trajet = trajet;
        this.numeroPlace = numeroPlace;
        this.dateReservation = LocalDate.now();
        this.statut = StatutReservation.CONFIRMEE;
    }
    // US19 : entree en liste d'attente (pas de trajet, pas de place)
    public Reservation(Passager passager, Ligne ligne, LocalDate dateSouhaitee, Set<PlageHoraire> margeDisponibilite) {
        this.passager = passager;
        this.ligne = ligne;
        this.dateSouhaitee = dateSouhaitee;
        this.margeDisponibilite = new HashSet<>(margeDisponibilite);
        this.numeroPlace = 0;
        this.dateReservation = LocalDate.now();
        this.statut = StatutReservation.EN_ATTENTE;
    }

    // le passager est absent, sa place redevient libre
    public void libererPlace() {
        this.statut = StatutReservation.ABSENT;
    }

    // US19/US20: plage compatible avec l'heure du trajet ? (heure inconnue ou aucune plage = compatible)
    public boolean estCompatibleAvec(LocalTime heure) {
        if (heure == null || margeDisponibilite == null || margeDisponibilite.isEmpty()) {
            return true;
        }
        return margeDisponibilite.stream().anyMatch(p -> p.contient(heure));
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getNumeroPlace() {
        return numeroPlace;
    }

    public void setNumeroPlace(int numeroPlace) {
        this.numeroPlace = numeroPlace;
    }

    public LocalDate getDateReservation() {
        return dateReservation;
    }

    public void setDateReservation(LocalDate dateReservation) {
        this.dateReservation = dateReservation;
    }

    public StatutReservation getStatut() {
        return statut;
    }

    public void setStatut(StatutReservation statut) {
        this.statut = statut;
    }

    public Passager getPassager() {
        return passager;
    }

    public void setPassager(Passager passager) {
        this.passager = passager;
    }

    public Trajet getTrajet() {
        return trajet;
    }

    public void setTrajet(Trajet trajet) {
        this.trajet = trajet;
    }
    public Ligne getLigne() {
        return ligne;
    }

    public void setLigne(Ligne ligne) {
        this.ligne = ligne;
    }

    public LocalDate getDateSouhaitee() {
        return dateSouhaitee;
    }

    public void setDateSouhaitee(LocalDate dateSouhaitee) {
        this.dateSouhaitee = dateSouhaitee;
    }

    public Set<PlageHoraire> getMargeDisponibilite() {
        return margeDisponibilite;
    }

    public void setMargeDisponibilite(Set<PlageHoraire> margeDisponibilite) {
        this.margeDisponibilite = margeDisponibilite;
    }
}
