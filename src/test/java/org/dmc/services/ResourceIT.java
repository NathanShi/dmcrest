package org.dmc.services;

import org.dmc.services.data.models.ResourceAssessmentModel;
import org.dmc.services.data.models.ResourceJobModel;
import org.dmc.services.data.models.ResourceLabModel;
import org.dmc.services.data.models.ResourceProjectModel;
import org.dmc.services.data.models.ResourceCourseModel;
import org.dmc.services.data.models.ResourceBayModel;
import org.dmc.services.data.models.ResourceMachineModel;

import java.util.*; 
import org.junit.Test;
import org.springframework.http.HttpStatus;
import java.util.ArrayList;
import java.util.List;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import static org.junit.Assert.assertTrue;

/**
 * Created by Josh Lustig on 6/29/2016.
 */

public class ResourceIT extends BaseIT {

    private String RESOURCE_ASSESSMENT_GET_POST  = "/resource/assessment";
    private String RESOURCE_ASSESSMENT_DELETE  = "/resource/assessment/{id}";
    private String RESOURCE_ASSESSMENT_GET_ID = "/resource/assessment/{id}";

    
    private String RESOURCE_JOB_GET_POST  = "/resource/job";
    private String RESOURCE_JOB_DELETE = "/resource/job/{id}";
    private String RESOURCE_JOB_GET_ID = "/resource/job/{id}";


    private	String RESOURCE_LAB_GET_POST  = "/resource/lab";
    private String RESOURCE_LAB_DELETE = "/resource/lab/{id}";
    private String RESOURCE_LAB_GET_ID = "/resource/lab/{id}";

    private	String RESOURCE_COURSE_GET_POST  = "/resource/course";
    private String RESOURCE_COURSE_DELETE = "/resource/course/{id}";
    private String RESOURCE_COURSE_GET_ID = "/resource/course/{id}";

    private String RESOURCE_PROJECT_GET_POST  = "/resource/project";
    private String RESOURCE_PROJECT_DELETE  = "/resource/project/{id}";
    private String RESOURCE_PROJECT_GET_ID  = "/resource/project/{id}";


    private String RESOURCE_BAY_GET_POST  = "/resource/bay";
    private String RESOURCE_BAY_DELETE = "/resource/bay/{id}";
    private String RESOURCE_BAY_GET_ID = "/resource/bay/{id}";
    
    
    

    private String RESOURCE_MACHINE_GET_POST_DELETE  = "/resource/machine/{bayId}";

    public static final String userEPPN = "fforgeadmin";

 
    /* 
     * TEST CASES FOR RESOURCE: 
     * LAB
     * PROJECT
     * ASSESSMENT
     * COURSE
     * JOB
     * 
     * ALL TEST CASES GET ARRAY LIST, POST NEW ENTITY, COMPARE LENGTHS, DELETE THE SAME ENTITY, AND COMPARE LENGTHS AGAIN. 
     */
    @Test
    public void addAndGetAndDeleteAssessment () {
    	    
    	//Get a list of the current images
        ArrayList<ResourceAssessmentModel> original =
        given().
        		header("AJP_eppn", userEPPN).
                expect().
                statusCode(HttpStatus.OK.value()).
                when().
                get(RESOURCE_ASSESSMENT_GET_POST).
                as(ArrayList.class);
        
        int id = addAssessment();
        
        //Make sure the added image returns a valid id
        assertTrue("Resource ID returned invalid", id != -1);

        //Get a list of the new images 
        ArrayList<ResourceAssessmentModel> newList =
        given().
        		header("AJP_eppn", userEPPN).
                expect().
                statusCode(HttpStatus.OK.value()).
                when().
                get(RESOURCE_ASSESSMENT_GET_POST).
                as(ArrayList.class);

        int numBefore = (original != null) ? original.size() : 0;
        int numAfter  = (newList != null) ? newList.size() : 0;
        int numExpected = numBefore + 1;
        //the new list and old list should only differ by one
        assertTrue ("Adding resource failed"  , numAfter == numExpected);
        
        //Get Resource By ID
        Integer checkId  = 
                given().
        				header("Content-type", "application/json").
        				header("AJP_eppn", userEPPN).
                        expect().
                        statusCode(HttpStatus.OK.value()).
                        when().
                        get(RESOURCE_ASSESSMENT_GET_ID, id).
                        then()
                        .extract().path("id");
        
        assertTrue ("Get individual assessment failed"  , id == checkId);

       

        delete(id, RESOURCE_ASSESSMENT_DELETE);

        ArrayList<ResourceAssessmentModel> after =
        given().
        		header("AJP_eppn", userEPPN).
                expect().
                statusCode(HttpStatus.OK.value()).
                when().
                get(RESOURCE_ASSESSMENT_GET_POST).
                as(ArrayList.class);

        int numAfterDelete  = (after != null) ? after.size() : 0;
        assertTrue ("Deleting resource failed", numAfterDelete == numBefore);

    }
    
