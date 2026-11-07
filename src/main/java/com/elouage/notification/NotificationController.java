package com.elouage.notification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("notifications")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public List<Notification> lister(@RequestParam Long utilisateurId){
        //AuthUtil.varifier(utilisateurId);
        return notificationService.mesNotifications(utilisateurId);
    }

    @GetMapping("/non-lues")
    public Map<String,Long> nonlues(@RequestParam Long utilisateurId){
        //AuthUtil.varifier(utilisateurId);
        return Map.of("nonLues", notificationService.nbNonLues(utilisateurId));
    }

    @PutMapping("/{id}/lue")
    public Notification marquerLue(@PathVariable Long id, @RequestParam Long utilisateurId) {
        //AuthUtil.verifier(utilisateurId);
        return notificationService.marquerLue(id, utilisateurId);
    }
}
