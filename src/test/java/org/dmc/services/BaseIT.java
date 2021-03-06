package org.dmc.services;

import com.jayway.restassured.RestAssured;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest("localhost:8080")
public class BaseIT {

	protected static final String APP_TOKEN_HEADER = "APP_TOKEN";
	protected static final String APP_TOKEN = "testtoken";

	@Value("${local.server.port}")
	private int serverPort;

	protected String userEPPN;

	@Before
	public void setup() {
		userEPPN = new String("fforgeadmin");

		// Use the embedded container port when testing
		// This setup() method is inherited by all tests so all tests will use
		// this port similarly
		// It can also be overridden
		String baseURI = System.getProperty("baseURI", "not specified");
		if (baseURI.equals("not specified")) {
			RestAssured.port = serverPort;
			ServiceLogger.log("BaseIT::" + this.getClass().getSimpleName(), "BASE URI not specified.");
		} else {
			ServiceLogger.log("BaseIT::" + this.getClass().getSimpleName(), "BASE URI specified, setting attributes");
			RestAssured.baseURI = baseURI;
			RestAssured.port = Integer.getInteger("port", 8080).intValue();
			RestAssured.basePath = "";

		}
	}

}