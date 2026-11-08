package com.elouage.ligne;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LigneRepository extends JpaRepository<Ligne,Long> {
    List<Ligne> findByStationDepartVilleIgnoreCaseAndVilleDestinationIgnoreCase(
            String villeDepart, String villeDestination);
    //US10 / US27 : la ligne inverse = meme stations, sens contraire (ex. Tunis->Sousse  <->  Sousse->Tunis)
    Optional<Ligne> findFirstByStationDepartIdAndStationArriveeIdOrderByIdAsc(Long stationDepartId, Long stationArriveeId);

}
