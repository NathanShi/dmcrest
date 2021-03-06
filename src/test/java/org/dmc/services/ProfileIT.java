package org.dmc.services;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.dmc.services.utils.SQLUtils.DEFAULT_LIMIT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpStatus.NOT_IMPLEMENTED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.dmc.services.data.dao.user.UserDao;
import org.dmc.services.member.FollowingMember;
import org.dmc.services.profile.Profile;
import org.dmc.services.utility.TestUserUtil;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;

import com.jayway.restassured.response.ValidatableResponse;
import com.jayway.restassured.specification.RequestSpecification;

public class ProfileIT extends BaseIT {

    private static final String LOGTAG = ProfileIT.class.getName();

    private static final String USER = "/user";
    
    private static final String PROFILE_CREATE_RESOURCE = "/profiles";
    private static final String PROFILE_READ_RESOURCE = "/profiles/{id}";
    private static final String PROFILES_READ_RESOURCE = "/profiles";
    private static final String PROFILE_UPDATE_RESOURCE = "/profiles/{id}";
    private static final String PROFILE_DELETE_RESOURCE = "/profiles/{id}/delete";
    private static final String PROFILE_HISTORY = "/profiles/{id}/profile_history";
    private static final String PROFILE_REVIEWS = "/profiles/{id}/profile_reviews";
    private static final String PROFILE_FOLLOWING_MEMBERS = "/profiles/{id}/following_members";
    private static final String FOLLOWING_MEMBERS = "/following_members";

    private final String profileId = "1";
    private String knownEPPN;
    String unique = null;
    Integer createdId = -1;

    // Setup test data
    @Before
    // @Test
    public void testProfileCreate() {
        if (knownEPPN == null) {
            knownEPPN = TestUserUtil.createNewUser();
        }

        // Integer id =
        given().header("Content-type", TEXT_PLAIN_VALUE).header("AJP_eppn", knownEPPN)
                .header("AJP_givenName", "userGivenName" + knownEPPN).header("AJP_sn", "userSurname" + knownEPPN)
                .header("AJP_displayName", knownEPPN).header("AJP_mail", "userEmail" + knownEPPN).expect()
                .statusCode(OK.value()).when().get(USER);
        // then().
        // body(matchesJsonSchemaInClasspath("Schemas/idSchema.json")).
        // extract().path("id");

        Profile json = createFixture("create");
        this.createdId = given().header("Content-type", APPLICATION_JSON_VALUE).header("AJP_eppn", knownEPPN).body(json)
                .expect().statusCode(OK.value()).when().post(PROFILE_CREATE_RESOURCE).then()
                .body(matchesJsonSchemaInClasspath("Schemas/idSchema.json")).extract().path("id");

        // Adding test to get out preSignedURL
        Profile profile = given().header("AJP_eppn", knownEPPN).expect().statusCode(OK.value()).when()
                .get(PROFILE_READ_RESOURCE, this.createdId.toString()).as(Profile.class);

        // Extract
        final String preSignedURL = profile.getImage();
        assertNotNull(preSignedURL);
    }

    @Test
    public void testProfileCreateAndGet() {
        unique = TestUserUtil.generateTime();

        // Integer id =
        given().header("Content-type", TEXT_PLAIN_VALUE).header("AJP_eppn", "userEPPN" + unique)
                .header("AJP_givenName", "userGivenName" + unique).header("AJP_sn", "userSurname" + unique)
                .header("AJP_displayName", unique).header("AJP_mail", "userEmail" + unique).expect().statusCode(OK.value())
                .when().get(USER);
        // then().
        // body(matchesJsonSchemaInClasspath("Schemas/idSchema.json")).
        // extract().path("id");

        Profile orginalProfile = createFixture("create");
        this.createdId = given().header("Content-type", APPLICATION_JSON_VALUE).header("AJP_eppn", "userEPPN" + unique)
                .body(orginalProfile).expect().statusCode(OK.value()).when().post(PROFILE_CREATE_RESOURCE).then()
                .body(matchesJsonSchemaInClasspath("Schemas/idSchema.json")).extract().path("id");

        // Adding test to get out preSignedURL
        Profile responceProfile = given().header("AJP_eppn", "userEPPN" + unique).expect().statusCode(OK.value()).when()
                .get(PROFILE_READ_RESOURCE, this.createdId.toString()).as(Profile.class);

        // Extract
        final String preSignedURL = responceProfile.getImage();
        assertNotNull(preSignedURL);

        ServiceLogger.log(LOGTAG, "orginalProfile " + orginalProfile.toString());
        ServiceLogger.log(LOGTAG, "responceProfile " + responceProfile.toString());

        assertTrue("Display names are not equal",
                orginalProfile.getDisplayName().equals(responceProfile.getDisplayName()));
        assertTrue("Job titles are not equal", orginalProfile.getJobTitle().equals(responceProfile.getJobTitle()));
        assertTrue("Phone numbers are not equal", orginalProfile.getPhone().equals(responceProfile.getPhone()));
        assertTrue("Emails are not equal", orginalProfile.getEmail().equals(responceProfile.getEmail()));
        assertTrue("Locations are not equal", orginalProfile.getLocation().equals(responceProfile.getLocation()));
        assertTrue("Images are not equal", orginalProfile.getImage().equals(responceProfile.getImage()));
        assertTrue("Descriptions are not equal",
                orginalProfile.getDescription().equals(responceProfile.getDescription()));
    }

