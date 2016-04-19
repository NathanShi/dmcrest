package org.dmc.services.accounts;

import org.junit.Test;
import static org.junit.Assert.*;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.SQLException;
import org.json.JSONObject;

import static com.jayway.restassured.RestAssured.*;
import static com.jayway.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import org.dmc.services.BaseIT;
import org.dmc.services.utility.TestUserUtil;
import org.dmc.services.accounts.UserAccount;
import org.dmc.services.accounts.UserAccountServer;
import org.dmc.services.users.User;
import org.dmc.services.users.UserDao;

public class AccountsIT extends BaseIT {
	private final String logTag = AccountsIT.class.getName();
	private String knownUserEPPN = "fforgeadmin";
	private String knownUserID = "102";
	private String notificationSettingID = "1";
	private String serverID = "2";

	/**
	 * Tests for <code> get /accounts/{accountID} </code>
	 **/

	@Test
	public void testAccountGet_UnknownUser() {
		String unknownUserEPPN = TestUserUtil.uniqueUserEPPN();
		String unknownUserID = Integer.toString(Integer.MAX_VALUE);

		given().header("AJP_eppn", unknownUserEPPN).expect().statusCode(401).when().get("/accounts/" + unknownUserID);

		// not json is returned on 401. If it is, then the check below should be
		// perfromed.
		// .then().
		// body(matchesJsonSchemaInClasspath("Schemas/userAccountSchema.json"));
		// check resulting json
	}

	@Test
	public void testAccountGet_KnownUser() {
		given().header("AJP_eppn", knownUserEPPN).expect().statusCode(200).when().get("/accounts/" + knownUserID).then()
				.body(matchesJsonSchemaInClasspath("Schemas/userAccountSchema.json"));

		// check results
		// assertTrue(false);
	}

	/**
	 * <code> testAccountGet_MismatchedUserEPPNAndUserId </code> Tests for
	 * mismatched user EPPN (from header) and user ID (from path)
	 **/
	@Test
	public void testAccountGet_MismatchedUserEPPNAndUserId() {
		String unknownUserID = Integer.toString(Integer.MAX_VALUE);

		given().header("AJP_eppn", knownUserEPPN).expect().statusCode(401).when().get("/accounts/" + unknownUserID);

		// not json is returned on 401. If it is, then the check below should be
		// perfromed.
		// .then().
		// body(matchesJsonSchemaInClasspath("Schemas/userAccountSchema.json"));
		// check resulting json
	}

	/**
	 * Tests for <code> patch /accounts/{accountID} </code>
	 **/

	@Test
	public void testAccountUpdate_MismatchedUserId() {
		String unique = TestUserUtil.generateTime();
		User user = getUser(unique);
		JSONObject userAccountJson = getUserAccountJson(unique, user.getAccountId());

		// perfrom update
		userAccountJson.put("firstName", "NEWGivenName" + unique);
		userAccountJson.put("lastName", "NEWSurname" + unique);
		userAccountJson.put("displayName", "NEWDisplayName" + unique);

		given().header("Content-type", "application/json").header("AJP_eppn", "userEPPN" + unique)
				.body(userAccountJson.toString()).expect().statusCode(401). // Unauthorized
																			// access
				when().patch("/accounts/1" + userAccountJson.getString("id"));

		// check results
	}

	@Test
	public void testAccountUpdateOfNewlyCreatedUser() {
		String unique = TestUserUtil.generateTime();
		User user = getUser(unique);
		JSONObject userAccountJson = getUserAccountJson(unique, user.getAccountId());

		// perfrom update
		userAccountJson.put("firstName", "NEWGivenName" + unique);
		userAccountJson.put("lastName", "NEWSurname" + unique);
		userAccountJson.put("displayName", "NEWDisplayName" + unique);

		// ServiceLogger.log(logTag, "testAccountUpdateOfNewlyCreatedUser,
		// userAccountJson: " + userAccountJson.toString());

		UserAccount patchedUserAccount = given().header("Content-type", "application/json")
				.header("AJP_eppn", "userEPPN" + unique).body(userAccountJson.toString()).expect().statusCode(200)
				.when().patch("/accounts/" + userAccountJson.getString("id")).as(UserAccount.class);

		// check results
		assertTrue(
				"New and patched user's id are equal " + userAccountJson.getString("id") + " "
						+ patchedUserAccount.getId(),
				userAccountJson.getString("id").equals(patchedUserAccount.getId()));

		assertFalse("Orginal and patched user's display names are different",
				user.getDisplayName().equals(patchedUserAccount.getDisplayName()));

		assertTrue("New and patched user's fistname are different",
				userAccountJson.getString("firstName").equals(patchedUserAccount.getFirstName()));

		assertTrue("New and patched user's lastname are different",
				userAccountJson.getString("lastName").equals(patchedUserAccount.getLastName()));

		assertTrue("New and patched user's display name are different",
				userAccountJson.getString("displayName").equals(patchedUserAccount.getDisplayName()));

	}

