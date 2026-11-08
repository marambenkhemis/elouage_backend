package com.elouage.document;

import com.elouage.utilisateur.Louagiste;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_document_louagiste_type",
        columnNames = {"louagiste_id", "type"}))   // one document per type per louagiste
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING) private TypeDocument type;
    @Enumerated(EnumType.STRING) private FormatDocument format;
    @JsonIgnore
    private String chemin;
    private String nomOriginal;
    private LocalDateTime dateDepot;
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Louagiste louagiste;

    public Document() {

    }

    public Document(Louagiste l, TypeDocument type, FormatDocument format, String chemin, String nomOriginal) {
        this.louagiste=l;
        this.type = type;
        this.format = format;
        this.chemin = chemin;
        this.nomOriginal = nomOriginal;
        this.dateDepot=LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TypeDocument getType() {
        return type;
    }

    public void setType(TypeDocument type) {
        this.type = type;
    }

    public FormatDocument getFormat() {
        return format;
    }

    public void setFormat(FormatDocument format) {
        this.format = format;
    }

    public String getChemin() {
        return chemin;
    }

    public void setChemin(String chemin) {
        this.chemin = chemin;
    }

    public String getNomOriginal() {
        return nomOriginal;
    }

    public void setNomOriginal(String nomOriginal) {
        this.nomOriginal = nomOriginal;
    }

    public LocalDateTime getDateDepot() {
        return dateDepot;
    }

    public void setDateDepot(LocalDateTime dateDepot) {
        this.dateDepot = dateDepot;
    }

    public Louagiste getLouagiste() {
        return louagiste;
    }

    public void setLouagiste(Louagiste louagiste) {
        this.louagiste = louagiste;
    }
}
