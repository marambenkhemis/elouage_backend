package com.elouage;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

// toutes les erreurs de regles metier deviennent un JSON {"message": "..."}
@RestControllerAdvice
public class GestionErreurs {

    // 404 de LouagisteLigneService / changerLigne : on garde leur statut
    @ExceptionHandler(ResponseStatusException.class)

    public ResponseEntity<Map<String, String>> statut(ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode()).body(Map.of("message", String.valueOf(e.getReason())));
    }

    // toutes les autres regles metier (place prise, planning cloture, ligne non autorisee...)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> regle(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", String.valueOf(e.getMessage())));
    }
}
