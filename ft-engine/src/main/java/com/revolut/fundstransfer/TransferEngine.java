package com.revolut.fundstransfer;

import com.revolut.app.rest.fundstransfer.service.AccountRestService;
import com.revolut.app.rest.fundstransfer.service.FundsTransferRestService;
import com.revolut.app.rest.fundstransfer.service.GenericExceptionMapper;
import com.revolut.app.rest.fundstransfer.service.RevolutExceptionMapper;
import com.revolut.fundstransfer.tools.H2SchemaGenerator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A standalone funds transfer engine with in memory H2 DB and communication interface exposed as Rest Services
 */
public class TransferEngine {

	private static Logger log = Logger.getLogger(TransferEngine.class.getName());

	public TransferEngine() {
        H2SchemaGenerator.generate();
    }

	public void start() throws Exception {
	    Server server = startInMemoryWebServer();
	    server.join();
	}

	public Server startInMemoryWebServer() throws Exception{
        Server server = new Server(7777);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);
        log.log(Level.INFO,"########Starting server...");
        ServletHolder servletHolder = context.addServlet(ServletContainer.class, "/*");
        servletHolder.setInitParameter("jersey.config.server.provider.classnames",
                AccountRestService.class.getCanonicalName() + ","
                        + FundsTransferRestService.class.getCanonicalName() + ","
                        + RevolutExceptionMapper.class.getCanonicalName() + ","
                        + GenericExceptionMapper.class.getCanonicalName());
        server.start();
        log.log(Level.INFO,"######Server Started...");

        return server;
    }

    public static void main(String[] args) throws Exception {
        new TransferEngine().start();
    }
}
