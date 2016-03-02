package org.dmc.services.accounts;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import org.dmc.services.ServiceLogger;

import javax.validation.*;
import javax.xml.ws.http.HTTPException;
import org.springframework.http.HttpStatus;

import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.*;

@Controller
@RequestMapping(value = "/accounts", produces = {APPLICATION_JSON_VALUE})
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringMVCServerCodegen", date = "2016-02-22T14:57:06.776Z")
public class AccountsController {
    
    private final String logTag = AccountsController.class.getName();
    private AccountsDao accounts = new AccountsDao();
    
    /**
     
     **/
    @RequestMapping(value = "/{accountID}",
                    produces = { "application/json"},
                    method = RequestMethod.GET)
    public ResponseEntity<UserAccount> accountsAccountIDGet(@PathVariable("accountID") String accountID,
                                                            @RequestHeader(value="AJP_eppn", defaultValue="testUser") String userEPPN)
    {
        ServiceLogger.log(logTag, "accountsAccountIDGet, accountID: " + accountID);
        
        int httpStatusCode = HttpStatus.OK.value();
        UserAccount userAccount = null;
        
        try{
            userAccount = accounts.getUserAccount(accountID, userEPPN);
        } catch(HTTPException httpException) {
            httpStatusCode = httpException.getStatusCode();
        }
        
        return new ResponseEntity<UserAccount>(userAccount, HttpStatus.valueOf(httpStatusCode));
    }
    
    
    /**
     
     **/
    @RequestMapping(value = "/{accountID}",
                    produces = { "application/json" },
                    method = RequestMethod.PATCH)
    public ResponseEntity<UserAccount> accountsAccountIDPatch(@PathVariable("accountID") String accountID,
                                                              @RequestBody UserAccount account,
                                                              @RequestHeader(value="AJP_eppn", defaultValue="testUser") String userEPPN) {
        ServiceLogger.log(logTag, "accountsAccountIDPatch, accountID: " + accountID + ", account.id: " + account.getId() + ", userEPPN: " + userEPPN);
        
        int httpStatusCode = HttpStatus.OK.value();
        UserAccount userAccount = null;
        
        try {
            userAccount = accounts.patchUserAccount(accountID, account, userEPPN);
        } catch(HTTPException httpException) {
            httpStatusCode = httpException.getStatusCode();
        }
        return new ResponseEntity<UserAccount>(userAccount, HttpStatus.valueOf(httpStatusCode));
    }
    
    
    /**
     
     **/
    @RequestMapping(value = "/{accountID}/account-notification-settings",
                    produces = { "application/json" },
                    method = RequestMethod.GET)
    public ResponseEntity<List<AccountNotificationSetting>> accountsAccountIDAccountNotificationSettingsGet(@PathVariable("accountID") String accountID) {
        // do some magic!
        return new ResponseEntity<List<AccountNotificationSetting>>(HttpStatus.OK);
    }
    
    
    /**
     
     **/
    @RequestMapping(value = "/{accountID}/account_servers",
                    produces = { "application/json" },
                    method = RequestMethod.GET)
    public ResponseEntity<List<UserAccountServer>> accountsAccountIDAccountServersGet(@PathVariable("accountID") String accountID) {
        // do some magic!
        return new ResponseEntity<List<UserAccountServer>>(HttpStatus.OK);
    }
    
    
    /**
     
     **/
    @RequestMapping(value = "/{accountID}/favorite_products",
                    produces = { "application/json" },
                    method = RequestMethod.GET)
    public ResponseEntity<List<InlineResponse200>> accountsAccountIDFavoriteProductsGet(@PathVariable("accountID") String accountID,
                                                                                        @RequestParam(value = "expand", required = true) List<String> expand,
                                                                                        @RequestParam(value = "limit", required = false) Integer limit,
                                                                                        @RequestParam(value = "order", required = false) String order,
                                                                                        @RequestParam(value = "start", required = false) Integer start,
                                                                                        @RequestParam(value = "sort", required = false) String sort,
                                                                                        @RequestParam(value = "title", required = false) String title,
                                                                                        @RequestParam(value = "type", required = false) String type,
                                                                                        @RequestParam(value = "authors", required = false) List<Integer> authors,
                                                                                        @RequestParam(value = "ratings", required = false) List<String> ratings,
                                                                                        @RequestParam(value = "favorites", required = false) String favorites,
                                                                                        @RequestParam(value = "dates", required = false) List<String> dates)
    {
        // do some magic!
        return new ResponseEntity<List<InlineResponse200>>(HttpStatus.OK);
    }
    
    
    /**
     
     **/
    @RequestMapping(value = "/{accountID}/following_companies",
                    produces = { "application/json" }, 
                    method = RequestMethod.GET)
    public ResponseEntity<List<FollowingCompany>> accountsAccountIDFollowingCompaniesGet(@PathVariable("accountID") String accountID)
    {
        // do some magic!
        return new ResponseEntity<List<FollowingCompany>>(HttpStatus.OK);
    }
    
    
}