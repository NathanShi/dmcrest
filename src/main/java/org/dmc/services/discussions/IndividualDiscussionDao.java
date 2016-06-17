package org.dmc.services.discussions;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.xml.ws.http.HTTPException;

import org.dmc.services.DBConnector;
import org.dmc.services.DMCError;
import org.dmc.services.DMCServiceException;
import org.dmc.services.Id;
import org.dmc.services.ServiceLogger;
import org.dmc.services.services.GetDomeInterface;
import org.dmc.services.services.PostUpdateDomeInterface;
import org.dmc.services.sharedattributes.Util;

public class IndividualDiscussionDao {

	private final String logTag = DiscussionDao.class.getName();

	public List<IndividualDiscussion> getListOfCommunityIndividualDiscussions(Integer limit, String order, String sort) throws DMCServiceException {
		Connection connection = DBConnector.connection();
		IndividualDiscussion retObj = null;
		List<IndividualDiscussion> individualDiscussions = new ArrayList<IndividualDiscussion>();

		try {
			connection.setAutoCommit(false);

			ArrayList<String> columnsInIndividualDiscussionTable = new ArrayList<String>();
			columnsInIndividualDiscussionTable.add("id");
			columnsInIndividualDiscussionTable.add("title");
			columnsInIndividualDiscussionTable.add("created_by");
			columnsInIndividualDiscussionTable.add("created_at");
			columnsInIndividualDiscussionTable.add("account_id");
			columnsInIndividualDiscussionTable.add("project_id");

			String domeInterfacesQuery = "SELECT * FROM individual_discussions";

			if (sort == null) {
				domeInterfacesQuery += " ORDER BY id";
			} else if (!columnsInIndividualDiscussionTable.contains(sort)) {
				domeInterfacesQuery += " ORDER BY id";
			} else {
				domeInterfacesQuery += " ORDER BY " + sort;
			}

			if (order == null) {
				domeInterfacesQuery += " ASC";
			} else if (!order.equals("ASC") && !order.equals("DESC")) {
				domeInterfacesQuery += " ASC";
			} else {
				domeInterfacesQuery += " " + order;
			}

			PreparedStatement preparedStatement = DBConnector.prepareStatement(domeInterfacesQuery);
			preparedStatement.execute();
			ResultSet resultSet = preparedStatement.getResultSet();

			int counter = 0;
			while (resultSet.next() && counter < limit) {
				if (new BigDecimal(resultSet.getInt("project_id")) != null) {
					retObj = new IndividualDiscussion();
					counter++;

					retObj.setId(Integer.toString(resultSet.getInt("id")));
					retObj.setTitle(resultSet.getString("title"));
					retObj.setCreatedBy(resultSet.getString("created_by"));
					retObj.setCreatedAt(new BigDecimal(resultSet.getString("created_at")));
					retObj.setAccountId(new BigDecimal(resultSet.getInt("account_id")));
					retObj.setProjectId(new BigDecimal(resultSet.getInt("project_id")));

					individualDiscussions.add(retObj);
				}
			}

		} catch (SQLException se) {
			ServiceLogger.log(logTag, se.getMessage());
			try {
				connection.rollback();
			} catch (SQLException e) {
				ServiceLogger.log(logTag, e.getMessage());
			}
			throw new DMCServiceException(DMCError.OtherSQLError, se.getMessage());

		} finally {
			try {
				connection.setAutoCommit(true);
			} catch (SQLException se) {
				ServiceLogger.log(logTag, se.getMessage());
			}

		}

		return individualDiscussions;
	}

