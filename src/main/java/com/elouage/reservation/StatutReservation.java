package com.elouage.reservation;
import java.util.List;

public enum StatutReservation {
    CONFIRMEE,
    PRESENTE,
    EMBARQUEE,
    ANNULEE,
    ABSENT, // US18 : le louagiste a marque le passager absent, sa place est libre
    EN_ATTENTE;// US19 : passager en liste d'attente

    // statuts qui ne bloquent plus une place du trajet
    public static final List<StatutReservation> INACTIFS = List.of(ANNULEE, ABSENT);

}
