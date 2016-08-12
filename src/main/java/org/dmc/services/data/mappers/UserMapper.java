package org.dmc.services.data.mappers;

import org.dmc.services.data.entities.OrganizationUser;
import org.dmc.services.data.entities.User;
import org.dmc.services.data.entities.UserContactInfo;
import org.dmc.services.data.entities.UserRoleAssignment;
import org.dmc.services.data.models.UserContactInfoModel;
import org.dmc.services.data.models.UserModel;
import org.dmc.services.data.repositories.OrganizationRepository;
import org.dmc.services.data.repositories.OrganizationUserRepository;
import org.dmc.services.security.SecurityRoles;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

@Component
public class UserMapper extends AbstractMapper<User, UserModel> {

	@Inject
	private OrganizationRepository organizationRepository;

	@Inject
	private OrganizationUserRepository organizationUserRepository;

	@Override
	public User mapToEntity(UserModel model) {
		if (model == null) return null;

		Mapper<UserContactInfo, UserContactInfoModel> contactInfoMapper = mapperFactory.mapperFor(UserContactInfo.class, UserContactInfoModel.class);

		User entity = copyProperties(model, new User());
		entity.setUserContactInfo(contactInfoMapper.mapToEntity(model.getUserContactInfo()));

		OrganizationUser orgUserEntity = organizationUserRepository.findByUserIdAndOrganizationId(entity.getId(), model.getOrganization());
		if(orgUserEntity == null) {
			orgUserEntity = new OrganizationUser();
			orgUserEntity.setOrganization(organizationRepository.findOne(model.getOrganization()));
		}

		entity.setOrganizationUser(orgUserEntity);
		entity.getOrganizationUser().setUser(entity);

		return entity;
	}

	@Override
	public UserModel mapToModel(User entity) {
		if (entity == null) return null;

		Mapper<UserContactInfo, UserContactInfoModel> contactInfoMapper = mapperFactory.mapperFor(UserContactInfo.class, UserContactInfoModel.class);

		UserModel model = copyProperties(entity, new UserModel());
		model.setDMDIIMember(entity.getRoles().stream().anyMatch(this::orgIsDMDIIMember));
		model.setUserContactInfo(contactInfoMapper.mapToModel(entity.getUserContactInfo()));
		model.setTermsConditions(entity.getTermsAndCondition()!=null);

		Map<Integer, String> roles = new HashMap<Integer, String>();
		for (UserRoleAssignment assignment : entity.getRoles()) {
			if (assignment.getRole().getRole().equals(SecurityRoles.SUPERADMIN)) {
				roles.put(0, SecurityRoles.SUPERADMIN);
				model.setDMDIIMember(true);
			} else {
				roles.put(assignment.getOrganization().getId(), assignment.getRole().getRole());
			}
		}
		model.setRoles(roles);
		model.setOrganization( (entity.getOrganizationUser() == null ) ? null : entity.getOrganizationUser().getOrganization().getId());

		return model;
	}

	private boolean orgIsDMDIIMember(UserRoleAssignment roleAssignment) {
		boolean isMember = false;

		if (roleAssignment.getOrganization() != null) {
			isMember = roleAssignment.getOrganization().getDmdiiMember() != null;
		}

		return isMember;
	}

	@Override
	public Class<User> supportsEntity() {
		return User.class;
	}

	@Override
	public Class<UserModel> supportsModel() {
		return UserModel.class;
	}

}