    // Tests to see if presignedURL Works
    /*
     * @Test public void urlGet() { if(this.preSignedURL != null){ //Create URL
     * object that is needed try{ this.url = new URL(this.preSignedURL);
     * }catch(Exception e){ assert(false); } assert(this.url != null);
     * 
     * //Simple Url check test String host = this.url.getHost(); assertTrue(
     * "S3 Host doesn't match",
     * host.equals("dmc-profiletest.s3.amazonaws.com"));
     * 
     * try{ //Test Remote Connection to AWS to see if resource exists
     * URLConnection urlConnection = url.openConnection();
     * urlConnection.connect(); }catch (Exception e){ assert(false); } } }
     */
    @Test
    public void testProfileGet() {

        if (this.createdId > 0) {
            Integer retrivedId = given().header("AJP_eppn", knownEPPN).expect().statusCode(OK.value()).when()
                    .get(PROFILE_READ_RESOURCE, this.createdId.toString()).then()
                    .body(matchesJsonSchemaInClasspath("Schemas/idSchema.json")).extract().path("id");
            assertTrue("Retrieved Id is not the same as newly created user's id", this.createdId.equals(retrivedId));
            assertTrue("Retrieved Id is " + retrivedId, retrivedId > 0);

        }
    }

    @Test
    public void testProfilePatch() {
        final Profile json = createFixture("update");
        if (this.createdId > 0) {
            final Integer retrievedId = given().header("Content-type", APPLICATION_JSON_VALUE)
                    .header("AJP_eppn", knownEPPN).body(json).expect().statusCode(OK.value()).when()
                    .patch(PROFILE_UPDATE_RESOURCE, this.createdId.toString()).then()
                    .body(matchesJsonSchemaInClasspath("Schemas/idSchema.json")).extract().path("id");

            assertTrue("Retrieved Id is not the same as newly created user's id", this.createdId.equals(retrievedId));
            assertTrue("Retrieved Id is " + retrievedId, retrievedId > 0);
        }
    }

    @Test
    public void testProfilePatchWithNullValues() {
        final Profile json = createFixture("update");
        json.setJobTitle(null);
        json.setPhone(null);
        json.setLocation(null);
        // json.put("image", JSONObject.NULL);
        json.setDescription("");
        json.setSkills(new ArrayList<String>());
        if (this.createdId > 0) {
            final Integer retrivedId = given().header("Content-type", APPLICATION_JSON_VALUE)
                    .header("AJP_eppn", knownEPPN).body(json).expect().statusCode(OK.value()).when()
                    .patch(PROFILE_UPDATE_RESOURCE, this.createdId.toString()).then()
                    .body(matchesJsonSchemaInClasspath("Schemas/idSchema.json")).extract().path("id");

            assertTrue("Retrieved Id is not the same as newly created user's id", this.createdId.equals(retrivedId));
            assertTrue("Retrieved Id is " + retrivedId, retrivedId > 0);
        }
    }

    // Cleanup
    @After
    public void testProfileDelete() {
        if (this.createdId > 0) {
            given().header("AJP_eppn", knownEPPN).expect().statusCode(OK.value()).when()
                    .get(PROFILE_DELETE_RESOURCE, this.createdId.toString()).then()
                    .body(matchesJsonSchemaInClasspath("Schemas/idSchema.json"));
        }
    }

