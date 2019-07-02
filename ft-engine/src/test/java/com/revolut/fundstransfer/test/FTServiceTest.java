package com.revolut.fundstransfer.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revolut.app.rest.fundstransfer.model.Account;
import com.revolut.app.rest.fundstransfer.model.TransactionRequest;
import com.revolut.app.rest.fundstransfer.model.TransferRequest;
import com.revolut.fundstransfer.TransferEngine;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.eclipse.jetty.server.Server;
import org.junit.After;
import org.junit.Before;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.assertTrue;

public abstract class FTServiceTest {

    private HttpClient client ;
    private Server server;
    private final URIBuilder uriBuilder = new URIBuilder().setScheme("http").setHost("localhost:7777");
    private final int timeout = 120000;

    @Before
    public void startServer() throws Exception {
//        System.out.println("############ TEST####### start server");
        server = new TransferEngine().startInMemoryWebServer();
        client= HttpClients.createDefault();
/*        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(timeout)
                .setConnectionRequestTimeout(timeout)
                .setConnectTimeout(timeout)
                .build();

        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
        connManager.setMaxTotal(1200);
        connManager.setDefaultMaxPerRoute(1200);

        httpClientBuilder.setConnectionManager(connManager);
        httpClientBuilder.setConnectionManagerShared(true);
//        httpClientBuilder.setDefaultRequestConfig(requestConfig);

        client = httpClientBuilder.build();*/
    }

    @After
    public void stopServer() throws Exception {
//        System.out.println("############ TEST####### stop server");
        HttpClientUtils.closeQuietly(client);
        server.stop();
        server.destroy();
    }

    protected String testGet(String url) throws URISyntaxException, IOException {
        URI uri = uriBuilder.setPath(url).build();
        HttpGet request = new HttpGet(uri);
        HttpResponse response = client.execute(request);
        String responseJsonBody = EntityUtils.toString(response.getEntity());
        return responseJsonBody;
    }

    protected String testGet(String url, int expectedStatusCode) throws URISyntaxException, IOException {
        URI uri = uriBuilder.setPath(url).build();
        HttpGet request = new HttpGet(uri);
        HttpResponse response = client.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        assertTrue(statusCode == expectedStatusCode);
        String responseJsonBody = EntityUtils.toString(response.getEntity());
        return responseJsonBody;
    }

    protected String testPost(String url, Object body, int expectedStatusCode) throws URISyntaxException, IOException {
        URI uri = uriBuilder.setPath(url).build();
        String reqJsonBody = new ObjectMapper().writeValueAsString(body);
        HttpPost request = new HttpPost(uri);
        request.setHeader("Content-type", "application/json");
        request.setEntity(new StringEntity(reqJsonBody));
        HttpResponse response = client.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        assertTrue(statusCode == expectedStatusCode);
        String responseJsonBody = EntityUtils.toString(response.getEntity());
        return responseJsonBody;
    }

    protected String testPost(String url, Object body) throws URISyntaxException, IOException {
        URI uri = uriBuilder.setPath(url).build();
        String reqJsonBody = new ObjectMapper().writeValueAsString(body);
        HttpPost request = new HttpPost(uri);
        request.setHeader("Content-type", "application/json");
        request.setEntity(new StringEntity(reqJsonBody));
        HttpResponse response = client.execute(request);
        String responseJsonBody = EntityUtils.toString(response.getEntity());
        return responseJsonBody;
    }

    protected Account depositMoney(long accountId, BigDecimal depositAmount) throws IOException, URISyntaxException {
        String url = "/account/deposit";
        testPost(url, new TransactionRequest(accountId, depositAmount));
        String updatedAccountJson = testGet("/account/"+accountId);
        Account accountAfterDeposit = new ObjectMapper().readValue(updatedAccountJson, Account.class);
        return  accountAfterDeposit;
    }

    protected Account depositMoney(long accountId, BigDecimal depositAmount, int expectedStatusCode) throws IOException, URISyntaxException {
        String url = "/account/deposit";
        testPost(url, new TransactionRequest(accountId, depositAmount), expectedStatusCode);
        String updatedAccountJson = testGet("/account/"+accountId);
        Account accountAfterDeposit = new ObjectMapper().readValue(updatedAccountJson, Account.class);
        return  accountAfterDeposit;
    }

    protected Account withdrawMoney(long accountId, BigDecimal withdrawalAmount) throws IOException, URISyntaxException {
        String url = "/account/withdraw";
        testPost(url, new TransactionRequest(accountId, withdrawalAmount));
        String updatedAccountJson = testGet("/account/"+accountId);
        return new ObjectMapper().readValue(updatedAccountJson, Account.class);
    }

    protected Account withdrawMoney(long accountId, BigDecimal withdrawalAmount, int expectedStatusCode) throws IOException, URISyntaxException {
        String url = "/account/withdraw";
        testPost(url, new TransactionRequest(accountId, withdrawalAmount), expectedStatusCode);
        String updatedAccountJson = testGet("/account/"+accountId);
        return new ObjectMapper().readValue(updatedAccountJson, Account.class);
    }

    protected Account getAccountById(long accountId) throws IOException, URISyntaxException {
        String accountJson = testGet("/account/"+accountId);
        return new ObjectMapper().readValue(accountJson, Account.class);
    }

    protected Account getAccountById(long accountId, int expectedStatusCode) throws IOException, URISyntaxException {
        String accountJson = testGet("/account/"+accountId, expectedStatusCode);
        return new ObjectMapper().readValue(accountJson, Account.class);
    }

    protected void transfer(long sourceAccId, long destinationAccId, BigDecimal transferAmount) throws IOException, URISyntaxException {
        Account sourceAccount = getAccountById(3L);
        Account destinationAccount = getAccountById(4L);
        TransferRequest transferRequest = new TransferRequest(transferAmount, sourceAccount.getAccountId(), destinationAccount.getAccountId());
        testPost("/transfer", transferRequest, 200);
    }
}
