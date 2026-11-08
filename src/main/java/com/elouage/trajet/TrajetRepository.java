package com.elouage.trajet;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TrajetRepository extends JpaRepository<Trajet, Long> {
    List<Trajet> findByLigneIdAndDate(Long ligneId, LocalDate date);

    // verrou en ecriture sur un trajet (reservation d'une place,promotion,check in)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Trajet t where t.id = :id")
    Optional<Trajet> findByIdForUpdate(@Param("id") Long id);

    // US21 :nombre de louagistes inscrits sur une ligne un jour donne (trajets aller seulement)
    @Query("select count(distinct t.louagiste.id) from Trajet t "
            + "where t.ligne.id = :ligneId and t.date = :date and t.retour = false")
    long countLouagistesInscrits(@Param("ligneId") Long ligneId, @Param("date") LocalDate date);

    //US27: le trajet suivant existe deja?
    boolean existsByTrajetPrecedentId(Long trajetPrecedentId);

    //US10: le retour qui suit un aller
    Optional<Trajet> findFirstByTrajetPrecedentId(Long trajetPrecedentId);
}
