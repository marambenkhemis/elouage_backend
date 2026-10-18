package com.elouage.ligne;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/louagistes")
public class LouagisteLigneController {

    private final LouagisteLigneService service;

    public LouagisteLigneController(LouagisteLigneService service) {
        this.service = service;
    }

    // GET http://localhost:8081/api/louagistes/1/lignes
    @GetMapping("/{louagisteId}/lignes")
    public List<LigneResponse> getLignesAutorisees(@PathVariable Long louagisteId) {
        return service.getLignesAutorisees(louagisteId);
    }
}