	public IndividualDiscussion createIndividualDiscussion(IndividualDiscussion discussion) throws DMCServiceException {
		IndividualDiscussion retObj = new IndividualDiscussion();
		Connection connection = DBConnector.connection();
		Util util = Util.getInstance();

		try {
			connection.setAutoCommit(false);

			String domeInterfaceQuery = "INSERT into individual_discussions (title, created_by, created_at, account_id, project_id) values ( ?, ?, ?, ?, ? )";

			PreparedStatement preparedStatementDomeInterfaceQuery = DBConnector.prepareStatement(domeInterfaceQuery, Statement.RETURN_GENERATED_KEYS);
			preparedStatementDomeInterfaceQuery.setString(1, discussion.getTitle());
			preparedStatementDomeInterfaceQuery.setString(2, discussion.getCreatedBy());
			preparedStatementDomeInterfaceQuery.setString(3, discussion.getCreatedAt().toString());
			preparedStatementDomeInterfaceQuery.setInt(4, discussion.getAccountId().intValue());
			preparedStatementDomeInterfaceQuery.setInt(5, discussion.getProjectId().intValue());

			int rowsAffected_interface = preparedStatementDomeInterfaceQuery.executeUpdate();
			if (rowsAffected_interface != 1) {
				connection.rollback();
				throw new DMCServiceException(DMCError.OtherSQLError, "unable to add individual discussion " + discussion.toString());
			}
			int id = util.getGeneratedKey(preparedStatementDomeInterfaceQuery, "id");

			retObj.setId(Integer.toString(id));
			retObj.setTitle(discussion.getTitle());
			retObj.setCreatedBy(discussion.getCreatedBy());
			retObj.setCreatedAt(discussion.getCreatedAt());
			retObj.setAccountId(discussion.getAccountId());
			retObj.setProjectId(discussion.getProjectId());

		} catch (SQLException se) {
			ServiceLogger.log(logTag, se.getMessage());
			try {
				connection.rollback();
			} catch (SQLException e) {
				ServiceLogger.log(logTag, e.getMessage());
			}
			throw new DMCServiceException(DMCError.OtherSQLError, se.getMessage());

		} finally {
			try {
				connection.setAutoCommit(true);
			} catch (SQLException se) {
				ServiceLogger.log(logTag, se.getMessage());
			}

		}

		return retObj;
	}

	public IndividualDiscussion getSingleIndividualDiscussionFromId(String id) throws DMCServiceException {
		Connection connection = DBConnector.connection();
		IndividualDiscussion retObj = null;

		try {
			connection.setAutoCommit(false);
			String domeInterfacesQuery = "SELECT * FROM individual_discussions WHERE id = ?";

			PreparedStatement preparedStatement = DBConnector.prepareStatement(domeInterfacesQuery);
			preparedStatement.setInt(1, Integer.parseInt(id));
			preparedStatement.execute();
			ResultSet resultSet = preparedStatement.getResultSet();

			if (resultSet.next()) {
				retObj = new IndividualDiscussion();

				retObj.setId(Integer.toString(resultSet.getInt("id")));
				retObj.setTitle(resultSet.getString("title"));
				retObj.setCreatedBy(resultSet.getString("created_by"));
				retObj.setCreatedAt(new BigDecimal(resultSet.getString("created_at")));
				retObj.setAccountId(new BigDecimal(resultSet.getInt("account_id")));
				retObj.setProjectId(new BigDecimal(resultSet.getInt("project_id")));
			}

		} catch (SQLException se) {
			ServiceLogger.log(logTag, se.getMessage());
			try {
				connection.rollback();
			} catch (SQLException e) {
				ServiceLogger.log(logTag, e.getMessage());
			}
			throw new DMCServiceException(DMCError.OtherSQLError, se.getMessage());

		} finally {
			try {
				connection.setAutoCommit(true);
			} catch (SQLException se) {
				ServiceLogger.log(logTag, se.getMessage());
			}

		}

		return retObj;
	}

