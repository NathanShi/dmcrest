package org.dmc.services.reviews;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.dmc.services.DBConnector;
import org.dmc.services.DMCError;
import org.dmc.services.DMCServiceException;
import org.dmc.services.Id;
import org.dmc.services.ServiceLogger;
import org.dmc.services.utils.SQLUtils;

import org.dmc.services.reviews.Review;
import org.dmc.services.company.CompanyUserUtil;
import org.dmc.services.data.dao.user.UserDao;
import org.dmc.services.profile.ProfileReview;

import org.dmc.services.sharedattributes.Util;

/**
 * Created by 200005921 on 6/9/2016.
 */
public class ReviewDao<T extends Review> {

    private final String logTag = ReviewDao.class.getName();
    private ResultSet resultSet;
    
    private ReviewType reviewType;
    private String tablePrefix, tableNameField, extraFieldName;
    
    private Util util = Util.getInstance();
    
    public ReviewDao(ReviewType reviewType) {
        this.reviewType = reviewType;
        setTablePrefix();

    }
    
    public <T extends Review> List<T> getAllReviews (int entityId, String reviewId, Integer limit, String order,
                                           String sort, Integer rating, Boolean status, String userEPPN, Class<T> entityClass) throws DMCServiceException {
        List<T> reviews = new ArrayList<T>();

        try {

            String whereClause = " WHERE r." + tablePrefix + "_id = ?";
            int numFields = 1;
            int reviewIdIndex = -1;
            int ratingIndex = -1;
            int statusIndex = -1;

            int reviewIdVal = 0;


            // reviewId > = indicates a specific reviewId, otherwise retrieve all reviews
            if (reviewId != null && reviewId.trim().length() > 0) {

                try {
                    reviewIdVal = Integer.parseInt(reviewId.toString());

                    if (reviewIdVal > 0) {
                        reviewIdIndex = ++numFields;
                        whereClause += " AND r.id = ?";
                        ServiceLogger.log(logTag, "reviewId index is " + reviewIdIndex + ", value is " + reviewId);
                    }
                } catch (NumberFormatException nfe) {
                    ServiceLogger.log(logTag, "Error parsing review id: " + reviewId + ": " + nfe.toString());
                }
            }

            if (rating != null) {
                ratingIndex = ++numFields;
                whereClause += " AND r.rating = ?";
                ServiceLogger.log(logTag, "rating index is " + ratingIndex + ", value is " + rating);
            }

            if (status != null) {
                statusIndex = ++numFields;
                whereClause += " AND r.status = ?";
                ServiceLogger.log(logTag, "status index is " + statusIndex + ", value is " + status);
            }

            // "ORDER BY " + sort + " " + order + " LIMIT " + limit;
            final ArrayList<String> validFieldsForSort = getReviewFields();
            String orderByClause = SQLUtils.buildOrderByClause(order, sort, validFieldsForSort);
            String limitClause = SQLUtils.buildLimitClause(limit);

            String query =
                "Select r.id, r." + tablePrefix + "_id, u.realname as name, r.user_id as accountId, review_timestamp , r.review as comment, r.rating as rating, count(RR.*) AS count_helpfulOrNot " +
                "FROM " + tablePrefix + "_review_new r " +
                "LEFT JOIN " + tablePrefix + "  o ON  r." + tablePrefix + "_id  = o." + extraFieldName + "_id " +
                "LEFT JOIN " + tablePrefix + "_review_rate RR on RR.review_id = r.id " +
                "LEFT JOIN users u ON u.user_id = r.user_id " +
                whereClause +
                " GROUP BY o." + tableNameField + ", r.id, u.realname";

            if (orderByClause != null) {
                query += " " + orderByClause;
            }

            if (limitClause != null) {
                query += limitClause;
            }

            ServiceLogger.log(logTag, "Get " + tablePrefix + " reviews sql=" + query);

            PreparedStatement preparedStatement = DBConnector.prepareStatement(query);
            preparedStatement.setInt(1, entityId);
            if (reviewIdIndex != -1) preparedStatement.setInt(reviewIdIndex, reviewIdVal);
            if (ratingIndex != -1) preparedStatement.setInt(ratingIndex, rating);
            if (statusIndex != -1) preparedStatement.setBoolean(statusIndex, status);

            this.resultSet = preparedStatement.executeQuery();
            while (this.resultSet.next()) {
                T r = null;
                try {
                    r = entityClass.newInstance();
                } catch(InstantiationException ie) {
                    
                } catch(IllegalAccessException iae) {
                    
                }

                switch(reviewType) {
                    case ORGANIZATION:
                        r.setEntityId(Integer.toString(resultSet.getInt("organization_id")));
                        break;
                    case SERVICE:
                        //                        r.setEntityId(Integer.toString(resultSet.getInt("organization_id")));
                        break;
                    case PROFILE:
                    	r.setEntityId(Integer.toString(resultSet.getInt("users_id")));
                        break;
                    default:
                        throw new DMCServiceException(DMCError.Generic, "Unknow review type");
                }

                r.setId(Integer.toString(resultSet.getInt("id")));
                r.setName(resultSet.getString("name"));
                r.setReviewId(reviewId);
                java.sql.Timestamp reviewTimestamp = resultSet.getTimestamp("review_timestamp");
                BigDecimal bdDate = new BigDecimal(reviewTimestamp.getTime());
                r.setDate(bdDate);
                r.setRating(resultSet.getInt("rating"));
                r.setComment(resultSet.getString("comment"));

                // set to true if review has a reply?
                int count_replies = countRepliesForReview(Integer.parseInt(reviewId));
                boolean hasRplies = (count_replies > 0);
                r.setReply(hasRplies);

                //r.setStatus(resultSet.getBoolean("status"));
                r.setStatus(true);

                int count_likes = countHelpfulForReview(Integer.parseInt(r.getId()), true);
                r.setLike(count_likes);

                int count_dislikes = countHelpfulForReview(Integer.parseInt(r.getId()), false);
                r.setDislike(count_dislikes);
                
                // account_id is associated with organization table:  accountId integer
                r.setAccountId(Integer.toString(resultSet.getInt("accountId")));

                if (reviews == null) reviews = new ArrayList<T>();

                reviews.add(r);
            }

        } catch (SQLException ex) {
            throw new DMCServiceException(DMCError.OtherSQLError, "Error: " + ex.toString());
        }
        return reviews;
    }

    
	public <T extends Review> List<T> getReviewByReviewIdWithReplies(String reviewId, String userEPPN, Class<T> entityClass)
			throws DMCServiceException {
		T r = null;
        List<T> reviews = new ArrayList<T>();

		int user_id = -99999;
		try {
	          user_id = UserDao.getUserID(userEPPN);
	      } catch (SQLException sqlEX) {
	          throw new DMCServiceException(DMCError.Generic, "Unknow user " + userEPPN);
	      }
		try {

			String whereClause = " WHERE r.id = ? AND r.user_id = ?";
			int reviewIdVal = 0;
			reviewIdVal = Integer.parseInt(reviewId.toString());
			String query = "Select r.id, r." + tablePrefix
					+ "_id, u.realname as name, r.user_id as accountId, review_timestamp , r.review as comment, r.rating as rating, count(RR.*) AS count_helpfulOrNot "
					+ "FROM " + tablePrefix + "_review_new r " + "LEFT JOIN " + tablePrefix + "  o ON  r." + tablePrefix
					+ "_id  = o." + extraFieldName + "_id " + "LEFT JOIN " + tablePrefix
					+ "_review_rate RR on RR.review_id = r.id " + "LEFT JOIN users u ON u.user_id = r.user_id "
					+ whereClause + " GROUP BY o." + tableNameField + ", r.id, u.realname";

			ServiceLogger.log(logTag, "Get " + tablePrefix + " reviews sql=" + query);

			PreparedStatement preparedStatement = DBConnector.prepareStatement(query);
			preparedStatement.setInt(1, reviewIdVal);
			preparedStatement.setInt(2, user_id);
			this.resultSet = preparedStatement.executeQuery();
			while (this.resultSet.next()) {
				//T r = null;
				try {
					r = entityClass.newInstance();
				} catch (InstantiationException ie) {

				} catch (IllegalAccessException iae) {

				}

				switch (reviewType) {
				case ORGANIZATION:
					r.setEntityId(Integer.toString(resultSet.getInt("organization_id")));
					break;
				case SERVICE:
					// r.setEntityId(Integer.toString(resultSet.getInt("organization_id")));
					break;
				case PROFILE:
					r.setEntityId(Integer.toString(resultSet.getInt("users_id")));
					break;
				default:
					throw new DMCServiceException(DMCError.Generic, "Unknow review type");
				}

				r.setId(Integer.toString(resultSet.getInt("id")));
				r.setName(resultSet.getString("name"));
				r.setReviewId(reviewId);
				java.sql.Timestamp reviewTimestamp = resultSet.getTimestamp("review_timestamp");
				BigDecimal bdDate = new BigDecimal(reviewTimestamp.getTime());
				r.setDate(bdDate);
				r.setRating(resultSet.getInt("rating"));
				r.setComment(resultSet.getString("comment"));

				// set to true if review has a reply?
				int count_replies = countRepliesForReview(Integer.parseInt(reviewId));
				boolean hasRplies = (count_replies > 0);
				r.setReply(hasRplies);

				// r.setStatus(resultSet.getBoolean("status"));
				r.setStatus(true);

				int count_likes = countHelpfulForReview(Integer.parseInt(r.getId()), true);
				r.setLike(count_likes);

				int count_dislikes = countHelpfulForReview(Integer.parseInt(r.getId()), false);
				r.setDislike(count_dislikes);

				// account_id is associated with organization table: accountId
				// integer
				r.setAccountId(Integer.toString(resultSet.getInt("accountId")));
				
				if (reviews == null) reviews = new ArrayList<T>();

                reviews.add(r);	
			}
			List<T> replies = new ArrayList<T>();
			replies = getRepliesByReviewId(reviewId, userEPPN, entityClass);
			reviews.addAll(replies);
			
		} catch (SQLException ex) {
			throw new DMCServiceException(DMCError.OtherSQLError, "Error: " + ex.toString());
		}

		return reviews;
	}
    
    
    private static ArrayList<String> getReviewFields() {
        final ArrayList<String> reviewFields = new ArrayList<String>();
        reviewFields.add("id");
        reviewFields.add("organization_id");
        reviewFields.add("name");
        reviewFields.add("accountId");
        reviewFields.add("review_timestamp");
        reviewFields.add("comment");
        reviewFields.add("rating");
        reviewFields.add("count_helpfulOrNot");
        return reviewFields;
    }

