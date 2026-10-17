package com.elouage.ligne;

import jakarta.persistence.*;

@Entity
public class Ligne {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int minimumLouagistes;
    private String villeDestination;
    private String delegationDestination;
    private double prix;

    @ManyToOne
    private Station stationDepart;

    @ManyToOne
    private Station stationArrivee;

    public Ligne() {
    }

    public Ligne(Station stationDepart, Station stationArrivee, int minimumLouagistes,
                 String villeDestination, String delegationDestination, double prix) {
        this.stationDepart = stationDepart;
        this.stationArrivee = stationArrivee;
        this.minimumLouagistes = minimumLouagistes;
        this.villeDestination = villeDestination;
        this.delegationDestination = delegationDestination;
        this.prix = prix;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getMinimumLouagistes() {
        return minimumLouagistes;
    }

    public void setMinimumLouagistes(int minimumLouagistes) {
        this.minimumLouagistes = minimumLouagistes;
    }

    public String getVilleDestination() {
        return villeDestination;
    }

    public void setVilleDestination(String villeDestination) {
        this.villeDestination = villeDestination;
    }

    public String getDelegationDestination() {
        return delegationDestination;
    }

    public void setDelegationDestination(String delegationDestination) {
        this.delegationDestination = delegationDestination;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public Station getStationDepart() {
        return stationDepart;
    }

    public void setStationDepart(Station stationDepart) {
        this.stationDepart = stationDepart;
    }

    public Station getStationArrivee() {
        return stationArrivee;
    }

    public void setStationArrivee(Station stationArrivee) {
        this.stationArrivee = stationArrivee;
    }
}
