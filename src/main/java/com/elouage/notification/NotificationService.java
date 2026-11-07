package com.elouage.notification;

import com.elouage.utilisateur.Utilisateur;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;

    // UML: Utilisateur.recevoirNotification(message)
    @Transactional
    public void envoyer(Utilisateur destinataire, TypeNotification type, String message) {
        notificationRepository.save(new Notification(destinataire, type, message));
    }

    @Transactional(readOnly = true)
    public List<Notification> mesNotifications(Long utilisateurId) {
        return notificationRepository.findByDestinataireIdOrderByDateEnvoiDesc(utilisateurId);
    }

    @Transactional(readOnly = true)
    public long nbNonLues(Long utilisateurId) {
        return notificationRepository.countByDestinataireIdAndLuFalse(utilisateurId);
    }

    @Transactional
    public Notification marquerLue(Long id, Long utilisateurId){
        Notification n=notificationRepository.findById(id)
                .orElseThrow(()->new RuntimeException("Notification introuvable : " + id));
        if(!n.getDestinataire().getId().equals(utilisateurId)){
            throw new RuntimeException("Cette notification appartient à un autre utilisateur.");
        }
        n.setLu(true);
        return n;
    }
}
