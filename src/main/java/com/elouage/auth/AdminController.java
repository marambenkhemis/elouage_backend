package com.elouage.auth;

import com.elouage.document.Document;
import com.elouage.document.DocumentService;
import com.elouage.utilisateur.StatutCompte;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    @Autowired
    private AdminService adminService;
    @Autowired
    private DocumentService documentService;

    @GetMapping("/louagistes")
    public List<Map<String, Object>> lister(@RequestParam(required = false) StatutCompte statut) {
        return adminService.listerLouagistes(statut);
    }

    @PutMapping("/louagistes/{id}/validation")
    public Map<String, Object> valider(@PathVariable Long id, @RequestBody Map<String, List<Long>> body) {
        return adminService.validerCompte(id, body);
    }

    // GET /api/admin/louagistes/3/documents
    @GetMapping("/louagistes/{id}/documents")
    public List<Document> documents(@PathVariable Long id) {
        return documentService.lister(id);
    }

    // GET /api/admin/documents/12/fichier   -> streams the file so the admin can look at it
    @GetMapping("/documents/{id}/fichier")
    public ResponseEntity<Resource> fichier(@PathVariable Long id) {
        Document d = documentService.getDocument(id);
        MediaType type = switch (d.getFormat()) {
            case PDF -> MediaType.APPLICATION_PDF;
            case PNG -> MediaType.IMAGE_PNG;
            case JPG -> MediaType.IMAGE_JPEG;
        };
        return ResponseEntity.ok()
                .contentType(type)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(documentService.charger(d));
    }
}
