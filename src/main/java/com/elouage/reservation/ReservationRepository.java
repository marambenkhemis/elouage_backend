package com.elouage.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByTrajetIdAndNumeroPlaceAndStatutNot(Long trajetId, int numeroPlace, StatutReservation statut);

    boolean existsByTrajetIdAndPassagerIdAndStatutNot(Long trajetId, Long passagerId, StatutReservation statut);

    boolean existsByTrajetIdAndStatutNot(Long trajetId, StatutReservation statut);

    List<Reservation> findByPassagerIdOrderByDateReservationDescIdDesc(Long passagerId);

    List<Reservation> findByTrajetIdAndStatutOrderByNumeroPlaceAsc(Long trajetId, StatutReservation statut);

    @Query("select r.numeroPlace from Reservation r where r.trajet.id = :trajetId and r.statut <> :statut")
    List<Integer> findNumerosOccupes(@Param("trajetId") Long trajetId, @Param("statut") StatutReservation statut);
}