    public Profile createFixture(String type) {

        Profile profile = new Profile();
        final ArrayList<String> skills = new ArrayList<String>();
        skills.add("Skill one " + type);
        skills.add("Skill two " + type);
        skills.add("Skill three " + type);

        profile.setDisplayName("test displayName " + type);
        profile.setCompany("1");
        profile.setJobTitle("test jobTitle " + type);
        profile.setPhone("test phone " + type);
        profile.setEmail("test email " + type);
        profile.setLocation("test location " + type);

        // Adding a hardcoded test image
        profile.setImage("https://s3.amazonaws.com/dmc-uploads2/test/cat.jpeg");
        profile.setDescription("test description " + type);
        profile.setSkills(skills);

        return profile;
    }

    private List<Profile> getProfiles(String userEPPN) {
        return getProfiles(userEPPN, 100, "DESC", "realname", null);
    }

    private List<Profile> getProfiles(String userEPPN, Integer limit) {
        return getProfiles(userEPPN, limit, "DESC", "realname", null);
    }

    private List<Profile> getProfiles(String userEPPN, Integer limit, String order, String sort, List<String> ids) {
        RequestSpecification requestSpecification = given().header("AJP_eppn", userEPPN)
                .header("Content-type", APPLICATION_JSON_VALUE).param("limit", limit).param("order", order)
                .param("sort", sort);

        if (null != ids) {
            ServiceLogger.log(LOGTAG, "ids equal " + ids.toString());
            final Iterator<String> iterStr = ids.iterator();
            while (iterStr.hasNext()) {
                requestSpecification = requestSpecification.param("id", iterStr.next());
            }
        }

        final List<Profile> profiles = Arrays.asList(requestSpecification.expect().statusCode(OK.value()).when()
                .get(PROFILES_READ_RESOURCE).as(Profile[].class));

        return profiles;
    }

    /**
     * test case for GET /profiles default response
     */
    @Test
    public void testProfileGet_Profiles_defaultResponse() {
        final List<Profile> profiles = getProfiles(knownEPPN);

        assertTrue("No profiles returned", profiles.size() > 0);
        assertTrue("Too many profiles returned: " + profiles.size(), profiles.size() <= DEFAULT_LIMIT);

        final Iterator<Profile> profilesIterator = profiles.iterator();
        Profile profile = profilesIterator.next();
        while (profilesIterator.hasNext()) {
            final Profile nextProfile = profilesIterator.next();
            final String profileName = profile.getDisplayName();
            final String nextProfileName = nextProfile.getDisplayName();

            assertTrue("List is not sorted in descending order (by default) : " + profileName + " < " + nextProfileName,
                    profileName.compareTo(nextProfileName) >= 0);
            profile = nextProfile;
        }
    }

    /**
     * test case for GET /profiles ascending order response
     */
    @Test
    public void testProfileGet_Profiles_ascendingOrderResponse() {
        final List<Profile> profiles = getProfiles(knownEPPN, DEFAULT_LIMIT, "ASC", "realname", null);

        assertTrue("No profiles returned", profiles.size() > 0);
        assertTrue("Too many profiles returned: " + profiles.size(), profiles.size() <= DEFAULT_LIMIT);

        final Iterator<Profile> profilesIterator = profiles.iterator();
        Profile profile = profilesIterator.next();
        while (profilesIterator.hasNext()) {
            final Profile nextProfile = profilesIterator.next();
            final String profileName = profile.getDisplayName();
            final String nextProfileName = nextProfile.getDisplayName();

            ServiceLogger.log(LOGTAG, "profileName = " + profileName + " : next : " + nextProfileName);
            assertTrue("List is not sorted in ascending order: " + profileName + " > " + nextProfileName,
                    profileName.compareTo(nextProfileName) <= 0);
            profile = nextProfile;
        }
    }

    /**
     * test case for GET /profiles ascending order for non-default order
     * response
     */
    @Test
    public void testProfileGet_Profiles_ascendingOrderForTitleResponse() {
        final List<Profile> profiles = getProfiles(knownEPPN, DEFAULT_LIMIT, "ASC", "title", null);

        assertTrue("No profiles returned", profiles.size() > 0);
        assertTrue("Too many profiles returned: " + profiles.size(), profiles.size() <= DEFAULT_LIMIT);

        final Iterator<Profile> profilesIterator = profiles.iterator();
        Profile profile = profilesIterator.next();

        while (profilesIterator.hasNext()) {
            final Profile nextProfile = profilesIterator.next();
            final String profileTitle = profile.getJobTitle();
            final String nextProfileTitle = nextProfile.getJobTitle();
            if (profileTitle != null && nextProfileTitle != null) {
                assertTrue("List is not sorted in ascending order: " + profileTitle + " > " + nextProfileTitle,
                        profileTitle.compareTo(nextProfileTitle) <= 0);
            }
            profile = nextProfile;
        }
    }

