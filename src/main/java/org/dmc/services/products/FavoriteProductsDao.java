package org.dmc.services.products;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.ListIterator;
import java.util.ArrayList;

import org.dmc.services.DBConnector;
import org.dmc.services.DMCError;
import org.dmc.services.DMCServiceException;
import org.dmc.services.ServiceLogger;
import org.dmc.services.data.dao.user.UserDao;
import org.dmc.services.sharedattributes.Util;


public class FavoriteProductsDao {

	private final String logTag = FavoriteProductsDao.class.getName();
	
    public FavoriteProduct createFavoriteProduct(Integer accountId, Integer serviceId, String userEPPN) throws DMCServiceException {
        Util util = Util.getInstance();
        int id = -1;
        int userId = getUserId(userEPPN);
        
        if(accountId.intValue() != userId) {
            throw new DMCServiceException(DMCError.OtherSQLError, "User " + userEPPN + " id does not match the accountId " + accountId);
        }
        
        String sqlInsertFavoriteProduct = "INSERT INTO favorite_products(account_id, service_id) VALUES (?,?)";
        
        try {
            PreparedStatement preparedStatement = null;
            // Insert into organization_review_reply
            preparedStatement = DBConnector.prepareStatement(sqlInsertFavoriteProduct, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setInt(1, accountId);
            preparedStatement.setInt(2, serviceId);
            
            int rCreate = preparedStatement.executeUpdate();
        
            id = util.getGeneratedKey(preparedStatement, "id");
            ServiceLogger.log(logTag, "Created Favorite Product: " + id);
            
        } catch (SQLException sqlEx) {
            // if it is a duplicate we'll continue without error,
            // other errors will fail
            if (!sqlEx.getMessage().contains("duplicate key")) {
                throw new DMCServiceException(DMCError.OtherSQLError, sqlEx.toString());
            }
        }
        
        FavoriteProduct favoriteProduct = new FavoriteProduct();
        favoriteProduct.setId(Integer.toString(id));
        favoriteProduct.setAccountId(accountId.toString());
        favoriteProduct.setServiceId(serviceId.toString());
        
        return favoriteProduct;
    }

    public void deleteFavoriteProduct(Integer favoriteProductId, String userEPPN) throws DMCServiceException {
        ServiceLogger.log(logTag, "In deleteFavoriteProduct " + favoriteProductId + " for userEPPN: " + userEPPN);
        Util util = Util.getInstance();
        int userId = getUserId(userEPPN);
        
        String sqlDeleteFavoriteProduct = "DELETE FROM favorite_products WHERE account_id = ? AND id = ?";
        
        PreparedStatement preparedStatement = DBConnector.prepareStatement(sqlDeleteFavoriteProduct);
        try {
            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, favoriteProductId);
            int change = preparedStatement.executeUpdate();
            if(change != 1) {
                throw new DMCServiceException(DMCError.OtherSQLError, "number of changes were: " + change); // single item not deleted
            }
            
        } catch (SQLException e) {
            ServiceLogger.log(logTag, e.getMessage());
            throw new DMCServiceException(DMCError.OtherSQLError, e.getMessage());
        }
        return;
    }
    
    public List<FavoriteProduct> getFavoriteProductForAccounts(List<Integer> accountIds, String userEPPN) throws DMCServiceException {
        return getFavoriteProductForAccounts(accountIds, null, null, null, userEPPN);
    }
    
    public List<FavoriteProduct> getFavoriteProductForAccounts(List<Integer> accountIds, Integer limit, String order, String sort, String userEPPN) throws DMCServiceException {
        return getFavoriteProduct(accountIds, limit, order, sort, userEPPN, "account_id");
    }
    
    public List<FavoriteProduct> getFavoriteProductForServices(List<Integer> serviceIds, String userEPPN) throws DMCServiceException {
        return getFavoriteProductForServices(serviceIds, null, null, null, userEPPN);
    }
    
    private List<FavoriteProduct> getFavoriteProductForServices(List<Integer> serviceIds, Integer limit, String order, String sort, String userEPPN) throws DMCServiceException {
        return getFavoriteProduct(serviceIds, limit, order, sort, userEPPN, "service_id");
    }

    
    private List<FavoriteProduct> getFavoriteProduct(List<Integer> ids, Integer limit, String order, String sort, String userEPPN, String column) {
        ListIterator<Integer> iterator = ids.listIterator();
        ArrayList<FavoriteProduct> favoriteProducts = new ArrayList<FavoriteProduct>();

        
        //int userId = getUserId(userEPPN);
        // need to check is user_id can see accountId's favorite products
        
        while(iterator.hasNext()) {
            Integer id = iterator.next();
            
            String sqlSelectFavoriteProduct = "SELECT * FROM favorite_products WHERE " + column + " = ?";
            
            // ADD limit clause
            // ADD order clause
            // ADD sort clause

            PreparedStatement preparedStatement = DBConnector.prepareStatement(sqlSelectFavoriteProduct);
            
                try {
                    preparedStatement.setInt(1, id);
                    final ResultSet resultSet = preparedStatement.executeQuery();
                
                    while(resultSet.next()) {
                        FavoriteProduct favoriteProduct = new FavoriteProduct();
                        favoriteProduct.setId(Integer.toString(resultSet.getInt("id")));
                        favoriteProduct.setAccountId(Integer.toString(resultSet.getInt("account_id")));
                        favoriteProduct.setServiceId(Integer.toString(resultSet.getInt("service_id")));
                        favoriteProducts.add(favoriteProduct);
                    }
                
                } catch (SQLException e) {
                    ServiceLogger.log(logTag, e.getMessage());
                    throw new DMCServiceException(DMCError.OtherSQLError, e.getMessage());
                }
        }
        
    
        return favoriteProducts;
    }
    
    
    int getUserId(String userEPPN){
        try {
            return UserDao.getUserID(userEPPN);
        } catch(SQLException sqlEx) {
            throw new DMCServiceException(DMCError.UnknownUser, sqlEx.toString());
        }
    }
}
