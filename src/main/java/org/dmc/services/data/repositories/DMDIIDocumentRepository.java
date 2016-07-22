package org.dmc.services.data.repositories;

import org.dmc.services.data.entities.DMDIIDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DMDIIDocumentRepository extends BaseRepository<DMDIIDocument, Integer> {

	Page<DMDIIDocument> findByDmdiiProjectIdAndIsDeletedFalse(Pageable pageable, Integer dmdiiProjectId);

	Page<DMDIIDocument> findByIsDeletedFalse(Pageable pageable);

}
