package com.elouage.auth;

import com.elouage.document.TypeDocument;
import com.elouage.utilisateur.Louagiste;
import com.elouage.utilisateur.Passager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/inscription/passager")
    public ResponseEntity<?> inscrirePassager(@RequestBody Passager passager) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(authService.inscrirePassager(passager));
        } catch (RuntimeException e) {
            return erreur(e, HttpStatus.BAD_REQUEST);
        }
    }



    @PostMapping(value = "/inscription/louagiste", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> inscrireLouagiste(
            @RequestParam String nom, @RequestParam String prenom, @RequestParam String email,
            @RequestParam String telephone, @RequestParam String motDePasse,
            @RequestParam String matricule, @RequestParam String modele,
            @RequestParam MultipartFile cin, @RequestParam MultipartFile permis,
            @RequestParam MultipartFile carteGrise, @RequestParam MultipartFile autorisationTransport) {
        try {
            Louagiste l = new Louagiste(nom, prenom, email, telephone, motDePasse, matricule, modele);
            Map<TypeDocument, MultipartFile> f = new EnumMap<>(TypeDocument.class);
            f.put(TypeDocument.CIN, cin);
            f.put(TypeDocument.PERMIS, permis);
            f.put(TypeDocument.CARTE_GRISE, carteGrise);
            f.put(TypeDocument.AUTORISATION_TRANSPORT, autorisationTransport);
            return ResponseEntity.status(HttpStatus.CREATED).body(authService.inscrireLouagiste(l, f));
        } catch (RuntimeException e) {
            return erreur(e, HttpStatus.BAD_REQUEST);
        }
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.ok(authService.login(body));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", e.getMessage()));
        }
    }


    private ResponseEntity<Map<String, Object>> erreur(RuntimeException e, HttpStatus parDefaut) {
        HttpStatus status = parDefaut;
        String message = e.getMessage() == null ? "Erreur inattendue." : e.getMessage();

        if (e instanceof ResponseStatusException rse) {          // if the service gives a status, use it
            status = HttpStatus.valueOf(rse.getStatusCode().value());
            if (rse.getReason() != null) message = rse.getReason();
        } else {                                                 // otherwise guess from the message
            String m = message.toLowerCase();
            if (m.contains("existe déjà") || m.contains("existe deja")) status = HttpStatus.CONFLICT;          // 409
            else if (m.contains("attente de validation") || m.contains("suspendu")) status = HttpStatus.FORBIDDEN; // 403
            else if (m.contains("introuvable")) status = HttpStatus.NOT_FOUND;                                  // 404
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status.value());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
