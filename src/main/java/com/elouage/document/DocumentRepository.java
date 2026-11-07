package com.elouage.document;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document,Long> {
    List<Document> findByLouagisteId(Long louagisteId);
    Optional<Document> findByLouagisteIdAndType(Long louagisteId, TypeDocument type);
}
