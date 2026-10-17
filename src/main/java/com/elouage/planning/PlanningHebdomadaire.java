package com.elouage.planning;

import com.elouage.ligne.Ligne;
import com.elouage.trajet.Trajet;
import com.elouage.utilisateur.Louagiste;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_planning_louagiste_semaine",
        columnNames = {"louagiste_id", "date_debut_semaine"}))
public class PlanningHebdomadaire {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "date_debut_semaine")
    private LocalDate dateDebutSemaine;
    private LocalDate dateCloture;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "louagiste_id")
    private Louagiste louagiste;

    @OneToMany(mappedBy = "planning", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("date ASC")
    private List<Trajet> trajets = new ArrayList<>();

    public PlanningHebdomadaire() { }

    public PlanningHebdomadaire(Louagiste louagiste, LocalDate dateDebutSemaine, LocalDate dateCloture) {
        this.louagiste = louagiste;
        this.dateDebutSemaine = dateDebutSemaine;
        this.dateCloture = dateCloture;
    }

    // lignesParJour du diagramme : calcule a partir des trajets du planning (date -> ligne), pas de colonne en base
    public Map<LocalDate, Ligne> getLignesParJour() {
        Map<LocalDate, Ligne> lignesParJour = new TreeMap<>();
        for (Trajet t : trajets) {
            lignesParJour.put(t.getDate(), t.getLigne());
        }
        return lignesParJour;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDateDebutSemaine() {
        return dateDebutSemaine;
    }

    public void setDateDebutSemaine(LocalDate dateDebutSemaine) {
        this.dateDebutSemaine = dateDebutSemaine;
    }

    public LocalDate getDateCloture() {
        return dateCloture;
    }

    public void setDateCloture(LocalDate dateCloture) {
        this.dateCloture = dateCloture;
    }

    public Louagiste getLouagiste() {
        return louagiste;
    }

    public void setLouagiste(Louagiste louagiste) {
        this.louagiste = louagiste;
    }

    public List<Trajet> getTrajets() {
        return trajets;
    }

    public void setTrajets(List<Trajet> trajets) {
        this.trajets = trajets;
    }
}
