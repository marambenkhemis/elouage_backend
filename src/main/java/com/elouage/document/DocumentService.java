package com.elouage.document;

import com.elouage.utilisateur.Louagiste;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DocumentService {
    private static final Set<String> EXTENSIONS = Set.of("pdf", "jpg", "jpeg", "png");
    private final DocumentRepository repository;
    private final Path racine;

    public DocumentService(DocumentRepository repository,
                           @Value("${elouage.upload.dir}") String dir) throws IOException {
        this.repository = repository;
        this.racine = Paths.get(dir).toAbsolutePath().normalize();
        Files.createDirectories(racine);
    }

    // validation BEFORE anything is written (called first by AuthService)
    public String verifier(TypeDocument type, MultipartFile f) {
        if (f == null || f.isEmpty())
            throw new RuntimeException("Le document " + type + " est obligatoire.");
        String nom = StringUtils.cleanPath(Objects.requireNonNullElse(f.getOriginalFilename(), ""));
        int point = nom.lastIndexOf('.');
        String ext = point < 0 ? "" : nom.substring(point + 1).toLowerCase();
        if (!EXTENSIONS.contains(ext))
            throw new RuntimeException("Format refusé pour " + type + " : PDF, JPG ou PNG uniquement.");
        try (InputStream in = f.getInputStream()) {            // magic bytes: the extension alone proves nothing
            byte[] b = in.readNBytes(4);
            boolean ok = switch (ext) {
                case "pdf" -> b.length >= 4 && b[0] == '%' && b[1] == 'P' && b[2] == 'D' && b[3] == 'F';
                case "png" -> b.length >= 4 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G';
                default    -> b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF;
            };
            if (!ok) throw new RuntimeException("Le contenu du fichier " + type + " ne correspond pas à son format.");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return ext;
    }

    // writes the file on disk + saves the relative path in the DB
    @Transactional
    public Document sauvegarder(Louagiste l, TypeDocument type, MultipartFile f) {
        String ext = verifier(type, f);
        FormatDocument format = ext.equals("pdf") ? FormatDocument.PDF
                : ext.equals("png") ? FormatDocument.PNG : FormatDocument.JPG;
        // we NEVER use the original name in the path (path traversal): TYPE_timestamp.ext
        String relatif = "documents/" + l.getId() + "/" + type + "_" + System.currentTimeMillis() + "." + ext;
        Path cible = resoudre(relatif);
        try {
            Files.createDirectories(cible.getParent());
            f.transferTo(cible);
        } catch (IOException e) {
            throw new UncheckedIOException("Écriture du document impossible", e);
        }
        repository.findByLouagisteIdAndType(l.getId(), type).ifPresent(ancien -> {   // re-upload replaces
            supprimerFichier(ancien);
            repository.delete(ancien);
        });
        return repository.save(new Document(l, type, format, relatif, f.getOriginalFilename()));
    }

    @Transactional(readOnly = true)
    public List<Document> lister(Long louagisteId) { return repository.findByLouagisteId(louagisteId); }

    @Transactional(readOnly = true)
    public Document getDocument(Long id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Document introuvable : " + id));
    }

    @Transactional(readOnly = true)
    public List<TypeDocument> typesManquants(Long louagisteId) {
        Set<TypeDocument> presents = repository.findByLouagisteId(louagisteId).stream()
                .filter(d -> Files.exists(resoudre(d.getChemin())))
                .map(Document::getType).collect(Collectors.toSet());
        return Arrays.stream(TypeDocument.values()).filter(t -> !presents.contains(t)).toList();
    }

    public Resource charger(Document d) {
        Path p = resoudre(d.getChemin());
        if (!Files.exists(p)) throw new RuntimeException("Fichier introuvable sur le serveur.");
        return new FileSystemResource(p);
    }

    public void supprimerFichier(Document d) {                       // best effort
        try { Files.deleteIfExists(resoudre(d.getChemin())); } catch (IOException ignored) { }
    }

    private Path resoudre(String relatif) {
        Path p = racine.resolve(relatif).normalize();
        if (!p.startsWith(racine)) throw new RuntimeException("Chemin invalide.");
        return p;
    }
}
