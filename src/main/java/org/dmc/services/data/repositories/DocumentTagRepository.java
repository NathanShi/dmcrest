package org.dmc.services.data.repositories;

import java.util.List;

import org.dmc.services.data.entities.DocumentTag;

public interface DocumentTagRepository extends BaseRepository<DocumentTag, Integer> {

	DocumentTag findByTagName(String tagName);

}