    @Test
    public void addAndGetAndDeleteJob() {
	    
    	//Get a list of the current images
        ArrayList<ResourceJobModel> original =
        given().
        		header("AJP_eppn", userEPPN).
                expect().
                statusCode(HttpStatus.OK.value()).
                when().
                get(RESOURCE_JOB_GET_POST).
                as(ArrayList.class);
        
        int id = addJob();
        
        //Make sure the added image returns a valid id
        assertTrue("Resource ID returned invalid", id != -1);

        //Get a list of the new images 
        ArrayList<ResourceJobModel> newList =
        given().
        		header("AJP_eppn", userEPPN).
                expect().
                statusCode(HttpStatus.OK.value()).
                when().
                get(RESOURCE_JOB_GET_POST).
                as(ArrayList.class);

        int numBefore = (original != null) ? original.size() : 0;
        int numAfter  = (newList != null) ? newList.size() : 0;
        int numExpected = numBefore + 1;
        
        //the new list and old list should only differ by one
        assertTrue ("Adding resource failed"  , numAfter == numExpected);
        
        
        //Get Resource By ID
        Integer checkId  = 
                given().
        				header("Content-type", "application/json").
        				header("AJP_eppn", userEPPN).
                        expect().
                        statusCode(HttpStatus.OK.value()).
                        when().
                        get(RESOURCE_JOB_GET_ID, id).
                        then()
                        .extract().path("id");
        
        assertTrue ("Get individual job failed"  , id == checkId);


        delete(id, RESOURCE_JOB_DELETE);

        ArrayList<ResourceJobModel> after =
        given().
        		header("AJP_eppn", userEPPN).
                expect().
                statusCode(HttpStatus.OK.value()).
                when().
                get(RESOURCE_JOB_GET_POST).
                as(ArrayList.class);

        int numAfterDelete  = (after != null) ? after.size() : 0;
        assertTrue ("Deleting resource failed", numAfterDelete == numBefore);

    }
    
    
    @Test
    public void addAndGetAndDeleteCourse() {
	    
    	//Get a list of the current images
        ArrayList<ResourceCourseModel> original =
        given().
        		header("AJP_eppn", userEPPN).
                expect().
                statusCode(HttpStatus.OK.value()).
                when().
                get(RESOURCE_COURSE_GET_POST).
                as(ArrayList.class);
        
        int id = addCourse();
        
        //Make sure the added image returns a valid id
        assertTrue("Resource ID returned invalid", id != -1);

        //Get a list of the new images 
        ArrayList<ResourceCourseModel> newList =
        given().
        		header("AJP_eppn", userEPPN).
                expect().
                statusCode(HttpStatus.OK.value()).
                when().
                get(RESOURCE_COURSE_GET_POST).
                as(ArrayList.class);

        int numBefore = (original != null) ? original.size() : 0;
        int numAfter  = (newList != null) ? newList.size() : 0;
        int numExpected = numBefore + 1;
        
        //the new list and old list should only differ by one
        assertTrue ("Adding resource failed"  , numAfter == numExpected);
        
        //Get Resource By ID
        Integer checkId  = 
                given().
        				header("Content-type", "application/json").
        				header("AJP_eppn", userEPPN).
                        expect().
                        statusCode(HttpStatus.OK.value()).
                        when().
                        get(RESOURCE_COURSE_GET_ID, id).
                        then()
                        .extract().path("id");
        
        assertTrue ("Get individual course failed"  , id == checkId);


        delete(id, RESOURCE_COURSE_DELETE);

        ArrayList<ResourceCourseModel> after =
        given().
        		header("AJP_eppn", userEPPN).
                expect().
                statusCode(HttpStatus.OK.value()).
                when().
                get(RESOURCE_COURSE_GET_POST).
                as(ArrayList.class);

        int numAfterDelete  = (after != null) ? after.size() : 0;
        assertTrue ("Deleting resource failed", numAfterDelete == numBefore);

    }
    
    
    @Test
    public void addAndGetAndDeleteProject() {
	    
    	//Get a list of the current images
        ArrayList<ResourceProjectModel> original =
        given().
        		header("AJP_eppn", userEPPN).
                expect().
                statusCode(HttpStatus.OK.value()).
                when().
                get(RESOURCE_PROJECT_GET_POST).
                as(ArrayList.class);
        
        int id = addProject();
        
        //Make sure the added image returns a valid id
        assertTrue("Resource ID returned invalid", id != -1);

        //Get a list of the new images 
        ArrayList<ResourceProjectModel> newList =
        given().
        		header("AJP_eppn", userEPPN).
                expect().
                statusCode(HttpStatus.OK.value()).
                when().
                get(RESOURCE_PROJECT_GET_POST).
                as(ArrayList.class);

        int numBefore = (original != null) ? original.size() : 0;
        int numAfter  = (newList != null) ? newList.size() : 0;
        int numExpected = numBefore + 1;
        
        //the new list and old list should only differ by one
        assertTrue ("Adding resource failed"  , numAfter == numExpected);
        
        //Get Resource By ID
        Integer checkId  = 
                given().
        				header("Content-type", "application/json").
        				header("AJP_eppn", userEPPN).
                        expect().
                        statusCode(HttpStatus.OK.value()).
                        when().
                        get(RESOURCE_PROJECT_GET_ID, id).
                        then()
                        .extract().path("id");
        
        assertTrue ("Get individual project failed"  , id == checkId);


        delete(id, RESOURCE_PROJECT_DELETE);

        ArrayList<ResourceProjectModel> after =
        given().
        		header("AJP_eppn", userEPPN).
                expect().
                statusCode(HttpStatus.OK.value()).
                when().
                get(RESOURCE_PROJECT_GET_POST).
                as(ArrayList.class);

        int numAfterDelete  = (after != null) ? after.size() : 0;
        assertTrue ("Deleting resource failed", numAfterDelete == numBefore);

    }
    
    
    