	public IndividualDiscussionComment createIndividualDiscussionComment(IndividualDiscussionComment comment) throws DMCServiceException {
		IndividualDiscussionComment retObj = new IndividualDiscussionComment();
		Connection connection = DBConnector.connection();
		Util util = Util.getInstance();

		try {
			connection.setAutoCommit(false);
			String domeInterfaceQuery = "INSERT into individual_discussions_comments (individual_discussion_id, full_name, account_id, comment_id, avatar, reply, text, created_at, likes, dislikes) values ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";

			PreparedStatement preparedStatementDomeInterfaceQuery = DBConnector.prepareStatement(domeInterfaceQuery, Statement.RETURN_GENERATED_KEYS);
			preparedStatementDomeInterfaceQuery.setInt(1, Integer.parseInt(comment.getIndividualDiscussionId()));
			preparedStatementDomeInterfaceQuery.setString(2, comment.getFullName());
			preparedStatementDomeInterfaceQuery.setInt(3, comment.getAccountId().intValue());
			preparedStatementDomeInterfaceQuery.setInt(4, comment.getCommentId().intValue());
			preparedStatementDomeInterfaceQuery.setString(5, comment.getAvatar());
			preparedStatementDomeInterfaceQuery.setBoolean(6, comment.getReply());
			preparedStatementDomeInterfaceQuery.setString(7, comment.getText());
			preparedStatementDomeInterfaceQuery.setString(8, comment.getCreatedAt().toString());
			preparedStatementDomeInterfaceQuery.setInt(9, comment.getLike().intValue());
			preparedStatementDomeInterfaceQuery.setInt(10, comment.getDislike().intValue());

			int rowsAffected_interface = preparedStatementDomeInterfaceQuery.executeUpdate();
			if (rowsAffected_interface != 1) {
				connection.rollback();
				throw new DMCServiceException(DMCError.OtherSQLError, "unable to add individual discussion comment " + comment.toString());
			}
			int id = util.getGeneratedKey(preparedStatementDomeInterfaceQuery, "id");

			retObj.setId(Integer.toString(id));
			retObj.setIndividualDiscussionId(comment.getIndividualDiscussionId());
			retObj.setFullName(comment.getFullName());
			retObj.setAccountId(comment.getAccountId());
			retObj.setCommentId(comment.getCommentId());
			retObj.setAvatar(comment.getAvatar());
			retObj.setReply(comment.getReply());
			retObj.setText(comment.getText());
			retObj.setCreatedAt(comment.getCreatedAt());
			retObj.setLike(comment.getLike());
			retObj.setDislike(comment.getDislike());

		} catch (SQLException se) {
			ServiceLogger.log(logTag, se.getMessage());
			try {
				connection.rollback();
			} catch (SQLException e) {
				ServiceLogger.log(logTag, e.getMessage());
			}
			throw new DMCServiceException(DMCError.OtherSQLError, se.getMessage());

		} finally {
			try {
				connection.setAutoCommit(true);
			} catch (SQLException se) {
				ServiceLogger.log(logTag, se.getMessage());
			}

		}

		return retObj;
	}

	public List<IndividualDiscussionComment> getListOfIndividualDiscussionComments(Integer limit, String order, String sort, String commentId,
			ArrayList<String> individualDiscussionIdList) throws DMCServiceException {
		Connection connection = DBConnector.connection();
		IndividualDiscussionComment retObj = null;
		List<IndividualDiscussionComment> individualDiscussionsComments = new ArrayList<IndividualDiscussionComment>();

		try {
			connection.setAutoCommit(false);

			ArrayList<String> columnsInIndividualDiscussionCommentTable = new ArrayList<String>();
			columnsInIndividualDiscussionCommentTable.add("id");
			columnsInIndividualDiscussionCommentTable.add("individual_discussion_id");
			columnsInIndividualDiscussionCommentTable.add("full_name");
			columnsInIndividualDiscussionCommentTable.add("account_id");
			columnsInIndividualDiscussionCommentTable.add("comment_id");
			columnsInIndividualDiscussionCommentTable.add("avatar");
			columnsInIndividualDiscussionCommentTable.add("reply");
			columnsInIndividualDiscussionCommentTable.add("text");
			columnsInIndividualDiscussionCommentTable.add("created_at");
			columnsInIndividualDiscussionCommentTable.add("likes");
			columnsInIndividualDiscussionCommentTable.add("dislikes");

			String commentsQuery = "SELECT * FROM individual_discussions_comments";

			if (sort == null) {
				commentsQuery += " ORDER BY id";
			} else if (!columnsInIndividualDiscussionCommentTable.contains(sort)) {
				commentsQuery += " ORDER BY id";
			} else {
				commentsQuery += " ORDER BY " + sort;
			}

			if (order == null) {
				commentsQuery += " ASC";
			} else if (!order.equals("ASC") && !order.equals("DESC")) {
				commentsQuery += " ASC";
			} else {
				commentsQuery += " " + order;
			}

			PreparedStatement preparedStatement = DBConnector.prepareStatement(commentsQuery);
			preparedStatement.execute();
			ResultSet resultSet = preparedStatement.getResultSet();

			int counter = 0;
			while (resultSet.next() && counter < limit) {
				if (Integer.parseInt(commentId) == resultSet.getInt("comment_id")) {
					if (individualDiscussionIdList.contains(Integer.toString(resultSet.getInt("individual_discussion_id")))) {
						retObj = new IndividualDiscussionComment();
						counter++;

						retObj.setId(Integer.toString(resultSet.getInt("id")));
						retObj.setIndividualDiscussionId(Integer.toString(resultSet.getInt("individual_discussion_id")));
						retObj.setFullName(resultSet.getString("full_name"));
						retObj.setAccountId(new BigDecimal(resultSet.getInt("account_id")));
						retObj.setCommentId(new BigDecimal(resultSet.getInt("comment_id")));
						retObj.setAvatar(resultSet.getString("avatar"));
						retObj.setReply(resultSet.getBoolean("reply"));
						retObj.setText(resultSet.getString("text"));
						retObj.setCreatedAt(new BigDecimal(resultSet.getString("created_at")));
						retObj.setLike(new BigDecimal(resultSet.getInt("likes")));
						retObj.setDislike(new BigDecimal(resultSet.getInt("dislikes")));

						individualDiscussionsComments.add(retObj);
					}
				}
			}

		} catch (SQLException se) {
			ServiceLogger.log(logTag, se.getMessage());
			try {
				connection.rollback();
			} catch (SQLException e) {
				ServiceLogger.log(logTag, e.getMessage());
			}
			throw new DMCServiceException(DMCError.OtherSQLError, se.getMessage());

		} finally {
			try {
				connection.setAutoCommit(true);
			} catch (SQLException se) {
				ServiceLogger.log(logTag, se.getMessage());
			}

		}

		return individualDiscussionsComments;
	}

