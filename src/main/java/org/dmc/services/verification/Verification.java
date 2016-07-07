package org.dmc.services.verification;
import org.dmc.services.DMCError;
import org.dmc.services.DMCServiceException;
import org.springframework.web.client.RestTemplate;

public class Verification {
	
	//Env var for our verification machine 
	private static final String targetURL = System.getenv("verifyURL");
	
	
	public VerificationPatch verify (String url, String table, String userEPPN,  int id, String resourceType, String folder) throws DMCServiceException {
		try{ 
			
			//Create upload object
			VerificationPatch upload = new VerificationPatch(); 
			upload.setId(id);
			upload.setTable(table);
			upload.setUrl("http://tempurl");
			upload.setUserEPPN(userEPPN);
			upload.setFolder(folder);
			upload.setResourceType(resourceType);
			upload.setVerified(true);
			

			RestTemplate restTemplate = new RestTemplate();
			
			//URL of API, body of post, data type of response
	        VerificationPatch returnObject = restTemplate.postForObject("http://54.173.115.100:3000", upload, VerificationPatch.class);
	        return returnObject; 

		} 
		catch (Exception e){ 
			throw new DMCServiceException(DMCError.AWSError, e.getMessage());
		}

	}	 //End Verify

}