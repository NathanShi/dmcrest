package org.dmc.services.security;

import javax.inject.Inject;

import org.dmc.services.data.entities.User;
import org.dmc.services.data.entities.UserRoleAssignment;
import org.dmc.services.data.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserPrincipalService implements UserDetailsService {
	
	@Inject
	private UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findFirstByUsername(username);
		if (user == null) {
			throw new UsernameNotFoundException("Could not find user with username: " + username);
		}
		
		UserPrincipal principal = new UserPrincipal(user.getId(), user.getUsername());
		
		for (UserRoleAssignment roleAssignment : user.getRoles()) {
			String role = roleAssignment.getRole().getRole();
			principal.addRole(roleAssignment.getOrganizationId(), role);
			
			if (!principal.hasAuthority(role)) {
				principal.addAuthorities(PermissionEvaluationHelper.getInheritedRolesForRole(role));
			}
		}
		
		return principal;
	}

}