    @Test
    public void addAndGetAndDeleteLab() {
	    
    	//Get a list of the current images
        ArrayList<ResourceLabModel> original =
        given().
        		header("AJP_eppn", userEPPN).
                expect().
                statusCode(HttpStatus.OK.value()).
                when().
                get(RESOURCE_LAB_GET_POST).
                as(ArrayList.class);
        
        int id = addLab();
        
        //Make sure the added image returns a valid id
        assertTrue("Resource ID returned invalid", id != -1);

        //Get a list of the new images 
        ArrayList<ResourceLabModel> newList =
        given().
        		header("AJP_eppn", userEPPN).
                expect().
                statusCode(HttpStatus.OK.value()).
                when().
                get(RESOURCE_LAB_GET_POST).
                as(ArrayList.class);

        int numBefore = (original != null) ? original.size() : 0;
        int numAfter  = (newList != null) ? newList.size() : 0;
        int numExpected = numBefore + 1;
        
        //the new list and old list should only differ by one
        assertTrue ("Adding resource failed"  , numAfter == numExpected);
        
        //Get Resource By ID
        Integer checkId  = 
                given().
        				header("Content-type", "application/json").
        				header("AJP_eppn", userEPPN).
                        expect().
                        statusCode(HttpStatus.OK.value()).
                        when().
                        get(RESOURCE_LAB_GET_ID, id).
                        then()
                        .extract().path("id");
        
        assertTrue ("Get individual lab failed"  , id == checkId);


        delete(id, RESOURCE_LAB_DELETE);

        ArrayList<ResourceLabModel> after =
        given().
        		header("AJP_eppn", userEPPN).
                expect().
                statusCode(HttpStatus.OK.value()).
                when().
                get(RESOURCE_LAB_GET_POST).
                as(ArrayList.class);

        int numAfterDelete  = (after != null) ? after.size() : 0;
        assertTrue ("Deleting resource failed", numAfterDelete == numBefore);

    }
       
       
    @Test
    public void addAndGetAndDeleteBay() {
	    
    	//Get a list of the current images
        ArrayList<ResourceBayModel> original =
        given().
        		header("AJP_eppn", userEPPN).
                expect().
                statusCode(HttpStatus.OK.value()).
                when().
                get(RESOURCE_BAY_GET_POST).
                as(ArrayList.class);
        
        int id = addBay();
        
        //Make sure the added resource returns a valid id
        assertTrue("Resource ID returned invalid", id != -1);

        ArrayList<ResourceBayModel> newList =
        given().
        		header("AJP_eppn", userEPPN).
                expect().
                statusCode(HttpStatus.OK.value()).
                when().
                get(RESOURCE_BAY_GET_POST).
                as(ArrayList.class);

        int numBefore = (original != null) ? original.size() : 0;
        int numAfter  = (newList != null) ? newList.size() : 0;
        int numExpected = numBefore + 1;
        
        //the new list and old list should only differ by one
        assertTrue ("Adding resource failed"  , numAfter == numExpected);
                         
        //Get Resource By ID
        Integer checkId  = 
                given().
        				header("Content-type", "application/json").
        				header("AJP_eppn", userEPPN).
                        expect().
                        statusCode(HttpStatus.OK.value()).
                        when().
                        get(RESOURCE_BAY_GET_ID, id).
                        then()
                        .extract().path("id");
        
        assertTrue ("Get individual bay failed"  , id == checkId);

        delete(id, RESOURCE_BAY_DELETE);

        ArrayList<ResourceBayModel> after =
        given().
        		header("AJP_eppn", userEPPN).
                expect().
                statusCode(HttpStatus.OK.value()).
                when().
                get(RESOURCE_BAY_GET_POST).
                as(ArrayList.class);

        int numAfterDelete  = (after != null) ? after.size() : 0;
        assertTrue ("Deleting resource failed", numAfterDelete == numBefore);

    }
    
    
    
