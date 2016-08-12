package org.dmc.services.users;

import org.dmc.services.DMCServiceException;
import org.dmc.services.ServiceLogger;
import org.dmc.services.UserService;
import org.dmc.services.data.models.UserModel;
import org.dmc.services.profile.Profile;
import org.dmc.services.profile.ProfileDao;
import org.dmc.services.projects.ProjectMember;
import org.dmc.services.projects.ProjectMemberDao;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
public class AssignUserController {
	
	private final String logTag = AssignUserController.class.getName();
	private ProjectMemberDao projectMemberDao = new ProjectMemberDao();

	@Inject
	private UserService userService;

	/**
	 * Handle GET request for assign users
	 * @param userEPPN
	 * @param projectId
	 * @return List<AssignUser>
	 * @throws Exception
	 */
	@RequestMapping(value = "/assign_users", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getAssignUsers(@RequestHeader(value = "AJP_eppn", defaultValue = "testUser") String userEPPN,
											@RequestHeader(value = "projectId", defaultValue = "-1") Integer projectId) throws Exception {
		
		ServiceLogger.log(logTag, "In getAssignUsers: as user " + userEPPN);
		ResponseEntity<?> responseEntity;

		try {
			final List<UserModel> users = userService.findAllWhereDmdiiMemberExpiryDateIsAfterNow();
			final ArrayList<AssignUser> assignUser = new ArrayList<AssignUser>();
			users.stream().forEach(u -> assignUser.add(new AssignUser(u.getId(), u.getRealname())));
			responseEntity = new ResponseEntity<ArrayList<AssignUser>>(assignUser, HttpStatus.OK);
		} catch (DMCServiceException e) {
			ServiceLogger.logException(logTag, e);
			responseEntity = new ResponseEntity<String>(e.getMessage(), e.getHttpStatusCode());
		}
		return responseEntity;
	}

	/**
	 * Handle GET request for
	 * @param userEPPN
	 * @param projectId
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/assign_users/{projectId}", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getAssignUsersForProject(@RequestHeader(value = "AJP_eppn", defaultValue = "testUser") String userEPPN,
													  @PathVariable(value = "projectId") Integer projectId) throws Exception {
		
		ServiceLogger.log(logTag, "In getAssignUsers: as user " + userEPPN);
		
		try {
			final ArrayList<ProjectMember> members =  projectMemberDao.getMembersForProject(projectId.toString(), userEPPN);
			final ArrayList<AssignUser> assignUser = new ArrayList<AssignUser>();
			final ProfileDao profileDao = new ProfileDao();
			
			final Iterator<ProjectMember> iter = members.iterator();
			while(iter.hasNext()) {
				final ProjectMember projectMember = iter.next();
				final Profile userProfile = profileDao.getProfile(Integer.parseInt(projectMember.getProfileId()));
				final AssignUser user = new AssignUser(userProfile.getId(), userProfile.getDisplayName());
				assignUser.add(user);
			}
			
			return new ResponseEntity<ArrayList<AssignUser>>(assignUser, HttpStatus.OK);
			
		} catch (DMCServiceException e) {
			ServiceLogger.logException(logTag, e);
			return new ResponseEntity<String>(e.getMessage(), e.getHttpStatusCode());
		}
	}
}
