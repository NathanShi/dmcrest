package org.dmc.services.organization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.dmc.services.DMDIIDocumentService;
import org.dmc.services.DMDIIProjectService;
import org.dmc.services.OrganizationUserService;
import org.dmc.services.ResourceGroupService;
import org.dmc.services.data.entities.AreaOfExpertise;
import org.dmc.services.data.entities.DocumentParentType;
import org.dmc.services.data.entities.Organization;
import org.dmc.services.data.entities.QDMDIIMember;
import org.dmc.services.data.entities.QOrganization;
import org.dmc.services.data.entities.User;
import org.dmc.services.data.mappers.Mapper;
import org.dmc.services.data.mappers.MapperFactory;
import org.dmc.services.data.models.DMDIIProjectModel;
import org.dmc.services.data.models.OrganizationModel;
import org.dmc.services.data.repositories.AreaOfExpertiseRepository;
import org.dmc.services.data.repositories.OrganizationRepository;
import org.dmc.services.data.repositories.UserRepository;
import org.dmc.services.exceptions.InvalidFilterParameterException;
import org.dmc.services.roleassignment.UserRoleAssignmentService;
import org.dmc.services.security.UserPrincipal;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.mysema.query.jpa.JPASubQuery;
import com.mysema.query.types.ExpressionUtils;
import com.mysema.query.types.Predicate;
import com.mysema.query.types.query.ListSubQuery;

@Service
public class OrganizationService {

	@Inject
	private OrganizationRepository organizationRepository;

	@Inject
	private AreaOfExpertiseRepository areaOfExpertiseRepository;

	@Inject
	private MapperFactory mapperFactory;

	@Inject
	private UserRepository userRepository;

	@Inject
	private OrganizationUserService organizationUserService;

	@Inject
	private UserRoleAssignmentService userRoleAssignmentService;
	
	@Inject
	private DMDIIProjectService dmdiiProjectService;
	
	@Inject
	private DMDIIDocumentService dmdiiDocumentService;
	
	@Inject
	private ResourceGroupService resourceGroupService;

	public List<OrganizationModel> filter(Map filterParams, Integer pageNumber, Integer pageSize) throws InvalidFilterParameterException {
		Mapper<Organization, OrganizationModel> mapper = mapperFactory.mapperFor(Organization.class, OrganizationModel.class);
		Predicate where = ExpressionUtils.allOf(getFilterExpressions(filterParams));
		return mapper.mapToModel(organizationRepository.findAll(where, new PageRequest(pageNumber, pageSize)).getContent());
	}

	@Transactional
	public OrganizationModel save(OrganizationModel organizationModel) {
		Mapper<Organization, OrganizationModel> mapper = mapperFactory.mapperFor(Organization.class, OrganizationModel.class);

		Organization organizationEntity = mapper.mapToEntity(organizationModel);

		// Check each of the tags to see if they're new or not. If new, they're saved separately through their own repository.
		List<AreaOfExpertise> aTags = organizationEntity.getAreasOfExpertise();
		List<AreaOfExpertise> dTags = organizationEntity.getDesiredAreasOfExpertise();

		for(int i = 0; i < aTags.size(); i++) {
			if(aTags.get(i).getId() == null) {
				aTags.set(i, areaOfExpertiseRepository.save(aTags.get(i)));
			}
		}

		for(int i = 0; i < dTags.size(); i++) {
			if(dTags.get(i).getId() == null) {
				dTags.set(i, areaOfExpertiseRepository.save(dTags.get(i)));
			}
		}

		organizationEntity.setAreasOfExpertise(aTags);
		organizationEntity.setDesiredAreasOfExpertise(dTags);

		// if organization is being created, save it and set the user saving as company admin
		if(organizationEntity.getId() == null) {
			organizationEntity = organizationRepository.save(organizationEntity);
			User userEntity = userRepository.findOne(((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId());
			organizationUserService.createVerifiedOrganizationUser(userEntity, organizationEntity);
			userRoleAssignmentService.assignInitialCompanyAdmin(userEntity, organizationEntity);
			
			//add ResourceGroups for new Organization
			resourceGroupService.newCreate(DocumentParentType.ORGANIZATION, organizationEntity.getId());
			
			//add user to admin resource group
			resourceGroupService.addResourceGroup(userEntity, DocumentParentType.ORGANIZATION, organizationEntity.getId(), 2);
			
		} else {
			organizationEntity = organizationRepository.save(organizationEntity);
		}

		return mapper.mapToModel(organizationEntity);

	}

	public Organization save(Organization organization) {
		return organizationRepository.save(organization);
	}

	public OrganizationModel findById(Integer id) {
		Mapper<Organization, OrganizationModel> mapper = mapperFactory.mapperFor(Organization.class, OrganizationModel.class);
		return mapper.mapToModel(organizationRepository.findOne(id));
	}

	public List<OrganizationModel> findAll() {
		Mapper<Organization, OrganizationModel> mapper = mapperFactory.mapperFor(Organization.class, OrganizationModel.class);
		return mapper.mapToModel(organizationRepository.findAll());
	}

	public List<OrganizationModel> findNonDmdiiMembers() {
		ListSubQuery<Integer> subQuery = new JPASubQuery().from(QDMDIIMember.dMDIIMember).list(QDMDIIMember.dMDIIMember.organization().id);
		Predicate predicate = QOrganization.organization.id.notIn(subQuery);

		Mapper<Organization, OrganizationModel> mapper = mapperFactory.mapperFor(Organization.class, OrganizationModel.class);
		return mapper.mapToModel(organizationRepository.findAll(predicate));
	}

	private Collection<Predicate> getFilterExpressions(Map<String, String> filterParams) throws InvalidFilterParameterException {
		Collection<Predicate> expressions = new ArrayList<Predicate>();
		expressions.addAll(tagFilter(filterParams.get("expertiseTags"), "expertiseTags"));
		expressions.addAll(tagFilter(filterParams.get("desiredExpertiseTags"), "desiredExpertiseTags"));
		return expressions;
	}

	private Collection<Predicate> tagFilter(String tagIds, String tagType) throws InvalidFilterParameterException {
		if(tagIds == null)
			return new ArrayList<Predicate>();

		Collection<Predicate> returnValue = new ArrayList<Predicate>();
		String[] tags = tagIds.split(",");
		Integer tagIdInt = null;

		for(String tag: tags) {
			try{
				tagIdInt = Integer.parseInt(tag);
			} catch(NumberFormatException e) {
				throw new InvalidFilterParameterException(tagType, Integer.class);
			}

			if(tagType.equals("expertiseTags")) {
				returnValue.add(QOrganization.organization.areasOfExpertise.any().id.eq(tagIdInt));
			} else if (tagType.equals("desiredExpertiseTags")) {
				returnValue.add(QOrganization.organization.desiredAreasOfExpertise.any().id.eq(tagIdInt));
			}
		}
		return returnValue;
	}
	
	@Transactional
	public void delete(Integer organizationId) {
		List<DMDIIProjectModel> projectModels = dmdiiProjectService.findDmdiiProjectsByPrimeOrganizationId(organizationId, 0, Integer.MAX_VALUE);
		projectModels.stream().map(n -> n.getId()).forEach(dmdiiDocumentService::deleteDMDIIDocumentsByDMDIIProjectId);
		organizationRepository.delete(organizationId);
		
		//remove associated resource groups
		resourceGroupService.removeAll(DocumentParentType.ORGANIZATION, organizationId);
		
	}
}