    /*
     * Helper Functions
     */
    
    public int addAssessment() {

        int id;
        ResourceAssessmentModel json = new ResourceAssessmentModel();
        json.setId(1000);
        json.setTitle("Title");
        json.setImage("Image"); 
        json.setDescription("Description");
        json.setDateCreated("Date"); 
        json.setLink("Link");
        json.setContact("Contact"); 
        json.setHighlighted(true);
        
        Integer createdId  = 
        given().
        		header("Content-type", "application/json").
        		header("AJP_eppn", userEPPN).
                body(json).
                expect().
                statusCode(HttpStatus.OK.value()).
                when().
                post("/resource/assessment").
                then().
                extract().path("id");

        id = (createdId != null) ? createdId.intValue() : -1;
        return id;
    }
    
    public int addJob() {

        int id;
        ResourceJobModel json = new ResourceJobModel();
        json.setId(1000);
        json.setTitle("Title");
        json.setImage("Image"); 
        json.setDescription("Description");
        json.setDateCreated("Date"); 
        json.setLink("Link");
        json.setContact("Contact"); 
        json.setHighlighted(true);
        
        Integer createdId  = 
        given().
				header("Content-type", "application/json").
				header("AJP_eppn", userEPPN).
                body(json).
                expect().
                statusCode(HttpStatus.OK.value()).
                when().
                post(RESOURCE_JOB_GET_POST).
                then().
                extract().path("id");

        id = (createdId != null) ? createdId.intValue() : -1;
        return id;
    }
    
    public int addCourse() {

        int id;
        ResourceCourseModel json = new ResourceCourseModel();
        json.setId(1000);
        json.setTitle("Title");
        json.setImage("Image"); 
        json.setDescription("Description");
        json.setDateCreated("Date"); 
        json.setLink("Link");
        json.setContact("Contact"); 
        json.setHighlighted(true);
        
        Integer createdId  = 
        given().
				header("Content-type", "application/json").
				header("AJP_eppn", userEPPN).
                body(json).
                expect().
                statusCode(HttpStatus.OK.value()).
                when().
                post(RESOURCE_COURSE_GET_POST).
                then().
                extract().path("id");

        id = (createdId != null) ? createdId.intValue() : -1;
        return id;
    }
    