	public IndividualDiscussionComment getCommentFromId(String id) throws DMCServiceException {
		Connection connection = DBConnector.connection();
		IndividualDiscussionComment retObj = null;

		try {
			connection.setAutoCommit(false);
			String domeInterfacesQuery = "SELECT * FROM individual_discussions_comments WHERE id = ?";

			PreparedStatement preparedStatement = DBConnector.prepareStatement(domeInterfacesQuery);
			preparedStatement.setInt(1, Integer.parseInt(id));
			preparedStatement.execute();
			ResultSet resultSet = preparedStatement.getResultSet();

			if (resultSet.next()) {
				retObj = new IndividualDiscussionComment();

				retObj.setId(Integer.toString(resultSet.getInt("id")));
				retObj.setIndividualDiscussionId(Integer.toString(resultSet.getInt("individual_discussion_id")));
				retObj.setFullName(resultSet.getString("full_name"));
				retObj.setAccountId(new BigDecimal(resultSet.getInt("account_id")));
				retObj.setCommentId(new BigDecimal(resultSet.getInt("comment_id")));
				retObj.setAvatar(resultSet.getString("avatar"));
				retObj.setReply(resultSet.getBoolean("reply"));
				retObj.setText(resultSet.getString("text"));
				retObj.setCreatedAt(new BigDecimal(resultSet.getString("created_at")));
				retObj.setLike(new BigDecimal(resultSet.getInt("likes")));
				retObj.setDislike(new BigDecimal(resultSet.getInt("dislikes")));
			}

		} catch (SQLException se) {
			ServiceLogger.log(logTag, se.getMessage());
			try {
				connection.rollback();
			} catch (SQLException e) {
				ServiceLogger.log(logTag, e.getMessage());
			}
			throw new DMCServiceException(DMCError.OtherSQLError, se.getMessage());

		} finally {
			try {
				connection.setAutoCommit(true);
			} catch (SQLException se) {
				ServiceLogger.log(logTag, se.getMessage());
			}

		}

		return retObj;
	}

	public IndividualDiscussionComment updateIndividualDiscussionComment(BigDecimal id, IndividualDiscussionComment comment) throws DMCServiceException {

		Connection connection = DBConnector.connection();
		IndividualDiscussionComment retObj = new IndividualDiscussionComment();

		try {
			connection.setAutoCommit(false);

			String updateQuery = "UPDATE individual_discussions_comments SET individual_discussion_id=?, full_name=?, account_id=?, comment_id=?, avatar=?, reply=?, text=?, created_at=?, likes=?, dislikes=? WHERE id = ?";

			PreparedStatement preparedStatement = DBConnector.prepareStatement(updateQuery);
			preparedStatement.setInt(1, Integer.parseInt(comment.getIndividualDiscussionId()));
			preparedStatement.setString(2, comment.getFullName());
			preparedStatement.setInt(3, comment.getAccountId().intValue());
			preparedStatement.setInt(4, comment.getCommentId().intValue());
			preparedStatement.setString(5, comment.getAvatar());
			preparedStatement.setBoolean(6, comment.getReply());
			preparedStatement.setString(7, comment.getText());
			preparedStatement.setString(8, comment.getCreatedAt().toString());
			preparedStatement.setInt(9, comment.getLike().intValue());
			preparedStatement.setInt(10, comment.getDislike().intValue());
			preparedStatement.setInt(11, id.intValue());

			int rowsAffected_interface = preparedStatement.executeUpdate();
			if (rowsAffected_interface != 1) {
				connection.rollback();
				throw new DMCServiceException(DMCError.OtherSQLError, "unable to update individual discussion comment " + comment.toString());
			}

			retObj = comment;
			retObj.setId(id.toString());

		} catch (SQLException se) {
			ServiceLogger.log(logTag, se.getMessage());
			try {
				connection.rollback();
			} catch (SQLException e) {
				ServiceLogger.log(logTag, e.getMessage());
			}
			throw new DMCServiceException(DMCError.OtherSQLError, se.getMessage());

		} finally {
			try {
				connection.setAutoCommit(true);
			} catch (SQLException se) {
				ServiceLogger.log(logTag, se.getMessage());
			}

		}
		return retObj;
	}