	private User getUser(String uniqueString) {
		User user = given().header("Content-type", "text/plain").header("AJP_eppn", "userEPPN" + uniqueString)
				.header("AJP_givenName", "userGivenName" + uniqueString).header("AJP_sn", "userSurname" + uniqueString)
				.header("AJP_displayName", "userDisplayName" + uniqueString)
				.header("AJP_mail", "userEmail" + uniqueString).expect().statusCode(200).when().get("/user")
				.as(User.class);

		return user;
	}

	private JSONObject getUserAccountJson(String uniqueString, int userAccountId) {

		String userAccountJsonString = given().header("AJP_eppn", "userEPPN" + uniqueString).expect().statusCode(200)
				.when().get("/accounts/" + userAccountId).body().asString();

		JSONObject userAccountJson = new JSONObject(userAccountJsonString);

		return userAccountJson;
	}

	private UserAccount getUserAccount(String uniqueString, int userAccountId) {
		UserAccount userAccount = given().header("AJP_eppn", "userEPPN" + uniqueString).expect().statusCode(200).when()
				.get("/accounts/" + userAccountId).as(UserAccount.class);

		return userAccount;
	}

	/**
	 * Tests for <code> get /accounts/{accountID}/account_servers </code>
	 **/
	@Test
	public void testAccountGet_AccountServers() {
		given().header("AJP_eppn", knownUserEPPN).expect().statusCode(HttpStatus.NOT_IMPLEMENTED.value()).when()
				.get("/accounts/" + knownUserID + "/account_servers");
	}

	/**
	 * Tests for
	 * <code> get /accounts/{accountID}/account-notification-settings </code>
	 **/

	@Test
	public void testAccount_account_Notification_Settings() {
		given().header("AJP_eppn", knownUserEPPN).expect().statusCode(HttpStatus.NOT_IMPLEMENTED.value()).when()
				.get("/accounts/" + knownUserID + "/account-notification-settings");
	}

	/**
	 * Tests for
	 * <code> GET /accounts/{accountID}/account-notification-settings/NotificationSettingID </code>
	 **/

	@Test
	public void testAccountNotificationSettingGet_NotificationSettingID() {
		given().header("AJP_eppn", knownUserEPPN).expect().statusCode(HttpStatus.NOT_IMPLEMENTED.value()).when()
				.get("/account-notification-settings/" + notificationSettingID);

	}
	
	
	/**
	 * Tests for
	 * <code> PATCH /accounts/{accountID}/account-notification-settings/NotificationSettingID </code>
	 **/

