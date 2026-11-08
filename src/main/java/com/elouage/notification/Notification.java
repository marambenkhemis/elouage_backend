package com.elouage.notification;

import com.elouage.utilisateur.Utilisateur;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    private TypeNotification type;
    @Column(length = 500)
    private String message;
    private LocalDateTime dateEnvoi;
    private boolean lu;
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    private Utilisateur destinataire;

    public Notification() {
    }

    public Notification(Utilisateur destinataire, TypeNotification type, String message) {
        this.type = type;
        this.message = message;
        this.dateEnvoi = LocalDateTime.now();
        this.lu = false;
        this.destinataire = destinataire;
    }

    public Long getId() {
        return id;
    }

    public TypeNotification getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getDateEnvoi() {
        return dateEnvoi;
    }

    public void setLu(boolean lu) { this.lu = lu; }
    public boolean isLu() { return lu; }
    public Utilisateur getDestinataire() { return destinataire; }
}
