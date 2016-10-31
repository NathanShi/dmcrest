package org.dmc.services.email;

import org.dmc.services.data.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class EmailService {

	private static final String EMAIL_URL;

	private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

	static {
		String host = System.getenv("verifyURL");
		if (host == null) {
			host = "localhost";
		}
		EMAIL_URL = "http://" + host + ":4000/";
	}

	public ResponseEntity sendEmail(User user, Integer template, String token) {
		if(user.getEmail() == null) return ResponseEntity.badRequest().body("User does not have an email!");

		EmailModel emailModel = new EmailModel();
		emailModel.setName(String.format("%s %s", user.getFirstName(), user.getLastName()));
		emailModel.setEmail(user.getEmail());
		emailModel.setToken(token);
		emailModel.setTemplate(template);

		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<EmailModel> request = new HttpEntity<>(emailModel);
		ResponseEntity<EmailModel> response = restTemplate.exchange(EMAIL_URL, HttpMethod.POST, request, EmailModel.class);

		if (!HttpStatus.OK.equals(response.getStatusCode())) {
			logger.warn("Email for user token was not sent for user: {}", user.getEmail());
		}

		return response;
	}

}
