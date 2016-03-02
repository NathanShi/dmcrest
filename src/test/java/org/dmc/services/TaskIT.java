package org.dmc.services;

import org.junit.Test;

import static com.jayway.restassured.RestAssured.*;
import com.jayway.restassured.RestAssured;

import static org.hamcrest.Matchers.*;
import static com.jayway.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.util.Random;
import java.util.Date;
import java.text.SimpleDateFormat;

import org.json.JSONObject;

//@Ignore
public class TaskIT extends BaseIT {

	@Test
	public void testTaskCreateAndGet() {
      JSONObject json = createTaskJsonSample("testTaskCreateAndGet");
	  Integer id = 
	    given()
			.body(json.toString())
			.expect()
			.statusCode(200)
		.when()
			.post("/tasks/create")
		.then()
			.body(matchesJsonSchemaInClasspath("Schemas/idSchema.json"))
		.extract().path("id");
	  
	  String newGetRequest = "/tasks/" + id.toString();
	
	  // let's query the newly created task and make sure we get it
	  expect().
	    statusCode(200).
	    when().
	    get(newGetRequest).then().
	    log().all().
        body(matchesJsonSchemaInClasspath("Schemas/taskSchema.json"));
	}
	
	// WARNING: this test is ok as long as our test db has task with id = 1
	@Test
	public void testTask1(){
		expect().statusCode(200).when().get("/tasks/1").then().
		log().all().
        body(matchesJsonSchemaInClasspath("Schemas/taskSchema.json"));
	}
	
	@Test
	public void testTaskList(){
		expect().statusCode(200).when().get("/tasks").then().
        body(matchesJsonSchemaInClasspath("Schemas/taskListSchema.json"));
	}
	
	@Test
	public void testTaskCreate(){
		
	    JSONObject json = createTaskJsonSample("testTaskCreate");
		
		given()
			.body(json.toString())
			.expect()
			.statusCode(200)
			.when()
			.post("/tasks/create")
			.then()
				.body(matchesJsonSchemaInClasspath("Schemas/idSchema.json"));
		
	}
	
	private JSONObject createTaskJsonSample(String testDescription)
	{
	    JSONObject json = new JSONObject();
		Date date = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		String unique = format.format(date);
				
        json.put("title", "sample for test " + testDescription + " from junit on " + unique);
		json.put("description", "description for sample test  " + testDescription + " from junit on " + unique);
		json.put("priority", 0);
        json.put("dueDate", 0);      // a user ID in users table 
	    json.put("reporter", "bamboo tester");  // another user ID in users table
		json.put("assignee", 103);    // from group table
		json.put("projectId", 1);     // from group table and project_group_list, 1 is available in both
		return json;
	}
}