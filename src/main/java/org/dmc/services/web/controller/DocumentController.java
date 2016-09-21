package org.dmc.services.web.controller;

import java.util.List;

import javax.inject.Inject;

import org.dmc.services.DMCServiceException;
import org.dmc.services.DocumentService;
import org.dmc.services.ServiceLogger;
import org.dmc.services.data.entities.DocumentParentType;
import org.dmc.services.data.models.DocumentModel;
import org.dmc.services.security.SecurityRoles;
import org.dmc.services.web.validator.AWSLinkValidator;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DocumentController {
	
	private final String logTag = DocumentController.class.getName();

	@Inject
	private DocumentService documentService;
	
	@Inject
	private AWSLinkValidator awsLinkValidator;
	
	@RequestMapping(value="/documents/{id}", method = RequestMethod.GET)
	public DocumentModel getDocument(@PathVariable("id") Integer id) {
		ServiceLogger.log(logTag, "In getDocumentByDocumentId: " + id);
		
		return documentService.findOne(id);
	}
	
	@RequestMapping(value="/documents", params = {"parent_type", "parent_id", "doc_class_id", "limit"}, method = RequestMethod.GET)
	public List<DocumentModel> getDocuments (@RequestParam("parent_type") String parentType, 
			@RequestParam("parent_id") Integer parentId, @RequestParam("doc_class_id") Integer docClassId, @RequestParam("limit") Integer limit) {
		ServiceLogger.log(logTag, "In getDocuments: parent_type = " + parentType + " parent_id = " + parentId + " and docClassId = " + docClassId);
		return documentService.findDocuments(parentType, parentId, docClassId, limit);
	}
	
	@RequestMapping(value="/documents", method = RequestMethod.POST)
	public DocumentModel postDocument (@RequestBody DocumentModel doc, BindingResult result) throws DMCServiceException {
		ServiceLogger.log(logTag, "In postDocument " + doc.getDocumentName());
		validateSaveDocument(doc.getDocumentUrl(), result);
		return documentService.save(doc, result);
	}

	private void validateSaveDocument(String documentUrl, BindingResult result) {
		awsLinkValidator.validate(documentUrl, result);		
	}
	
	@RequestMapping(value="/documents/{documentId}", method = RequestMethod.DELETE)
	public DocumentModel deleteDocument (@PathVariable("documentId") Integer documentId) {
		ServiceLogger.log(logTag, "In deleteDocument id = " + documentId);
		return documentService.delete(documentId);
	}
	
//	@RequestMapping(value="/document/organization/count", params = {"organizationId", "docClassId"}, method = RequestMethod.GET)
//	public Long countDocumentsByOrganizationIdAndDocClassId (@RequestParam("organizationId") Integer organizationId, @RequestParam("docClassId") Integer docClassId) {
//		return documentService.countDocumentsByOrganizationIdAndDocClass(organizationId, docClassId);
//	}
}
