package com.elouage.auth;

import com.elouage.document.DocumentService;
import com.elouage.document.TypeDocument;
import com.elouage.ligne.Ligne;
import com.elouage.ligne.LigneRepository;
import com.elouage.notification.NotificationService;
import com.elouage.notification.TypeNotification;
import com.elouage.utilisateur.Louagiste;
import com.elouage.utilisateur.LouagisteRepository;
import com.elouage.utilisateur.StatutCompte;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {
    @Autowired
    private LouagisteRepository louagisteRepository;
    @Autowired
    private LigneRepository ligneRepository;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private DocumentService documentService;

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listerLouagistes(StatutCompte statut) {
        return louagisteRepository.findAll().stream()
                .filter(l -> statut == null || l.getStatut() == statut)
                .map(this::vue).toList();
    }

    @Transactional
    public Map<String, Object> validerCompte(Long id, Map<String, List<Long>> body){
        Louagiste l=louagisteRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Louagiste introuvable : " + id));
        if(l.getStatut() != StatutCompte.EN_ATTENTE)
            throw new RuntimeException("Ce compte n'est pas en attente de validation.");
        List<TypeDocument> manquants = documentService.typesManquants(l.getId());
        if (!manquants.isEmpty())
            throw new RuntimeException("Dossier incomplet, documents manquants : " + manquants);
        List<Long> ligneIds = body == null ? null : body.get("ligneIds");
        if (ligneIds == null || ligneIds.isEmpty())
            throw new RuntimeException("Au moins une ligne autorisée est obligatoire (ligneIds).");
        List<Ligne> lignes = ligneRepository.findAllById(ligneIds);
        if (lignes.size() != new HashSet<>(ligneIds).size())
            throw new RuntimeException("Une des lignes est introuvable.");
        l.setLignesAutorisees(new ArrayList<>(lignes));
        l.setStatut(StatutCompte.ACTIF);
        notificationService.envoyer(l, TypeNotification.TRAJET,
                "Votre compte a été validé. Vous pouvez maintenant remplir votre planning.");

        return vue(l);
    }

    private Map<String, Object> vue(Louagiste l) {
        Map<String, Object> m = AuthService.resume(l);
        m.put("matricule", l.getMatricule());
        m.put("modele", l.getModele());
        m.put("lignesAutorisees", l.getLignesAutorisees().stream().map(Ligne::getId).toList());
        m.put("documents", documentService.lister(l.getId())); //[{id,type,format,nomOriginal,dateDepot}], no path exposed
        return m;
    }
}
