package org.dmc.services;

import javax.inject.Inject;

import org.dmc.services.data.entities.DMDIIProjectEvent;
import org.dmc.services.data.mappers.Mapper;
import org.dmc.services.data.mappers.MapperFactory;
import org.dmc.services.data.models.DMDIIProjectEventModel;
import org.dmc.services.data.repositories.DMDIIProjectEventsRepository;
import org.springframework.stereotype.Service;

@Service
public class DMDIIProjectEventsService {

	@Inject
	private DMDIIProjectEventsRepository dmdiiProjectEventsRepository;
	
	@Inject
	private MapperFactory mapperFactory;
	
	public DMDIIProjectEventModel save(DMDIIProjectEventModel projectEvent) {
		Mapper<DMDIIProjectEvent, DMDIIProjectEventModel> mapper = mapperFactory.mapperFor(DMDIIProjectEvent.class, DMDIIProjectEventModel.class);
		
		DMDIIProjectEvent projectEventEntity = mapper.mapToEntity(projectEvent);
		
		projectEventEntity = dmdiiProjectEventsRepository.save(projectEventEntity);
		
		return mapper.mapToModel(projectEventEntity);
	}
	
	public DMDIIProjectEventModel delete(Integer projectEventId) {
		Mapper<DMDIIProjectEvent, DMDIIProjectEventModel> mapper = mapperFactory.mapperFor(DMDIIProjectEvent.class, DMDIIProjectEventModel.class);
		
		DMDIIProjectEvent eventEntity = dmdiiProjectEventsRepository.findOne(projectEventId);
		
		eventEntity.setIsDeleted(true);
		eventEntity = dmdiiProjectEventsRepository.save(eventEntity);
		
		return mapper.mapToModel(eventEntity);
	}
}
