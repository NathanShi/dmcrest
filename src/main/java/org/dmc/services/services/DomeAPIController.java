package org.dmc.services.services;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;

import org.dmc.services.ServiceLogger;
import org.dmc.services.DMCError;
import org.dmc.services.DMCServiceException;

import static org.springframework.http.MediaType.*;

@Controller
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringMVCServerCodegen", date = "2016-04-08T14:26:00.636Z")
public class DomeAPIController {

	private final String logTag = DomeAPIController.class.getName();
	private DomeAPIDao domeAPIDao = new DomeAPIDao();

	@RequestMapping(value = "/get-children", produces = { "application/json" }, method = RequestMethod.GET)
	public ResponseEntity childrenGet(
			@RequestParam(value = "dateModified", required = false) String dateModified,
			@RequestParam(value = "description", required = false) String description,
			@RequestParam(value = "domeServer", required = true) String domeServer,
			@RequestParam(value = "modelId", required = false) String modelId,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "path", required = false) List<BigDecimal> path,
			@RequestParam(value = "type", required = false) String type,
			@RequestParam(value = "url", required = false) String url,
			@RequestParam(value = "version", required = false) String version,
			@RequestHeader(value = "AJP_eppn", defaultValue = "testUser") String userEPPN) {

		ServiceLogger.log(logTag, "In childrenGet: as user " + userEPPN);
		
		DomeEntity domeEntity = new DomeEntity();
		domeEntity.setDateModified(dateModified);
		domeEntity.setDescription(description);
		domeEntity.setDomeServer(domeServer);
		domeEntity.setModelId(modelId);
		domeEntity.setName(name);
		domeEntity.setPath(path);
		domeEntity.setType(type);
		domeEntity.setVersion(version);
		
		try {
			return new ResponseEntity<String>(domeAPIDao.getChildren(domeEntity), HttpStatus.OK);
		} catch (DMCServiceException e) {
			ServiceLogger.logException(logTag, e);
			return new ResponseEntity<String>(e.getErrorMessage(), e.getHttpStatusCode());
		}

	}
}
