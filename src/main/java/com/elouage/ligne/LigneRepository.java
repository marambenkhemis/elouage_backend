package com.elouage.ligne;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LigneRepository extends JpaRepository<Ligne,Long> {
    List<Ligne> findByStationDepartVilleIgnoreCaseAndVilleDestinationIgnoreCase(
            String villeDepart, String villeDestination);
}
