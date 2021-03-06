package org.dmc.services.search.handlers;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.dmc.services.data.models.UserModel;

/**
 * Created by 200005921 on 2/2/2016.
 */
public class UserResponseHandler implements ResponseHandler<UserModel> {

	//    {
	//        "user_name": "berlier",
	//            "id": "103",
	//            "realname": "berlier test",
	//            "_version_": 1525634045174612000
	//    },

	// id, user_name, realname, skills_data.skill_keyword

	public static final String FIELD_ID = "id";
	public static final String FIELD_REALNAME = "realname";
	public static final String FIELD_USER_NAME = "user_name";
	public static final String FIELD_COMPANY_ID = "company_id";
	public static final String FIELD_COMPANY = "company";

	public List<UserModel> retrieve(QueryResponse queryResponse, String userEPPN) {

		List<UserModel> users = new ArrayList<UserModel>();

		if (queryResponse != null) {
			SolrDocumentList documents = queryResponse.getResults();

			for (SolrDocument doc : documents) {
				String idStr = (String) doc.getFieldValue(FIELD_ID);
				int id = Integer.parseInt(idStr);
				String realname = (String) doc.getFieldValue(FIELD_REALNAME);
				String companyIdStr = (String) doc.getFieldValue(FIELD_COMPANY_ID);
				int companyId = (companyIdStr != null) ? Integer.parseInt(companyIdStr) : -1;

				UserModel u = new UserModel();
				u.setId(id);
				u.setRealname(realname);
				u.setTermsConditions(false);
				u.setCompanyId(companyId);
				users.add(u);
			}
		}
		return users;
	}
}
