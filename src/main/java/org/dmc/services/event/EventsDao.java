package org.dmc.services.event;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import org.dmc.services.DBConnector;
import org.dmc.services.DMCError;
import org.dmc.services.DMCServiceException;
import org.dmc.services.Id;
import org.dmc.services.ServiceLogger;
import org.dmc.services.company.Company;
import org.dmc.services.services.ServiceImages;
import org.dmc.services.sharedattributes.FeatureImage;
import org.dmc.services.sharedattributes.Util;
import org.dmc.services.users.User;
import org.dmc.services.users.UserDao;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import java.util.ArrayList;
import java.util.List;

import javax.xml.ws.http.HTTPException;

import static org.dmc.services.company.CompanyUserUtil.isMemberOfCompany;

public class EventsDao 
{

	private final String logTag = EventsDao.class.getName();
	private ResultSet resultSet;
	
	
	// Only declare here and instantiate in method where it is used
	// Instantiating here may cause NullPointer Exceptions
	private Connection connection;
	
	public Id createCommunityEvent(CommunityEvent event) throws DMCServiceException
	{
		connection = DBConnector.connection();
		PreparedStatement statement;
		Util util = Util.getInstance();
		int id = -99999;

		// NEED TO PUT Get AWS URL FUNCTION
		//Tests to see if valid user, exits function if so

		try {
			connection.setAutoCommit(false);
		} catch (SQLException ex) {
			ServiceLogger.log(logTag, ex.getMessage());
			throw new DMCServiceException(DMCError.OtherSQLError, "An SQL exception has occured");
		}

		try
		{
			String query = "INSERT INTO community_events (title, date, startTime, endTime, address, description) VALUES (?, ?, ?, ?, ?, ?)";
			statement = DBConnector.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			statement.setString(1, event.getTitle());
			statement.setString(2, event.getDate());
			statement.setString(3, event.getStartTime());
			statement.setString(4, event.getEndTime());
			statement.setString(5, event.getAddress());
			statement.setString(6, event.getDescription());
			statement.executeUpdate();
			id = util.getGeneratedKey(statement, "id");
			ServiceLogger.log(logTag, "Creating discussion, returning ID: " + id);
			connection.commit();
		}
		catch (SQLException e)
		{
			ServiceLogger.log(logTag, "SQL EXCEPTION ----- " + e.getMessage());
			try
			{
				if (connection != null)
				{
					ServiceLogger.log(logTag, "createServiceImage transaction rolled back");
					connection.rollback();
				}
			}
			catch (SQLException ex)
			{
				ServiceLogger.log(logTag, ex.getMessage());
				throw new DMCServiceException(DMCError.OtherSQLError, ex.getMessage());
			}
			throw new DMCServiceException(DMCError.OtherSQLError, e.getMessage());
		}
		
		finally
		{
			try
			{
				if (connection != null)
				{
					connection.setAutoCommit(true);
				}
			}
			catch (SQLException et)
			{
				ServiceLogger.log(logTag, et.getMessage());
				throw new DMCServiceException(DMCError.OtherSQLError, et.getMessage());
			}
		}
		
		return new Id.IdBuilder(id).build();
	}
	
