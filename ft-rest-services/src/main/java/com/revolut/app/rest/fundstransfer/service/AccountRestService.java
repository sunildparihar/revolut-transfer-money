package com.revolut.app.rest.fundstransfer.service;

import com.revolut.app.rest.fundstransfer.mapper.AccountMapper;
import com.revolut.app.rest.fundstransfer.model.Account;
import com.revolut.app.rest.fundstransfer.model.TransactionRequest;
import com.revolut.core.fundstransfer.gateway.ServicesGateway;
import com.revolut.sdk.fundstransfer.services.AccountService;
import com.revolut.sdk.fundstransfer.exception.ServiceException;
import com.revolut.sdk.fundstransfer.model.AccountVO;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/account")
@Produces(MediaType.APPLICATION_JSON)
public class AccountRestService {

    /*
        propagate through service gateway to delegate the call to core business layer
     */
    private final ServicesGateway servicesGateway = ServicesGateway.getServicesGateway();

    @GET
    @Path("/list")
    public List<Account> getAllAccounts() throws ServiceException {
        return (List) servicesGateway.pass(AccountService.class, "getAllAccounts");
    }

    @GET
    @Path("/{accountId}")
    public Account getAccount(@PathParam("accountId") long accountId) throws ServiceException {

        //calling below for ServiceGateway testing purpose, should be ignored
        AccountMapper.convertFromVO((AccountVO) servicesGateway.pass(AccountService.class, "getAccount", accountId, true));

        return AccountMapper.convertFromVO((AccountVO) servicesGateway.pass(AccountService.class, "getAccount", accountId));
    }

    @POST
    @Path("/withdraw")
    public Response withdrawFromAccount(TransactionRequest transactionRequest) throws ServiceException {
        servicesGateway.pass(
                AccountService.class, "withdrawFromAccount", transactionRequest.getAccountId(), transactionRequest.getTransactionAmount());
        return Response.status(Response.Status.OK).build();
    }

    @POST
    @Path("/deposit")
    public Response depositToAccount(TransactionRequest transactionRequest) throws ServiceException {
        servicesGateway.pass(
                AccountService.class, "depositToAccount", transactionRequest.getAccountId(), transactionRequest.getTransactionAmount());
        return Response.status(Response.Status.OK).build();
    }
}