    /**
     * test case for GET /profiles response limit
     */
    @Test
    public void testProfileGet_Profiles_limitResponse() {
        List<Profile> profiles = getProfiles(knownEPPN, 1);
        assertTrue("No profiles returned", profiles.size() > 0);
        assertTrue("More then 1 profile returned", profiles.size() <= 1);

        profiles = getProfiles(knownEPPN, 3);
        assertTrue("No profiles returned", profiles.size() > 0);
        assertTrue("More then 3 profiles returned", profiles.size() <= 3);

        profiles = getProfiles(knownEPPN, 0);
        assertTrue("Non-zero profiles returned", profiles.size() == 0);
    }

    /**
     * test case for GET /profiles response with id list
     */
    @Test
    public void testProfileGet_Profiles_IdListResponse() {
        final List<Profile> hundredProfiles = getProfiles(knownEPPN);

        final Random random = new Random();
        final int numberOfSamples = random.nextInt(hundredProfiles.size()) + 1;

        // take subset
        final HashSet<String> idsSet = new HashSet<String>();
        for (int i = 0; i < numberOfSamples; i++) {
            final int sample = random.nextInt(hundredProfiles.size());
            final Profile profile = hundredProfiles.get(sample);
            final String profileId = Integer.toString(profile.getId());
            idsSet.add(profileId);
        }
        final List<String> ids = new ArrayList<String>(idsSet);
        ServiceLogger.log(LOGTAG, "original count of profiles equal " + hundredProfiles.size());
        ServiceLogger.log(LOGTAG, "subset   count of profiles equal " + ids.size());
        ServiceLogger.log(LOGTAG, "subset ids equal " + ids.toString());

        // retrieve subset profiles
        final List<Profile> profilesSubset = getProfiles(knownEPPN, DEFAULT_LIMIT, "DESC", "realname", ids);

        assertTrue("size of ids is " + ids.size() + " and size of profilesSubset is " + profilesSubset.size()
                + ", which are not the same", ids.size() == profilesSubset.size());
    }

    /**
     * test case for GET /profiles/{profileID}/following_members Same logic as
     * get /accounts/{accountID}/following_members
     */
    @Test
    public void testProfileGet_ProfileFollowingMember() throws Exception {
        ServiceLogger.log(LOGTAG, "starting testProfileGet_ProfileFollowingMember");

        final String followerUserName = TestUserUtil.createNewUser();
        final int followerId = UserDao.getUserID(followerUserName);
        final String followerIdText = Integer.toString(followerId);
        final String followedUserName = TestUserUtil.createNewUser();
        final int followedId = UserDao.getUserID(followedUserName);
        final String followedIdText = Integer.toString(followedId);

        FollowingMember followRequest = new FollowingMember();
        followRequest.setFollower(followerIdText);
        followRequest.setFollowed(followedIdText);

        final ValidatableResponse postResponse = given().header("Content-type", APPLICATION_JSON_VALUE)
                .header("AJP_eppn", followerUserName).body(followRequest).expect().statusCode(OK.value()).when()
                .post(FOLLOWING_MEMBERS).then()
                .body(matchesJsonSchemaInClasspath("Schemas/followingMemberSchema.json"));

        final FollowingMember followingMemberResponse = postResponse.extract().as(FollowingMember.class);

        final FollowingMember expected = new FollowingMember();
        expected.setFollower(followerIdText);
        expected.setFollowed(followedIdText);
        assertEquals("POST response does not match expected following member object", expected,
                followingMemberResponse);

        final ValidatableResponse getResponse = given().header("AJP_eppn", followerUserName).expect()
                .statusCode(OK.value()).when().get(PROFILE_FOLLOWING_MEMBERS, followerIdText).then()
                .body(matchesJsonSchemaInClasspath("Schemas/followingMemberListSchema.json"));

        final ArrayList<FollowingMember> list = TestUserUtil.readFollowingMemberResponse(getResponse);
        assertTrue("couldn't find newly added follow entry in GET response list", list.contains(expected));

    }

}
