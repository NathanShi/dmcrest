package org.dmc.services.resources;


import javax.inject.Inject;
import java.util.List;
import org.dmc.services.data.entities.ResourceBay;
import org.dmc.services.data.entities.ResourceMachine;
import org.dmc.services.data.mappers.Mapper;
import org.dmc.services.data.mappers.MapperFactory;
import org.dmc.services.data.models.ResourceBayModel;
import org.dmc.services.data.models.ResourceMachineModel;
import org.dmc.services.data.repositories.ResourceMachineRepository;
import org.dmc.services.data.repositories.ResourceBayRepository;
import org.springframework.stereotype.Service;


//A service is where you actually execute the mapping and repository query
@Service
public class ResourceMachineService {


	@Inject
	private ResourceMachineRepository resourceMachineRepository;

	//There is no need to inject your custom Mappers into each service.
	//Inheritance takes care of this for your and will map correctly based on your data types
	@Inject
	private MapperFactory mapperFactory;


	@Inject
	private ResourceBayService resourceBayService;



	//Gets a list specific machines in a bay
	public List<ResourceMachineModel> getAllMachines(Integer BayId) {
		//Declare mappers in order to convert from entity to model
		Mapper<ResourceMachine, ResourceMachineModel> mapper = mapperFactory.mapperFor(ResourceMachine.class, ResourceMachineModel.class);
		return mapper.mapToModel(resourceMachineRepository.findBybay_id(BayId));
	}

	//This is a more complex service because of the bidirectional mapping. Once again, a bay has many machines. Refer to comments to understand implementation
	//creates a machine in a bay
	public ResourceMachineModel createMachine(ResourceMachineModel machineModel) {

		//Create mappers
		Mapper<ResourceMachine, ResourceMachineModel> machineMapper = mapperFactory.mapperFor(ResourceMachine.class, ResourceMachineModel.class);
		Mapper<ResourceBay, ResourceBayModel> bayMapper = mapperFactory.mapperFor(ResourceBay.class, ResourceBayModel.class);

		//Convert to machine to entity
		ResourceMachine machineEntity = machineMapper.mapToEntity(machineModel);

		//Get the associated bay
		ResourceBay bayEntity = bayMapper.mapToEntity(resourceBayService.get((machineModel.getBay()).getId()));

		//Add bay entity
		machineEntity.setBay(bayEntity);

		//save changes
		machineEntity = resourceMachineRepository.save(machineEntity);

		//Return the created machine
		return machineMapper.mapToModel(machineEntity);
	}


	//deletes all machine from a bay
	public Integer removeAllMachines(Integer BayId) {
		List<ResourceMachine> entity = resourceMachineRepository.findBybay_id(BayId);

		//Delete in batch method is extended from the JPA repo
		resourceMachineRepository.deleteInBatch(entity);
		return BayId;
	}

	//deletes an individual bay machine
	public ResourceMachineModel removeMachine(Integer bayId, Integer machineId) {
		Mapper<ResourceMachine, ResourceMachineModel> mapper = mapperFactory.mapperFor(ResourceMachine.class, ResourceMachineModel.class);

		ResourceMachine entity = resourceMachineRepository.findOne(machineId);
		//Delete method is extended from JPA repo
		resourceMachineRepository.delete(entity);
		return mapper.mapToModel(entity);
	}


}
