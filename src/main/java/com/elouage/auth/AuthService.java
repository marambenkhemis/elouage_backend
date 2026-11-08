package com.elouage.auth;

import com.elouage.document.Document;
import com.elouage.document.DocumentService;
import com.elouage.document.TypeDocument;
import com.elouage.utilisateur.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
public class AuthService {
    @Autowired
    private UtilisateurRepository utilisateurRepository;
    @Autowired
    private PassagerRepository passagerRepository;
    @Autowired
    private LouagisteRepository louagisteRepository;
    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private DocumentService documentService;

    //us01
    @Transactional
    public Map<String, Object> inscrirePassager(Passager p){
        verifierCommun(p);
        p.setId(null);
        p.setEmail(p.getEmail().trim().toLowerCase());
        p.setMotDePasse(encoder.encode(p.getMotDePasse()));
        p.setStatut(StatutCompte.ACTIF);
        return resume(passagerRepository.save(p));
    }

    // sprint 2: louagiste registration without documents (documents = US03, sprint 3); waits for the admin (US04)
    @Transactional
    public Map<String, Object> inscrireLouagiste(Louagiste l, Map<TypeDocument, MultipartFile> fichiers) {
        verifierCommun(l);
        exiger(l.getMatricule(), "matricule");
        exiger(l.getModele(), "modele");
        for (TypeDocument t : TypeDocument.values()) {            // fail fast, before any write
            documentService.verifier(t, fichiers.get(t));
        }
        l.setId(null);
        l.setEmail(l.getEmail().trim().toLowerCase());
        l.setMotDePasse(encoder.encode(l.getMotDePasse()));
        l.setStatut(StatutCompte.EN_ATTENTE);
        l.setLignesAutorisees(new ArrayList<>());   // only the admin gives lines
        Louagiste saved = louagisteRepository.save(l);            // we need the id for the folder

        List<Document> ecrits = new ArrayList<>();
        try {
            for (TypeDocument t : TypeDocument.values()) {
                ecrits.add(documentService.sauvegarder(saved, t, fichiers.get(t)));
            }
        } catch (RuntimeException e) {                            // the DB is rolled back, so remove the files already written
            ecrits.forEach(documentService::supprimerFichier);
            throw e;
        }
        Map<String, Object> res = resume(saved);
        res.put("documents", ecrits);
        return res;
    }

    // US02
    @Transactional(readOnly = true)
    public Map<String, Object> login(Map<String, String> body) {
        String email = body == null ? null : body.get("email");
        String mdp = body == null ? null : body.get("motDePasse");
        if (email == null || mdp == null) throw new RuntimeException("email et motDePasse sont obligatoires.");
        Utilisateur u = utilisateurRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new RuntimeException("Email ou mot de passe incorrect."));
        if (!encoder.matches(mdp, u.getMotDePasse()))
            throw new RuntimeException("Email ou mot de passe incorrect.");
        if (u.getStatut() == StatutCompte.EN_ATTENTE)
            throw new RuntimeException("Votre compte est en attente de validation par l'administrateur.");
        if (u.getStatut() == StatutCompte.SUSPENDU)
            throw new RuntimeException("Votre compte est suspendu.");
        Map<String, Object> res = resume(u);
        res.put("token", jwtService.generer(u));
        return res;
    }



    public static Map<String, Object> resume(Utilisateur u) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", u.getId());
        m.put("nom", u.getNom());
        m.put("prenom", u.getPrenom());
        m.put("email", u.getEmail());
        m.put("telephone", u.getTelephone());
        m.put("role", u.getRole());
        m.put("statut", u.getStatut());
        return m;
    }

    private void verifierCommun(Utilisateur u){
        exiger(u.getNom(), "nom");
        exiger(u.getPrenom(), "prenom");
        exiger(u.getEmail(), "email");
        exiger(u.getTelephone(), "telephone");
        if(!u.getEmail().contains("@")) throw new RuntimeException("Email invalide.");
        if(u.getMotDePasse()==null || u.getMotDePasse().length()<8) throw new RuntimeException("Le mot de passe doit contenir au moins 8 caractères.");
        if(utilisateurRepository.existsByEmailIgnoreCase(u.getEmail().trim())) throw new RuntimeException("Un compte existe déjà avec cet email.");
    }

    private void exiger(String valeur, String champ){
        if(valeur == null || valeur.isBlank()) throw new RuntimeException("Le champ " + champ + " est obligatoire.");
    }
}
