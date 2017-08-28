package org.dmc.services.esignature;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.dmc.services.data.models.eSignStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;
import java.io.InputStreamReader;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import org.dmc.services.ServiceLogger;
@RestController
public class esignController {

	private final String logTag = esignController.class.getName();

	@Autowired
	private ESignService eSignService;

	@RequestMapping(value = "/esignDoc", method = RequestMethod.POST)
	public ResponseEntity<eSignStatus> signDocument(@RequestBody String CompanyInfo) {

			String response = "";

			ServiceLogger.log(logTag, "From frontend : " + CompanyInfo);

      try {
  				//Will throw an exception if esign fails
  				response = eSignService.eSignField(CompanyInfo);
					ServiceLogger.log(logTag, "Response from API :" + response);

					try{
						 JSONObject jsonObj = new JSONObject(response);
						 if (!jsonObj.has("fillable_form_id")){
							 	return new ResponseEntity<eSignStatus>(new eSignStatus("eSignature Failed!", response), HttpStatus.BAD_REQUEST);
						 }
						 else{
							// resultMap.put("url", map.get("url").toString());
							// 	resultMap.put("template_id", map.get("fillable_form_id").toString());
							// 	results = new ObjectMapper().writeValueAsString(resultMap);
							// ServiceLogger.log(logTag, "ResultMap to return :" + resultMap);
							 	JSONObject resultJSONObject = new JSONObject();
								resultJSONObject.put("template_id", jsonObj.get("fillable_form_id"));
								resultJSONObject.put("url", jsonObj.getString("url"));
								return new ResponseEntity<eSignStatus>(new eSignStatus("eSignature Successful!", resultJSONObject.toString()), HttpStatus.OK);
						 }
					}catch (Exception e) {
							e.printStackTrace();
				      return new ResponseEntity<eSignStatus>(new eSignStatus("eSignature Failed!", response), HttpStatus.BAD_REQUEST);
					}

  		} catch (Exception e) {
					return new ResponseEntity<eSignStatus>(new eSignStatus("eSignature Failed!", null), HttpStatus.BAD_REQUEST);
  		}
	}

	@RequestMapping(value = "/esignCheck/{LinkToFillID}", method = RequestMethod.GET)
	public ResponseEntity<eSignStatus> signCheck(@PathVariable("LinkToFillID") String LinkToFillID,
																							 @RequestHeader(value = "AJP_eppn", required = true) String UserEPPN) {

			String response = "";

      try {
  				//Will throw an exception if esign fails
  				response = eSignService.eSignCheck(LinkToFillID);

					try{
						 JSONObject jsonObj = new JSONObject(response);
						 JSONObject resultJsonObject = new JSONObject();
						 if (jsonObj.has("errors")){
							 	return new ResponseEntity<eSignStatus>(new eSignStatus("eSignCheck Failed!", jsonObj.getString("errors")), HttpStatus.BAD_REQUEST);
						 }
						 else if (response == "null"){
							 	return new ResponseEntity<eSignStatus>(new eSignStatus("eSignCheck Failed!", "Error when calling the API"), HttpStatus.BAD_REQUEST);
						 }
						 else{
							 	JSONArray eSignItems = jsonObj.getJSONArray("items");
								JSONArray eSignResultItems = new JSONArray();
								resultJsonObject.put("total", jsonObj.get("total"));
							 	if ((int)jsonObj.get("total") >= 1){

										for (int i = 0; i < eSignItems.length(); i++){
												JSONObject iterative = eSignItems.getJSONObject(i);
												String signatureToken = "";
												if (!iterative.getJSONObject("token").isNull("data")){
														signatureToken = iterative.getJSONObject("token").getJSONObject("data").getString("userEPPN");
												}
												iterative.remove("token");
												iterative.remove("additional_documents");
												if (signatureToken.equals(UserEPPN)){
														iterative.put("user", "same");
												}
												else{
														iterative.put("user", "different");
												}
												eSignResultItems.put(iterative);
										}
								}
								resultJsonObject.put("items", eSignResultItems);
								return new ResponseEntity<eSignStatus>(new eSignStatus("eSignCheck Successful!", resultJsonObject.toString()), HttpStatus.OK);
						 }

					}catch (Exception e) {
						 e.printStackTrace();
				     return new ResponseEntity<eSignStatus>(new eSignStatus("eSignCheck Failed!", response), HttpStatus.BAD_REQUEST);
					}

  		} catch (Exception e) {
					return new ResponseEntity<eSignStatus>(new eSignStatus("eSignCheck Failed!", "Failed to call API!"), HttpStatus.BAD_REQUEST);
  		}
	}

	@RequestMapping(value = "/esignToken", method = RequestMethod.GET)
	public ResponseEntity<eSignStatus> signToken(@RequestHeader(value = "AJP_eppn", required = true) String UserEPPN) {

			String response = "";

      try {
  				//Will throw an exception if esign fails
  				response = eSignService.eSignToken(UserEPPN);

					try{
						 JSONObject jsonObj = new JSONObject(response);
						 if (jsonObj.has("hash")){
						 		return new ResponseEntity<eSignStatus>(new eSignStatus("eSignToken Successful!", jsonObj.getString("hash")), HttpStatus.OK);
						 }
						 else{
							  return new ResponseEntity<eSignStatus>(new eSignStatus("eSignToken Failed!", response), HttpStatus.BAD_REQUEST);
						 }
					}catch (Exception e) {
						 e.printStackTrace();
				     return new ResponseEntity<eSignStatus>(new eSignStatus("eSignToken Failed!", response), HttpStatus.BAD_REQUEST);
					}

  		} catch (Exception e) {
					return new ResponseEntity<eSignStatus>(new eSignStatus("eSignToken Failed!", "eSignToken Failed!"), HttpStatus.BAD_REQUEST);
  		}
	}

	@RequestMapping(value = "/esignCallback", method = RequestMethod.POST)
	public ResponseEntity<eSignStatus> signCallback(@RequestBody String documentID) {

			String response = "";

      try {
  				//Will throw an exception if esign fails
  				response = eSignService.eSignCallback(documentID);
					System.out.println("response " + response);
					return new ResponseEntity<eSignStatus>(new eSignStatus(response, "esignCallback Successful!"), HttpStatus.OK);
  		} catch (Exception e) {
					// System.out.println("failed");
					return new ResponseEntity<eSignStatus>(new eSignStatus(response, "esignCallback Failed!"), HttpStatus.BAD_REQUEST);
  		}
	}
}