	public List<IndividualDiscussionComment> getCommentsForSingleDiscussionId(Integer limit, String order, String sort, String commentId, String individualDiscussionId)
			throws DMCServiceException {
		Connection connection = DBConnector.connection();
		IndividualDiscussionComment retObj = null;
		List<IndividualDiscussionComment> individualDiscussionComments = new ArrayList<IndividualDiscussionComment>();

		try {
			connection.setAutoCommit(false);

			ArrayList<String> columnsInIndividualDiscussionCommentTable = new ArrayList<String>();
			columnsInIndividualDiscussionCommentTable.add("id");
			columnsInIndividualDiscussionCommentTable.add("individual_discussion_id");
			columnsInIndividualDiscussionCommentTable.add("full_name");
			columnsInIndividualDiscussionCommentTable.add("account_id");
			columnsInIndividualDiscussionCommentTable.add("comment_id");
			columnsInIndividualDiscussionCommentTable.add("avatar");
			columnsInIndividualDiscussionCommentTable.add("reply");
			columnsInIndividualDiscussionCommentTable.add("text");
			columnsInIndividualDiscussionCommentTable.add("created_at");
			columnsInIndividualDiscussionCommentTable.add("likes");
			columnsInIndividualDiscussionCommentTable.add("dislikes");

			String commentsQuery = "SELECT * FROM individual_discussions_comments WHERE individual_discussion_id = ?";

			if (sort == null) {
				commentsQuery += " ORDER BY id";
			} else if (!columnsInIndividualDiscussionCommentTable.contains(sort)) {
				commentsQuery += " ORDER BY id";
			} else {
				commentsQuery += " ORDER BY " + sort;
			}

			if (order == null) {
				commentsQuery += " ASC";
			} else if (!order.equals("ASC") && !order.equals("DESC")) {
				commentsQuery += " ASC";
			} else {
				commentsQuery += " " + order;
			}

			PreparedStatement preparedStatement = DBConnector.prepareStatement(commentsQuery);
			preparedStatement.setInt(1, Integer.parseInt(individualDiscussionId));
			preparedStatement.execute();
			ResultSet resultSet = preparedStatement.getResultSet();

			int counter = 0;
			while (resultSet.next() && counter < limit) {
				if (Integer.parseInt(commentId) == resultSet.getInt("comment_id")) {
					retObj = new IndividualDiscussionComment();
					counter++;

					retObj.setId(Integer.toString(resultSet.getInt("id")));
					retObj.setIndividualDiscussionId(Integer.toString(resultSet.getInt("individual_discussion_id")));
					retObj.setFullName(resultSet.getString("full_name"));
					retObj.setAccountId(new BigDecimal(resultSet.getInt("account_id")));
					retObj.setCommentId(new BigDecimal(resultSet.getInt("comment_id")));
					retObj.setAvatar(resultSet.getString("avatar"));
					retObj.setReply(resultSet.getBoolean("reply"));
					retObj.setText(resultSet.getString("text"));
					retObj.setCreatedAt(new BigDecimal(resultSet.getString("created_at")));
					retObj.setLike(new BigDecimal(resultSet.getInt("likes")));
					retObj.setDislike(new BigDecimal(resultSet.getInt("dislikes")));

					individualDiscussionComments.add(retObj);
				}
			}

		} catch (SQLException se) {
			ServiceLogger.log(logTag, se.getMessage());
			try {
				connection.rollback();
			} catch (SQLException e) {
				ServiceLogger.log(logTag, e.getMessage());
			}
			throw new DMCServiceException(DMCError.OtherSQLError, se.getMessage());

		} finally {
			try {
				connection.setAutoCommit(true);
			} catch (SQLException se) {
				ServiceLogger.log(logTag, se.getMessage());
			}

		}

		return individualDiscussionComments;
	}

}