	@Test
	public void testAccountNotificationSettingPatch_NotificationSettingID() {
		AccountNotificationSetting obj = new AccountNotificationSetting();
		ObjectMapper mapper = new ObjectMapper();
		String postedNotificationSetting = null;
		try {
			postedNotificationSetting = mapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		given().
		header("Content-type", "application/json").
		header("AJP_eppn", knownUserEPPN).
		body(postedNotificationSetting).
		expect().statusCode(HttpStatus.NOT_IMPLEMENTED.value()).when()
				.patch("/account-notification-settings/" + notificationSettingID);

	}

	/**
	 * Tests for <code> get /account-notification-categories </code>
	 **/

	@Test
	public void testAccount_accountNotificationCategories() {
		given().header("AJP_eppn", knownUserEPPN).expect().statusCode(HttpStatus.NOT_IMPLEMENTED.value()).when()
				.get("/account-notification-categories");
	}

	
	/**
	 * Tests for
	 * <code> post /account-servers </code>
	 **/
	
	@Test
	public void testAccountPost_NewServer() {
		String newKnownUser = TestUserUtil.createNewUser();
		String uniqueID = TestUserUtil.uniqueID();
		String userAccountServerString = null;
		UserAccountServer userAccountServer = new UserAccountServer();
		ObjectMapper mapper = new ObjectMapper();
		int user_id_lookedup = -1;

		try{
            user_id_lookedup = UserDao.getUserID(newKnownUser);
        } catch (SQLException e) {
			assertTrue("Error looking up new user", false);
        }
				
		// setup userAccountServer
		userAccountServer.setAccountId(Integer.toString(user_id_lookedup));
		userAccountServer.setName("ServerName" + uniqueID);
		userAccountServer.setIp("ServerURL" + uniqueID);
		userAccountServer.setStatus("ServerStatus" + uniqueID);
		
		try {
			userAccountServerString = mapper.writeValueAsString(userAccountServer);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		
		UserAccountServer returnedUserAccountServer =
		given().
			header("Content-type", "application/json").
			header("AJP_eppn", newKnownUser).
			body(userAccountServerString).
		expect().
			statusCode(HttpStatus.OK.value()).  //ToDo should return 201 not 200
		when().
			post("/account_servers").
			as(UserAccountServer.class);
		
		// check that the orginal UserAccountServer object does not have an id
		assertTrue("Orginal server id is not null",
				   userAccountServer.getId() == null);

		// set if of orginal to id of returned
		userAccountServer.setId(returnedUserAccountServer.getId());
		
		// check returned and orginal UserAccountServer object is equal
		assertTrue("Orginal and returned UserAccountServer objects are not equal",
				   returnedUserAccountServer.equals(userAccountServer));
	}
	
	/**
	 * Tests for
	 * <code> get /account-servers/{serverID} </code>
	 **/

	@Test
	public void testAccountGet_ServerID() {
		String newKnownUser = TestUserUtil.createNewUser();
		String uniqueID = TestUserUtil.uniqueID();
		String userAccountServerString = null;
		UserAccountServer userAccountServer = new UserAccountServer();
		ObjectMapper mapper = new ObjectMapper();
		int user_id_lookedup = -1;
		
		try{
            user_id_lookedup = UserDao.getUserID(newKnownUser);
        } catch (SQLException e) {
			assertTrue("Error looking up new user", false);
        }
		
		// setup userAccountServer
		userAccountServer.setAccountId(Integer.toString(user_id_lookedup));
		userAccountServer.setName("ServerName" + uniqueID);
		userAccountServer.setIp("ServerURL" + uniqueID);
//		userAccountServer.setStatus("ServerStatus" + uniqueID);
		
		try {
			userAccountServerString = mapper.writeValueAsString(userAccountServer);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		
		UserAccountServer returnedUserAccountServer =
		given().
		header("Content-type", "application/json").
		header("AJP_eppn", newKnownUser).
		body(userAccountServerString).
		expect().
		statusCode(HttpStatus.OK.value()).  //ToDo should return 201 not 200
		when().
		post("/account_servers").
		as(UserAccountServer.class);
		
		
		UserAccountServer getReturnedUserAccountServer =
		given().
			header("Content-type", "application/json").
			header("AJP_eppn", newKnownUser).
		expect().
			statusCode(HttpStatus.OK.value()).  //ToDo should return 201 not 200
		when().
			get("/account_servers/" + returnedUserAccountServer.getId()).
			as(UserAccountServer.class);
		
		System.out.println(returnedUserAccountServer.toString());
		System.out.println(getReturnedUserAccountServer.toString());
		
		
		// check returned and orginal UserAccountServer object is equal
		assertTrue("Orginal and returned UserAccountServer objects are not equal",
				   returnedUserAccountServer.equals(getReturnedUserAccountServer));

	}
	
	
	/**
	 * Tests for
	 * <code> DELETE /account-servers/{serverID} </code>
	 **/

	@Test
	public void testAccountDelete_ServerID() {
		given().header("AJP_eppn", knownUserEPPN).expect().statusCode(HttpStatus.NOT_IMPLEMENTED.value()).when()
				.delete("/account_servers/" + serverID);
	}
	
	/**
	 * Tests for
	 * <code> PATCH /account-servers/{serverID} </code>
	 **/

	@Test
	public void testAccountPatch_ServerID() {
		UserAccountServer obj = new UserAccountServer();
		ObjectMapper mapper = new ObjectMapper();
		String patchedAccountServer = null;
		
		try {
			patchedAccountServer = mapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		given().
		header("Content-type", "application/json").
		header("AJP_eppn", knownUserEPPN).
		body(patchedAccountServer).expect().statusCode(HttpStatus.NOT_IMPLEMENTED.value()).when()
				.patch("/account_servers/" + serverID);
	}

	/**
	 * Tests for <code> get /accounts/{accountID}/favorite_products </code>
	 **/

	@Test
	public void testAccount_FavoriteProducts() {
		given().header("AJP_eppn", knownUserEPPN).expect().statusCode(HttpStatus.NOT_IMPLEMENTED.value()).when()
				.get("/accounts/" + knownUserID + "/favorite_products");
	}

	/**
	 * Tests for <code> get /accounts/{accountID}/following_companies </code>
	 **/

	@Test
	public void testAccount_FollowingCompanies() {
		given().header("AJP_eppn", knownUserEPPN).expect().statusCode(HttpStatus.NOT_IMPLEMENTED.value()).when()
				.get("/accounts/" + knownUserID + "/following_companies");
	}


	/**
	 * Tests for <code> get /accounts/{accountID}/following_members </code>
	 **/

	@Test
	public void testAccount_FollowingMembers() {
		given().header("AJP_eppn", knownUserEPPN).expect().statusCode(HttpStatus.NOT_IMPLEMENTED.value()).when()
				.get("/accounts/" + knownUserID + "/following_members");
	}
	
	
	/**
	 * Tests for <code> get /accounts/{accountID}/follow_discussions </code>
	 **/

	@Test
	public void testAccount_FollowDiscussions() {
		given().header("AJP_eppn", knownUserEPPN).expect().statusCode(400).when()
				.get("/accounts/" + knownUserID + "/follow_discussions");
	}

}