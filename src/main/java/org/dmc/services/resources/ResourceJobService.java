package org.dmc.services.resources;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.dmc.services.data.entities.ResourceCourse;
import org.dmc.services.data.entities.ResourceJob;
import org.dmc.services.data.mappers.Mapper;
import org.dmc.services.data.mappers.MapperFactory;
import org.dmc.services.data.models.ResourceCourseModel;
import org.dmc.services.data.models.ResourceJobModel;
import org.dmc.services.data.repositories.ResourceJobRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;


@Service
public class ResourceJobService {

	@Inject
	private ResourceJobRepository resourceJobRepository;
	
	@Inject
	private MapperFactory mapperFactory;


	//Gets all job
	public List<ResourceJobModel> getAll() {
		Mapper<ResourceJob, ResourceJobModel> mapper = mapperFactory.mapperFor(ResourceJob.class, ResourceJobModel.class);
		return mapper.mapToModel(resourceJobRepository.findAll());
	}

	//Gets a specific job
	public ResourceJobModel get(Integer id) {
		Mapper<ResourceJob, ResourceJobModel> mapper = mapperFactory.mapperFor(ResourceJob.class, ResourceJobModel.class);
		return mapper.mapToModel(resourceJobRepository.findOne(id));
	}
	
	
	//create job
	public ResourceJobModel create(ResourceJobModel assessment) {
		Mapper<ResourceJob, ResourceJobModel> mapper = mapperFactory.mapperFor(ResourceJob.class, ResourceJobModel.class);
		ResourceJob entity = mapper.mapToEntity(assessment);
		entity = resourceJobRepository.save(entity);
		return mapper.mapToModel(entity);
	}
	

	//deletes an job
		public ResourceJobModel remove(Integer id) {
			Mapper<ResourceJob, ResourceJobModel> mapper = mapperFactory.mapperFor(ResourceJob.class, ResourceJobModel.class);
			ResourceJob entity = resourceJobRepository.findOne(id);
			resourceJobRepository.delete(entity);
			return mapper.mapToModel(entity);
		}

	
	
	
	

}
