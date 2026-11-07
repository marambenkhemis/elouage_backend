package com.elouage.auth;

import com.elouage.utilisateur.Louagiste;
import com.elouage.utilisateur.Passager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/inscription/passager")
    public Map<String, Object> inscrirePassager(@RequestBody Passager passager) {
        return authService.inscrirePassager(passager);
    }

    @PostMapping("/inscription/louagiste")
    public Map<String, Object> inscrireLouagiste(@RequestBody Louagiste louagiste) {
        return authService.inscrireLouagiste(louagiste);
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        return authService.login(body);
    }
}