	/*
	 public Id createServiceImages(ServiceImages payload, String userEPPN) throws DMCServiceException {

		connection = DBConnector.connection();
		PreparedStatement statement;
		Util util = Util.getInstance();
		int id = -99999;

		// NEED TO PUT Get AWS URL FUNCTION
		//Tests to see if valid user, exits function if so
    try {
      int userId = UserDao.getUserID(userEPPN);
      if(userId == -1){
    			throw new DMCServiceException(DMCError.NotDMDIIMember, "User: " + userEPPN + " is not valid");
      }
    } catch (SQLException e) {
			ServiceLogger.log(logTag, e.getMessage());
			throw new DMCServiceException(DMCError.NotDMDIIMember, "User: " + userEPPN + " is not valid");
			}

		try {
			connection.setAutoCommit(false);
		} catch (SQLException ex) {
			ServiceLogger.log(logTag, ex.getMessage());
			throw new DMCServiceException(DMCError.OtherSQLError, "An SQL exception has occured");
		}

		try {
			String query = "INSERT INTO service_images (service_id, url) VALUES (?, ?)";
			statement = DBConnector.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			statement.setInt(1, payload.getServiceId());
			statement.setString(2, payload.getUrl());
			statement.executeUpdate();
			id = util.getGeneratedKey(statement, "id");
			ServiceLogger.log(logTag, "Creating discussion, returning ID: " + id);
			connection.commit();
		}
		catch (SQLException e) {
			ServiceLogger.log(logTag, "SQL EXCEPTION ----- " + e.getMessage());
			try {
				if (connection != null) {
					ServiceLogger.log(logTag, "createServiceImage transaction rolled back");
					connection.rollback();
				}
			} catch (SQLException ex) {
				ServiceLogger.log(logTag, ex.getMessage());
				throw new DMCServiceException(DMCError.OtherSQLError, ex.getMessage());
			}
			throw new DMCServiceException(DMCError.OtherSQLError, e.getMessage());
		}
		finally {
			try {
				if (connection != null) {
					connection.setAutoCommit(true);
				}
			} catch (SQLException et) {
				ServiceLogger.log(logTag, et.getMessage());
				throw new DMCServiceException(DMCError.OtherSQLError, et.getMessage());
			}
		}
		return new Id.IdBuilder(id).build();
	}
	 */
	
    public ArrayList<CommunityEvent> getEvents() throws HTTPException {
        ArrayList<CommunityEvent> events = null;
        ServiceLogger.log(logTag, "In Get Events");
        
        CommunityEvent event = new CommunityEvent();
		event.setId("a");
		event.setTitle("b");
		event.setDate("c");
		event.setStartTime("d");
		event.setEndTime("e");;
		event.setAddress("f");
		event.setDescription("g");
        events.add(event);
        
        return events;
	}
        //createCommunityEvent function
        
        
        
        /*
			//For GET Event
	        try {
            // get events;
            // does the organization need to be active member?  assume no.
            resultSet = DBConnector.executeQuery("SELECT organization_id, accountid, name FROM organization");
            companies = new ArrayList<Company>();

			//filling id, title, date, startTime, endTime, address, description
            while (resultSet.next()) {
                String id = resultSet.getString("id");
                String title = resultSet.getInt("title");
                String date = resultSet.getString("date");
				String startTime = resultSet.getString("startTime");
				String  endTime = resultSet.getString("endTime");
				String  address = resultSet.getString("address");
				String  description= resultSet.getString("description");
				
				//DOES THIS NEED SOMETHING LIKE A NEW COMPANY/LIKE  NEW EVENT?
                Company company = new Company();
				company.setId(Integer.toString(id));
				company.setAccountId(Integer.toString(accountId));
				company.setName(name);
                companies.add(company);
            }
        } catch (SQLException e) {
            ServiceLogger.log(logTag, e.getMessage());
            throw new HTTPException(HttpStatus.FORBIDDEN.value());  // ToDo: what error should this be?
        }
        return companies;
	}
			//For POST Event
			 * 
			//For PATCH Event
			//For DELETE Event
 */

     

        
        
        /*		
 * 
 * 
        try {
            // get all organizations;
            // does the organization need to be active member?  assume no.
            resultSet = DBConnector.executeQuery("SELECT organization_id, accountid, name FROM organization");
            events = new ArrayList<CommunityEvent>();

            while (resultSet.next()) {
                int id = resultSet.getInt("organization_id");
                int accountId = resultSet.getInt("accountid");
                String name = resultSet.getString("name");

                Event event = new event();
				event.setId(Integer.toString(id));
				event.setAccountId(Integer.toString(accountId));
				event.setName(name);
                events.add(event);
            }
        } catch (SQLException e) {
            ServiceLogger.log(logTag, e.getMessage());
            throw new HTTPException(HttpStatus.FORBIDDEN.value());  // ToDo: what error should this be?
        }
        */
        
}



