package com.revolut.app.rest.fundstransfer.service;

import com.revolut.app.rest.fundstransfer.mapper.TransferRequestMapper;
import com.revolut.app.rest.fundstransfer.model.TransferRequest;
import com.revolut.core.fundstransfer.gateway.ServicesGateway;
import com.revolut.sdk.fundstransfer.services.FundsTransferService;
import com.revolut.sdk.fundstransfer.exception.ServiceException;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/transfer")
@Produces(MediaType.APPLICATION_JSON)
public class FundsTransferRestService {

    /*
        propagate through service gateway to delegate the call to core business layer
     */
    private final ServicesGateway servicesGateway = ServicesGateway.getServicesGateway();

	@POST
	public Response transferFunds(TransferRequest transferRequest) throws ServiceException {
        servicesGateway.pass(FundsTransferService.class, "transferFunds", TransferRequestMapper.convertIntoVO(transferRequest));
        return Response.status(Response.Status.OK).build();
	}
}
