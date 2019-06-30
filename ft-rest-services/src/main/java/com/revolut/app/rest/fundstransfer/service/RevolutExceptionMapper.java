package com.revolut.app.rest.fundstransfer.service;

import com.revolut.app.rest.fundstransfer.model.ErrorResponse;
import com.revolut.sdk.fundstransfer.exception.ServiceException;
import org.eclipse.jetty.util.StringUtil;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

@Provider
public class RevolutExceptionMapper implements ExceptionMapper<ServiceException> {
	private static Logger log = Logger.getLogger(RevolutExceptionMapper.class.getName());

	public Response toResponse(ServiceException serviceException) {
		ErrorResponse errorResponse = new ErrorResponse();
		String errorCode = serviceException.getReasonCode();
        if (StringUtil.isBlank(errorCode)) {
            errorCode = "999";
        }
		errorResponse.setErrorCode(errorCode);
		errorResponse.setErrorMessage(serviceException.getMessage());
        log.log(Level.INFO,"Returning Error Response ..." + errorResponse);
		return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(errorResponse)
                .type(MediaType.APPLICATION_JSON)
                .build();
	}
}