    private static ArrayList<String> getReviewReplyFields() {
        final ArrayList<String> reviewReplyFields = new ArrayList<String>();
        reviewReplyFields.add("id");
        reviewReplyFields.add("organization_id");
        reviewReplyFields.add("name");
        reviewReplyFields.add("accountId");
        reviewReplyFields.add("review_reply_timestamp");
        reviewReplyFields.add("comment");
        reviewReplyFields.add("count_helpfulOrNot");
        return reviewReplyFields;
    }

    public <T extends Review> List<T> getReviewReplies(int entityId, String reviewId, Integer limit, String order,
                                    String sort, Integer rating, Boolean status, String userEPPN, Class<T> entityClass) throws DMCServiceException {

        List<T> reviews = new ArrayList<T>();

        try {

            String whereClause = " WHERE ";
            int numFields = 0;
            int reviewIdIndex = -1;
            int ratingIndex = -1;
            int statusIndex = -1;

            int reviewIdVal = 0;

            // reviewId > = indicates a specific reviewId, otherwise retrieve all reviews
            if (reviewId != null && reviewId.trim().length() > 0) {

                try {
                    reviewIdVal = Integer.parseInt(reviewId.toString());

                    if (reviewIdVal > 0) {
                        reviewIdIndex = ++numFields;
                        whereClause += " r.review_id = ?";
                        ServiceLogger.log(logTag, "reviewId index is " + reviewIdIndex + ", value is " + reviewId);
                    }
                } catch (NumberFormatException nfe) {
                    ServiceLogger.log(logTag, "Error parsing review id: " + reviewId + ": " + nfe.toString());
                }
            }

            if (reviewIdVal <= 0) {
                ServiceLogger.log(logTag, "review_id must be greater than 0 to retrieve company review replies");
                throw new DMCServiceException(DMCError.OtherSQLError, "review_id must be greater than 0 to retrieve company review replies");

            }

//            if (rating != null) {
//                ratingIndex = ++numFields;
//                whereClause += " AND r.rating = ?";
//                ServiceLogger.log(logTag, "rating index is " + ratingIndex + ", value is " + rating);
//            }

//            if (status != null) {
//                statusIndex = ++numFields;
//                whereClause += " AND r.status = ?";
//                ServiceLogger.log(logTag, "status index is " + statusIndex + ", value is " + status);
//            }

            // "ORDER BY " + sort + " " + order + " LIMIT " + limit;

            final ArrayList<String> validFieldsForSort = getReviewReplyFields();
            String orderByClause = SQLUtils.buildOrderByClause(order, sort, validFieldsForSort);
            String limitClause = SQLUtils.buildLimitClause(limit);

            String query =
                "select r.id, r.review_id AS reviewId, rn." + tablePrefix + "_id as " + tablePrefix + "_id, u.realname as name, r.user_id as accountId, r.review_reply_timestamp , r.review_reply as comment, count(RR.*) AS count_helpfulOrNot " +
                "FROM " + tablePrefix + "_review_reply r " +
                "LEFT JOIN " + tablePrefix + "_review_new rn ON rn.id = r.review_id " +
                "LEFT JOIN " + tablePrefix + "  o ON  rn." + tablePrefix + "_id  = o." + extraFieldName + "_id " +
                "LEFT JOIN " + tablePrefix + "_review_reply_rate RR on RR.review_reply_id = r.id " +
                "LEFT JOIN users u ON u.user_id = r.user_id " +
                whereClause +
                " GROUP BY rn." + tablePrefix + "_id, o." + tableNameField + ", r.id, u.realname";

            if (orderByClause != null) {
                query += " " + orderByClause;
            }

            if (limitClause != null) {
                query += limitClause;
            }

            ServiceLogger.log(logTag, "Get " + tablePrefix + " review replies sql=" + query);

            PreparedStatement preparedStatement = DBConnector.prepareStatement(query);
            preparedStatement.setInt(1, entityId);
            if (reviewIdIndex != -1) preparedStatement.setInt(reviewIdIndex, reviewIdVal);
            if (ratingIndex != -1) preparedStatement.setInt(ratingIndex, rating);
            if (statusIndex != -1) preparedStatement.setBoolean(statusIndex, status);

            this.resultSet = preparedStatement.executeQuery();
            while (this.resultSet.next()) {
                T r = null;
                try {
                    r = entityClass.newInstance();
                } catch(InstantiationException ie) {
                    
                } catch(IllegalAccessException iae) {
                    
                }
                
                switch(reviewType) {
                    case ORGANIZATION:
                        r.setEntityId(Integer.toString(resultSet.getInt("organization_id")));
                        break;
                    case SERVICE:
//                        r.setEntityId(Integer.toString(resultSet.getInt("organization_id")));
                        break;
                    case PROFILE:
                    	r.setEntityId(Integer.toString(resultSet.getInt("users_id")));
                        break;
                    default:
                        throw new DMCServiceException(DMCError.Generic, "Unknow review type");
                }
                
                r.setId(Integer.toString(resultSet.getInt("id")));
                
                r.setName(resultSet.getString("name"));

                r.setReviewId(resultSet.getString("reviewId"));
                java.sql.Timestamp reviewTimestamp = resultSet.getTimestamp("review_reply_timestamp");
                BigDecimal bdDate = new BigDecimal(reviewTimestamp.getTime());
                r.setDate(bdDate);
                r.setRating(0); // replies do not have stars
                r.setComment(resultSet.getString("comment"));

                // replies do not have replies
                boolean hasRplies = false;
                r.setReply(hasRplies);

                //r.setStatus(resultSet.getBoolean("status"));
                r.setStatus(true);

                int count_likes = countHelpfulForReviewReply(Integer.parseInt(r.getId()), true);
                r.setLike(count_likes);

                int count_dislikes = countHelpfulForReviewReply(Integer.parseInt(r.getId()), false);
                r.setDislike(count_dislikes);

                // account_id is associated with organization table:  accountId integer
                r.setAccountId(Integer.toString(resultSet.getInt("accountId")));

                if (reviews == null) reviews = new ArrayList<T>();

                reviews.add(r);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new DMCServiceException(DMCError.OtherSQLError, "Error: " + ex.toString());
        }

        return reviews;
    }

	public <T extends Review> List<T> getRepliesByReviewId(String reviewId, String userEPPN, Class<T> entityClass)
			throws DMCServiceException {

		List<T> reviews = new ArrayList<T>();

		try {

			String whereClause = " WHERE ";
			int numFields = 0;
			int reviewIdIndex = -1;
			int ratingIndex = -1;
			int statusIndex = -1;

			int reviewIdVal = 0;

			// reviewId > = indicates a specific reviewId, otherwise retrieve
			// all reviews
			if (reviewId != null && reviewId.trim().length() > 0) {

				try {
					reviewIdVal = Integer.parseInt(reviewId.toString());

					if (reviewIdVal > 0) {
						reviewIdIndex = ++numFields;
						whereClause += " r.review_id = ?";
						ServiceLogger.log(logTag, "reviewId index is " + reviewIdIndex + ", value is " + reviewId);
					}
				} catch (NumberFormatException nfe) {
					ServiceLogger.log(logTag, "Error parsing review id: " + reviewId + ": " + nfe.toString());
				}
			}

			if (reviewIdVal <= 0) {
				ServiceLogger.log(logTag, "review_id must be greater than 0 to retrieve company review replies");
				throw new DMCServiceException(DMCError.OtherSQLError,
						"review_id must be greater than 0 to retrieve company review replies");
			}


			String query = "select r.id, r.review_id AS reviewId, rn." + tablePrefix + "_id as " + tablePrefix
					+ "_id, u.realname as name, r.user_id as accountId, r.review_reply_timestamp , r.review_reply as comment, count(RR.*) AS count_helpfulOrNot "
					+ "FROM " + tablePrefix + "_review_reply r " + "LEFT JOIN " + tablePrefix
					+ "_review_new rn ON rn.id = r.review_id " + "LEFT JOIN " + tablePrefix + "  o ON  rn."
					+ tablePrefix + "_id  = o." + extraFieldName + "_id " + "LEFT JOIN " + tablePrefix
					+ "_review_reply_rate RR on RR.review_reply_id = r.id "
					+ "LEFT JOIN users u ON u.user_id = r.user_id " + whereClause + " GROUP BY rn." + tablePrefix
					+ "_id, o." + tableNameField + ", r.id, u.realname";

			ServiceLogger.log(logTag, "Get " + tablePrefix + " review replies sql=" + query);

			PreparedStatement preparedStatement = DBConnector.prepareStatement(query);
			preparedStatement.setInt(1, reviewIdVal);
		/*	if (reviewIdIndex != -1)
				preparedStatement.setInt(reviewIdIndex, reviewIdVal);*/
			
			this.resultSet = preparedStatement.executeQuery();
			while (this.resultSet.next()) {
				T r = null;
				try {
					r = entityClass.newInstance();
				} catch (InstantiationException ie) {

				} catch (IllegalAccessException iae) {

				}

				switch (reviewType) {
				case ORGANIZATION:
					r.setEntityId(Integer.toString(resultSet.getInt("organization_id")));
					break;
				case SERVICE:
					// r.setEntityId(Integer.toString(resultSet.getInt("organization_id")));
					break;
				case PROFILE:
					r.setEntityId(Integer.toString(resultSet.getInt("users_id")));
					break;
				default:
					throw new DMCServiceException(DMCError.Generic, "Unknow review type");
				}

				r.setId(Integer.toString(resultSet.getInt("id")));

				r.setName(resultSet.getString("name"));

				r.setReviewId(resultSet.getString("reviewId"));
				java.sql.Timestamp reviewTimestamp = resultSet.getTimestamp("review_reply_timestamp");
				BigDecimal bdDate = new BigDecimal(reviewTimestamp.getTime());
				r.setDate(bdDate);
				r.setRating(0); // replies do not have stars
				r.setComment(resultSet.getString("comment"));

				// replies do not have replies
				boolean hasRplies = false;
				r.setReply(hasRplies);

				// r.setStatus(resultSet.getBoolean("status"));
				r.setStatus(true);

				int count_likes = countHelpfulForReviewReply(Integer.parseInt(r.getId()), true);
				r.setLike(count_likes);

				int count_dislikes = countHelpfulForReviewReply(Integer.parseInt(r.getId()), false);
				r.setDislike(count_dislikes);

				// account_id is associated with organization table: accountId
				// integer
				r.setAccountId(Integer.toString(resultSet.getInt("accountId")));

				if (reviews == null)
					reviews = new ArrayList<T>();

				reviews.add(r);
			}

		} catch (SQLException ex) {
			ex.printStackTrace();
			throw new DMCServiceException(DMCError.OtherSQLError, "Error: " + ex.toString());
		}

		return reviews;
	}
    
    
    public int countRepliesForReview (int reviewId) {
        String q = "select count(*) FROM " + tablePrefix + "_review_reply WHERE review_id = " + reviewId;
        int count = 0;
        ResultSet rs = DBConnector.executeQuery(q);
        try {
            while (rs.next()) {
                count = rs.getInt("count");
            }
        } catch (SQLException sqlEX) {
            // ignore
        }
        return count;
    }


    public ReviewHelpful createHelpfulReview(ReviewHelpful serviceReviewHelpful, String userEPPN) throws DMCServiceException {
        int user_id = -9999;
        ServiceLogger.log(logTag, "createHelpfulReview: with user " + userEPPN +
                          " for reviewId " + serviceReviewHelpful.getReviewId() +
                          " with helpful = " + serviceReviewHelpful.getHelpful());

        try {
            user_id = CompanyUserUtil.getUserId(userEPPN);
        } catch (SQLException sqlEX) {
            throw new DMCServiceException(DMCError.Generic, "Unknow user " + userEPPN);
        }
        
        int reviewIdInt = Integer.parseInt(serviceReviewHelpful.getReviewId());
        Date date= new Date();
        
        String sqlInsertHelpfulReview = "INSERT INTO " + tablePrefix + "_review_rate (review_id, user_id, review_rate_timestamp, helpfulornot) VALUES (?,?,?,?)";

        try {
            PreparedStatement preparedStatement = DBConnector.prepareStatement(sqlInsertHelpfulReview, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setInt(1, reviewIdInt);
            preparedStatement.setInt(2, user_id);
            preparedStatement.setTimestamp(3, new java.sql.Timestamp(date.getTime()));
            preparedStatement.setBoolean(4, serviceReviewHelpful.getHelpful());
            
            preparedStatement.executeUpdate();
            
            int id = util.getGeneratedKey(preparedStatement, "id");
            serviceReviewHelpful.setId(Integer.toString(id));
            
            return serviceReviewHelpful;
            
        } catch (SQLException sqlEX) {
            throw new DMCServiceException(DMCError.OtherSQLError, sqlEX.getMessage());
        }
    }
    
    public List<ReviewHelpful> getHelpfulReview(String reviewId, String accountId, String userEPPN) throws DMCServiceException {
        int user_id = -9999;
        int account_id = Integer.parseInt(accountId);
        
        try {
            user_id = CompanyUserUtil.getUserId(userEPPN);
        } catch (SQLException sqlEX) {
            throw new DMCServiceException(DMCError.Generic, "Unknow user " + userEPPN);
        }
        
        if(account_id != user_id) {
            throw new DMCServiceException(DMCError.Generic, "user and account ids do not match");
        }
        
        ArrayList<ReviewHelpful> reviewHelpfulList = new ArrayList<ReviewHelpful>();
        
        String sqlInsertHelpfulReview = "SELECT * FROM  " + tablePrefix + "_review_rate WHERE review_id = ? AND user_id = ?";
        
        final PreparedStatement preparedStatement = DBConnector.prepareStatement(sqlInsertHelpfulReview);
        try {
            preparedStatement.setInt(1, Integer.parseInt(reviewId));
            preparedStatement.setInt(2, user_id);
        
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                ReviewHelpful reviewHelpful = newHelpfulReview(rs.getInt("id"),
                                                               rs.getInt("review_id"),
                                                               rs.getInt("user_id"),
                                                               rs.getBoolean("helpfulornot"));
            
                reviewHelpfulList.add(reviewHelpful);
            }
        } catch (SQLException sqlEX) {
            throw new DMCServiceException(DMCError.Generic, "Error retrieving database records");
            
        }
        
        return reviewHelpfulList;
    }
    
    
    public ReviewHelpful patchHelpfulReview(String helpfulID, ReviewHelpful helpful, String userEPPN) throws DMCServiceException {
        int user_id = -9999;
        
        try {
            user_id = CompanyUserUtil.getUserId(userEPPN);
        } catch (SQLException sqlEX) {
            throw new DMCServiceException(DMCError.Generic, "Unknow user " + userEPPN);
        }
        
        if(Integer.parseInt(helpful.getAccountId()) != user_id) {
            throw new DMCServiceException(DMCError.Generic, "user and account ids do not match");
        }
        
        if(helpful.getHelpful() == null) { // remove record if user unselects review as helpful or not
            deleteHelpfulReview(helpfulID, helpful);
            return helpful;
        }
        
        String sqlUpdateHelpfulReview = "UPDATE " + tablePrefix + "_review_rate SET helpfulornot = ? AND review_id = ? WHERE id = ? AND user_id = ?";
        
        final PreparedStatement preparedStatement = DBConnector.prepareStatement(sqlUpdateHelpfulReview);
        try {
            preparedStatement.setBoolean(1, helpful.getHelpful());
            preparedStatement.setInt(2, Integer.parseInt(helpful.getReviewId()));
            preparedStatement.setInt(3, Integer.parseInt(helpfulID));
            preparedStatement.setInt(4, user_id);
            
            if(preparedStatement.executeUpdate() != 1) {
                throw new SQLException("Unable to update " + tablePrefix + "_review_rate" +
                                       " for user_id: " + user_id + " and record " + helpfulID);
            }
            
        } catch (SQLException sqlEX) {
            throw new DMCServiceException(DMCError.Generic, "Error updating database records");
            
        }
        return helpful;
    }
    
    private void deleteHelpfulReview(String helpfulID, ReviewHelpful helpful) {
        String sqlDeleteHelpfulReview = "DELETE FROM " + tablePrefix + "_review_rate WHERE review_id = ? AND id = ? AND user_id = ?";
        int user_id = Integer.parseInt(helpful.getAccountId());
        final PreparedStatement preparedStatementDelete = DBConnector.prepareStatement(sqlDeleteHelpfulReview);
        
        try {
            preparedStatementDelete.setInt(1, Integer.parseInt(helpful.getReviewId()));
            preparedStatementDelete.setInt(2, Integer.parseInt(helpfulID));
            preparedStatementDelete.setInt(3, user_id);
            
            if(preparedStatementDelete.executeUpdate() != 1) {
                throw new SQLException("Unable to delete " + tablePrefix + "_review_rate" +
                                       " for user_id: " + user_id + " and record " + helpfulID);
            }
            
        } catch (SQLException sqlEX) {
            throw new DMCServiceException(DMCError.Generic, "Error deleting database records");
            
        }
        return;
    }
    
    private ReviewHelpful newHelpfulReview(int id, int accountid, int userid, boolean helpfulornot) {
        ReviewHelpful reviewHelpful = new ReviewHelpful();
        reviewHelpful.setId(Integer.toString(id));
        reviewHelpful.setReviewId(Integer.toString(accountid));
        reviewHelpful.setAccountId(Integer.toString(userid));
        reviewHelpful.setHelpful(helpfulornot);
        return reviewHelpful;
    }
    
    public int countHelpfulForReview (int reviewId, boolean helpfulOrNot) {
        String q = "select count(*) FROM " + tablePrefix + "_review_rate WHERE review_id = " + reviewId + " AND helpfulOrNot IS " + Boolean.toString(helpfulOrNot);
        int count = 0;
        ResultSet rs = DBConnector.executeQuery(q);
        try {
            while (rs.next()) {
                count = rs.getInt("count");
            }
        } catch (SQLException sqlEX) {
            // ignore
        }
        ServiceLogger.log(logTag, "reviewId:" + reviewId + " has " + count + " " + helpfulOrNot + " reviews");

        return count;
    }

    public int countHelpfulForReviewReply (int reviewReplyId, boolean helpfulOrNot) {
        String q = "select count(*) FROM " + tablePrefix + "_review_reply_rate WHERE review_reply_id = " + reviewReplyId + " AND helpful_or_not IS " + Boolean.toString(helpfulOrNot);
        int count = 0;
        ResultSet rs = DBConnector.executeQuery(q);
        try {
            while (rs.next()) {
                count = rs.getInt("count");
            }
        } catch (SQLException sqlEX) {
            // ignore
        }
        return count;
    }

    public ReviewFlagged createFlaggedReview(ReviewFlagged reviewFlagged, String userEPPN) throws DMCServiceException {
        int user_id = -9999;
        
        try {
            user_id = CompanyUserUtil.getUserId(userEPPN);
        } catch (SQLException sqlEX) {
            throw new DMCServiceException(DMCError.Generic, "Unknow user " + userEPPN);
        }
        
        int reviewIdInt = Integer.parseInt(reviewFlagged.getReviewId());
        Date date= new Date();
        
        String sqlInsertHelpfulReview = "INSERT INTO " + tablePrefix + "_review_flag (review_id, user_id, review_flag_timestamp, reason, comment) VALUES (?,?,?,?, ?)";
        
        try {
            PreparedStatement preparedStatement = DBConnector.prepareStatement(sqlInsertHelpfulReview, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setInt(1, reviewIdInt);
            preparedStatement.setInt(2, user_id);
            preparedStatement.setTimestamp(3, new java.sql.Timestamp(date.getTime()));
            preparedStatement.setString(4, "TODO: ADD REASON");
            preparedStatement.setString(5, "TODO: ADD COMMENT");
            
            preparedStatement.executeUpdate();
            
            int id = util.getGeneratedKey(preparedStatement, "id");
            reviewFlagged.setId(Integer.toString(id));
            
            return reviewFlagged;
            
        } catch (SQLException sqlEX) {
            throw new DMCServiceException(DMCError.OtherSQLError, sqlEX.getMessage());
        }
    }
    
    public List<ReviewFlagged> getFlaggedReview(String reviewId, String accountId, String userEPPN) throws DMCServiceException {
        int user_id = -9999;
        int account_id = Integer.parseInt(accountId);
        
        try {
            user_id = CompanyUserUtil.getUserId(userEPPN);
        } catch (SQLException sqlEX) {
            throw new DMCServiceException(DMCError.Generic, "Unknow user " + userEPPN);
        }
        
        if(account_id != user_id) {
            throw new DMCServiceException(DMCError.Generic, "user and account ids do not match");
        }
        
        ArrayList<ReviewFlagged> reviewFlaggedList = new ArrayList<ReviewFlagged>();
        
        String sqlInsertHelpfulReview = "SELECT * FROM  " + tablePrefix + "_review_flag WHERE review_id = ? AND user_id = ?";
        
        final PreparedStatement preparedStatement = DBConnector.prepareStatement(sqlInsertHelpfulReview);
        try {
            preparedStatement.setInt(1, Integer.parseInt(reviewId));
            preparedStatement.setInt(2, user_id);
            
            ResultSet rs = preparedStatement.executeQuery();
            
            while (rs.next()) {
                ReviewFlagged reviewFlagged = newFlaggedReview(rs.getInt("id"),
                                                               rs.getInt("review_id"),
                                                               rs.getInt("user_id"));  // skipping reason and comment
                
                reviewFlaggedList.add(reviewFlagged);
            }
        } catch (SQLException sqlEX) {
            throw new DMCServiceException(DMCError.Generic, "Error retrieving database records");
            
        }
        
        return reviewFlaggedList;
    }

    private ReviewFlagged newFlaggedReview(int id, int accountid, int userid) {
        ReviewFlagged reviewFlagged = new ReviewFlagged();
        reviewFlagged.setId(Integer.toString(id));
        reviewFlagged.setReviewId(Integer.toString(accountid));
        reviewFlagged.setAccountId(Integer.toString(userid));
        return reviewFlagged;
    }

    private void setTablePrefix() throws DMCServiceException {
        switch(reviewType) {
            case ORGANIZATION:
                tablePrefix = "organization";
                tableNameField = "name";
                extraFieldName = tablePrefix;
                break;
            case SERVICE:
                tablePrefix = "service";
                tableNameField = "title";
                extraFieldName = tablePrefix;
                break;
            case PROFILE:
                tablePrefix = "users";
                tableNameField = "realname";
                extraFieldName = "user";
                break;
            default:
                throw new DMCServiceException(DMCError.Generic, "Unknow review type");
        }
    }
    
    public Id createReview (T review, String userEPPN) throws DMCServiceException {
        int id = -1;
        
        //ToDo: compare review.getAccountId() with userEPPN id
        
        // organization_id, user_id, review_timestamp, review, stars
        String sqlInsertReview = "INSERT INTO " + tablePrefix + "_review_new (" + tablePrefix + "_id, user_id, review_timestamp, review, rating) VALUES (?,?,?,?,?)";

        // user_id integer, review_reply_timestamp timestamp, review_id integer, review_reply text
        String sqlInsertReply = "INSERT INTO " + tablePrefix + "_review_reply (user_id, review_reply_timestamp, review_id, review_reply) VALUES (?,?,?,?)";

        try {
            int reviewIdInt = 0;
            if (review.getReviewId() != null) {
                try {
                    reviewIdInt = Integer.parseInt(review.getReviewId());
                } catch (NumberFormatException nfe) {

                }
            }

            String tableInserted = tablePrefix + "_review_new";
            PreparedStatement preparedStatement = null;
            if (reviewIdInt > 0) {
                // Insert into organization_review_reply
                preparedStatement = DBConnector.prepareStatement(sqlInsertReply);
                preparedStatement.setInt(1, Integer.parseInt(review.getAccountId()));
                preparedStatement.setTimestamp(2, new java.sql.Timestamp(review.getDate().longValue()));
                preparedStatement.setInt(3, reviewIdInt);
                preparedStatement.setString(4, review.getComment());

                tableInserted = tablePrefix + "_review_reply";

            } else {
                // Insert into organization_review_new
                preparedStatement = DBConnector.prepareStatement(sqlInsertReview);
                preparedStatement.setInt(1, Integer.parseInt(review.getEntityId()));
                preparedStatement.setInt(2, Integer.parseInt(review.getAccountId()));
                preparedStatement.setTimestamp(3, new java.sql.Timestamp(review.getDate().longValue()));
                preparedStatement.setString(4, review.getComment());
                preparedStatement.setInt(5, review.getRating().intValue());

                tableInserted = tablePrefix + "_review_new";
            }


            int rCreate = preparedStatement.executeUpdate();

            String queryId = "select max(id) max_id from " + tableInserted;
            PreparedStatement preparedStatement1 = DBConnector.prepareStatement(queryId);
            ResultSet r=preparedStatement1.executeQuery();
            r.next();
            id=r.getInt("max_id");

        } catch (SQLException sqlEx) {
            throw new DMCServiceException(DMCError.OtherSQLError, sqlEx.toString());
        }

        return new Id.IdBuilder(id).build();
    }


	public ProfileReview patchProfileReview(String reviewId, ProfileReview review, String userEPPN) {
		ServiceLogger.log(logTag, "Starting running patchProfileReview: ");
		int user_id = -9999;
		Connection connection = DBConnector.connection();    
		int reviewIdIndex = Integer.parseInt(reviewId);
      try {
          user_id = CompanyUserUtil.getUserId(userEPPN);
      } catch (SQLException sqlEX) {
          throw new DMCServiceException(DMCError.Generic, "Unknow user " + userEPPN);
      }
      
      if(Integer.parseInt(review.getAccountId()) != user_id) {
          throw new DMCServiceException(DMCError.Generic, "user and account ids do not match");
      }
      String updateUserReviewNew = "UPDATE " + tablePrefix + "_review_new SET review = ?, rating = ? WHERE id = ?";
      String sqlInsertHelpfulReview = "UPDATE " + tablePrefix + "_review_rate SET helpfulornot = ? WHERE review_id = ? AND user_id = ?";
      
      final PreparedStatement preparedStatement = DBConnector.prepareStatement(sqlInsertHelpfulReview);
      final PreparedStatement preparedStatement2 = DBConnector.prepareStatement(updateUserReviewNew);
      try {
    	  connection.setAutoCommit(false);
          preparedStatement.setBoolean(1, review.getStatus());
          preparedStatement.setInt(2, Integer.parseInt(review.getId()));
          preparedStatement.setInt(3, user_id);
          
          try {
				int i = preparedStatement.executeUpdate();
				if(i != 1){
					throw new SQLException("Unable to update " + tablePrefix + "_review_rate" +
                            " for user_id: " + user_id + " and record ");
				}
			} catch (SQLException e) {
					ServiceLogger.log(logTag, "Unable to update for user user_id:" +  user_id);
					connection.rollback();
					throw new DMCServiceException(DMCError.OtherSQLError, "Unable to update " + tablePrefix + "_review_rate" + e.getMessage());
				}

          preparedStatement2.setString(1, review.getComment());
          preparedStatement2.setInt(2, review.getRating());
          preparedStatement2.setInt(3, reviewIdIndex);
          try {
				int i = preparedStatement2.executeUpdate();
				if(i != 1){
					throw new SQLException("Unable to update " + tablePrefix + "_review_new" +
                          " for user_id: " + user_id + " and record ");
				}
			} catch (SQLException e) {
					ServiceLogger.log(logTag, "Unable to create follow company");
					connection.rollback();
					throw new DMCServiceException(DMCError.OtherSQLError, "Unable to update " + tablePrefix + "_review_new" + e.getMessage());
				}
          
      } catch (SQLException sqlEX) {
 
    	  ServiceLogger.log(logTag, sqlEX.getMessage());
			try {
				connection.rollback();
			} catch (SQLException e1) {
				ServiceLogger.log(logTag, e1.getMessage());
				throw new DMCServiceException(DMCError.OtherSQLError, "unable to rollback: " + e1.getMessage());
			}
			throw new DMCServiceException(DMCError.OtherSQLError, sqlEX.getMessage());
		} finally {
			if (connection != null) {
				try {
					connection.setAutoCommit(true);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
      return review;
	}
	
	
	public static void main(String[] args){
		ReviewDao reviewDao = new ReviewDao(ReviewType.PROFILE);
		/*List<ProfileReview> reviews = reviewDao.getReviewByReviewId("1", "userEPPN20161017174331187", ProfileReview.class);
		System.out.println("The review number should be 1 without replies: " +  reviews.size());
		System.out.println("Print out the results : " +  reviews.toString());
		List<ProfileReview> reviews2 = reviewDao.getReviewByReviewId("3", "userEPPN20161017174332394", ProfileReview.class);
		System.out.println("The review number should be 3 with 2 replies: " +  reviews2.size());
		System.out.println("Print out the results : " +  reviews2.toString());*/
		
		
		List<ProfileReview> reviews = reviewDao.getAllReviews(19, "0", null, null, null, null, null, "userEPPN20161017174331187", ProfileReview.class);
		System.out.println("The review number should be 2 without replies: " +  reviews.size());
		System.out.println("Print out the results : " +  reviews.toString());
	}
	
}
