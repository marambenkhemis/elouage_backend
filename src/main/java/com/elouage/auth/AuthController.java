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

import java.util.EnumMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/inscription/passager")
    public Map<String, Object> inscrirePassager(@RequestBody Passager passager) {
        return authService.inscrirePassager(passager);
    }

    @PostMapping(value = "/inscription/louagiste", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> inscrireLouagiste(
            @RequestParam String nom, @RequestParam String prenom, @RequestParam String email,
            @RequestParam String telephone, @RequestParam String motDePasse,
            @RequestParam String matricule, @RequestParam String modele,
            @RequestParam MultipartFile cin, @RequestParam MultipartFile permis,
            @RequestParam MultipartFile carteGrise, @RequestParam MultipartFile autorisationTransport) {
        Louagiste l = new Louagiste(nom, prenom, email, telephone, motDePasse, matricule, modele);
        Map<TypeDocument, MultipartFile> f = new EnumMap<>(TypeDocument.class);
        f.put(TypeDocument.CIN, cin);
        f.put(TypeDocument.PERMIS, permis);
        f.put(TypeDocument.CARTE_GRISE, carteGrise);
        f.put(TypeDocument.AUTORISATION_TRANSPORT, autorisationTransport);
        return authService.inscrireLouagiste(l, f);
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
}
