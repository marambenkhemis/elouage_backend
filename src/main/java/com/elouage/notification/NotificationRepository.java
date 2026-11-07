package com.elouage.notification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification,Long> {
    List<Notification> findByDestinataireIdOrderByDateEnvoiDesc(Long utilisateurId);
    long countByDestinataireIdAndLuFalse(Long utilisateurId);
}
