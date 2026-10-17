package com.elouage.trajet;

import com.elouage.ligne.Ligne;
import com.elouage.utilisateur.Louagiste;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class Trajet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate date;
    private boolean present;
    private int rang;

    @ManyToOne
    private Ligne ligne;

    @ManyToOne
    private Louagiste louagiste;

    public Trajet() { }

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
}
