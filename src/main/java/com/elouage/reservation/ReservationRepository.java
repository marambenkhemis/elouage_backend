package com.elouage.reservation;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // StatutNotIn + une liste de statuts (INACTIFS = ANNULEE, ABSENTE) au lieu de StatutNot + ANNULEE
    boolean existsByTrajetIdAndNumeroPlaceAndStatutNotIn(Long trajetId, int numeroPlace, Collection<StatutReservation> statuts);

    boolean existsByTrajetIdAndPassagerIdAndStatutNotIn(Long trajetId, Long passagerId, Collection<StatutReservation> statuts);

    boolean existsByTrajetIdAndStatutNotIn(Long trajetId, Collection<StatutReservation> statuts);

    List<Reservation> findByPassagerIdOrderByDateReservationDescIdDesc(Long passagerId);

    List<Reservation> findByTrajetIdAndStatutOrderByNumeroPlaceAsc(Long trajetId, StatutReservation statut);

    // toutes les reservations qui bloquent une place sur le trajet (pour le louagiste)
    List<Reservation> findByTrajetIdAndStatutNotInOrderByNumeroPlaceAsc(Long trajetId, Collection<StatutReservation> statuts);

    //le 2e parametre est maintenant une liste de statuts exclus
    @Query("select r.numeroPlace from Reservation r where r.trajet.id = :trajetId and r.statut not in :statuts")
    List<Integer> findNumerosOccupes(@Param("trajetId") Long trajetId, @Param("statuts") Collection<StatutReservation> statuts);

    // US19 : le passager est-il deja en liste d'attente pour cette ligne et ce jour ?
    boolean existsByLigneIdAndDateSouhaiteeAndPassagerIdAndStatut(
            Long ligneId, LocalDate dateSouhaitee, Long passagerId, StatutReservation statut);

    // US19/US20 : la liste d'attente d'une ligne pour un jour, dans l'ordre d'arrivee (le plus ancien d'abord)
    List<Reservation> findByLigneIdAndDateSouhaiteeAndStatutAndTrajetIsNullOrderByIdAsc(
            Long ligneId, LocalDate dateSouhaitee, StatutReservation statut);

    // verrou en ecriture sur une reservation (presence du passager / reception d'un passager en attente)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Reservation r where r.id = :id")
    Optional<Reservation> findByIdForUpdate(@Param("id") Long id);

    // US18 : verrou sur les reservations d'un trajet ayant ce statut (un passager ne peut pas signaler sa presence
    // pendant que le louagiste libere  le marque absent)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Reservation r where r.trajet.id = :trajetId and r.statut = :statut order by r.numeroPlace")
    List<Reservation> findByTrajetAndStatutForUpdate(@Param("trajetId") Long trajetId,
                                                     @Param("statut") StatutReservation statut);
}