    public int addLab() {

        int id;
        ResourceLabModel json = new ResourceLabModel();
        json.setId(1000);
        json.setTitle("Title");
        json.setImage("Image"); 
        json.setDescription("Description");
        json.setDateCreated("Date"); 
        json.setLink("Link");
        json.setContact("Contact"); 
        json.setHighlighted(true);
        
        Integer createdId  = 
        given().
				header("Content-type", "application/json").
				header("AJP_eppn", userEPPN).
                body(json).
                expect().
                statusCode(HttpStatus.OK.value()).
                when().
                post(RESOURCE_LAB_GET_POST).
                then()
                .extract().path("id");

        id = (createdId != null) ? createdId.intValue() : -1;
        return id;
    }
    
    public int addProject() {

        int id;
        ResourceProjectModel json = new ResourceProjectModel();
        json.setId(1000);
        json.setTitle("Title");
        json.setImage("Image"); 
        json.setDescription("Description");
        json.setDateCreated("Date"); 
        json.setLink("Link");
        json.setContact("Contact"); 
        json.setHighlighted(true);
        
        Integer createdId  = 
        given().
				header("Content-type", "application/json").
				header("AJP_eppn", userEPPN).
                body(json).
                expect().
                statusCode(HttpStatus.OK.value()).
                when().
                post(RESOURCE_PROJECT_GET_POST).
                then()
                .extract().path("id");

        id = (createdId != null) ? createdId.intValue() : -1;
        return id;
    }
    

    public int addBay() {

        int id;
        ResourceBayModel json = new ResourceBayModel();    	
        json.setId(1000);
        json.setTitle("Title");
        json.setImage("Image"); 
        json.setDescription("Description");
        json.setDateCreated("Date"); 
        json.setLink("Link");
        json.setContact("Contact"); 
        json.setHighlighted(true);
        
        Integer createdId  = 
        given().
				header("Content-type", "application/json").
				header("AJP_eppn", userEPPN).
                body(json).
                expect().
                statusCode(HttpStatus.OK.value()).
                when().
                post(RESOURCE_BAY_GET_POST).
                then()
                .extract().path("id");

        id = (createdId != null) ? createdId.intValue() : -1;
        
        json.setId(id);
        //Create a machine 
        int machineId = addMachine(json); 
        
        //Get all the machines in the bay
        ArrayList<ResourceMachineModel> machines =
                given().
                		header("AJP_eppn", userEPPN).
                        expect().
                        statusCode(HttpStatus.OK.value()).
                        when().
                        get(RESOURCE_MACHINE_GET_POST_DELETE, id).
                        as(ArrayList.class);

                int num  = (machines != null) ? machines.size() : 0;
                assertTrue ("Creating machine failed", num == 1);
                
         //delete machine
         delete(id, RESOURCE_MACHINE_GET_POST_DELETE); 
          
        //Return the id of the bay
        return id;
    }
    
    public int addMachine(ResourceBayModel bay ) {

        int id;
        ResourceMachineModel json = new ResourceMachineModel();
        
        json.setTitle("Title");
        json.setImage("Image"); 
        json.setDescription("Description");
        json.setDateCreated("Date"); 
        json.setLink("Link");
        json.setContact("Contact"); 
        json.setHighlighted(true);
        json.setBay(bay);
       
        Integer createdId  = 
        given().
				header("Content-type", "application/json").
				header("AJP_eppn", userEPPN).
                body(json).
                expect().
                statusCode(HttpStatus.OK.value()).
                when().
                post( "/resource/machine").
                then()
                .extract().path("id");

        id = (createdId != null) ? createdId.intValue() : -1;
        return id;
    }
    
    
    public void delete (int id, String endpoint) {
        given().
        		header("AJP_eppn", userEPPN).
                expect().
                statusCode(HttpStatus.OK.value()).
                when().
                delete(endpoint, id);
    }
    


}
