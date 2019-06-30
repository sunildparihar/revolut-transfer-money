package com.revolut.fundstransfer.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revolut.app.rest.fundstransfer.model.Account;
import com.revolut.app.rest.fundstransfer.model.ErrorResponse;
import com.revolut.app.rest.fundstransfer.model.TransactionRequest;
import org.eclipse.jetty.util.StringUtil;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.assertTrue;

/**
 * This is integration testing for AccountService Rest APIs
 * Please refer resources/prepareh2.sql for test data
 */
public class AccountServiceTest extends FTServiceTest {

    private static final int THREAD_COUNT = 1000;

    /*
        test get all accounts, total 5 accounts
    */
    @Test
    public void testGetAllAccounts() throws IOException, URISyntaxException {
        String responseJsonBody = testGet("/account/list", 200);
        Account[] accounts = new ObjectMapper().readValue(responseJsonBody, Account[].class);
        assertTrue(accounts.length == 5);
    }

    /*
        test get account by account Id, the account which exists
     */
    @Test
    public void testGetExistingAccountById() throws IOException, URISyntaxException {
        Account account = getAccountById(4L, 200);
        assertTrue(account.getAccountId().equals(4L));
    }

    /*
       test get account by account Id, the account which doesn't exists
    */
    @Test
    public void testGetUnknownAccountById() throws IOException, URISyntaxException {
        String responseJsonBody = testGet("/account/7", 500);
        ErrorResponse errorResponse = new ObjectMapper().readValue(responseJsonBody, ErrorResponse.class);
        assertTrue(errorResponse.getErrorCode().equals("202"));
    }

    /*
      test withdrawal with sufficient funds
    */
    @Test
    public void testWithDraw() throws IOException, URISyntaxException {
        Account account = getAccountById(3L);
        BigDecimal withdrawalAmount = new BigDecimal(100L);
        Account updatedAccount = withdrawMoney(3L, withdrawalAmount, 200);
        assertTrue(account.getBalance().subtract(withdrawalAmount).longValue() == updatedAccount.getBalance().longValue());
    }

    /*
       test withdrawal with sufficient funds
    */
    @Test
    public void testWithDrawInSufficientFund() throws IOException, URISyntaxException {
        Account account = getAccountById(3L);
        BigDecimal withdrawalAmount = account.getBalance().add(new BigDecimal(100L));
        String responseJsonBody = testPost("/account/withdraw", new TransactionRequest(account.getAccountId(), withdrawalAmount), 500);
        ErrorResponse errorResponse = new ObjectMapper().readValue(responseJsonBody, ErrorResponse.class);
        assertTrue(errorResponse.getErrorCode().equals("203"));
    }

    /*
       test deposit
    */
    @Test
    public void testDeposit() throws IOException, URISyntaxException {
        Account account = getAccountById(3L);
        BigDecimal depositAmount = new BigDecimal(100L);
        Account updatedAccount = depositMoney(account.getAccountId(), depositAmount, 200);
        assertTrue(account.getBalance().add(depositAmount).longValue() == updatedAccount.getBalance().longValue());
    }

    /*
      test deposit with 1000 parallel threads to test server concurrency control.
    */
    @Test
    public void testDepositMultiThreaded() throws IOException, URISyntaxException, InterruptedException {

        Account accountBeforeUpdate = getAccountById(3L);

        BigDecimal perThreadDepositAmount = new BigDecimal(100L);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        for (int i = 1; i <= THREAD_COUNT; i++) {
            new Thread( ()-> {
                try {
                    testPost("/account/deposit", new TransactionRequest(accountBeforeUpdate.getAccountId(), perThreadDepositAmount));
                } catch (Exception e) {
                    System.out.println("#######Error occurred while deposit: "+e.getMessage());
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        latch.await();

        Account accountAfterDeposit = getAccountById(accountBeforeUpdate.getAccountId());
        BigDecimal totalDepositAmount = perThreadDepositAmount.multiply(new BigDecimal(THREAD_COUNT));

        assertTrue(accountBeforeUpdate.getBalance().add(totalDepositAmount).longValue() == accountAfterDeposit.getBalance().longValue());
    }

    /*
      test withdrawal with 1000 parallel threads to test server concurrency control.
      First deposit 100000 bucks to make sure enough balance.
      The same amount is supposed be deducted by 1000 threads, each withdrawing 100 bucks
    */
    @Test
    public void testWithdrawMultiThreaded() throws IOException, URISyntaxException, InterruptedException {

        Account accountBeforeUpdate = getAccountById(3L);

        BigDecimal depositAmount = new BigDecimal(100000000000L);
        Account accountAfterDeposit = depositMoney(accountBeforeUpdate.getAccountId(), depositAmount);
        assertTrue(accountBeforeUpdate.getBalance().add(depositAmount).longValue() == accountAfterDeposit.getBalance().longValue());

        BigDecimal perThreadWithdrawalAmount = new BigDecimal(100L);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        for (int i = 1; i <= THREAD_COUNT; i++) {
            new Thread( ()-> {
                    try {
                        testPost("/account/withdraw", new TransactionRequest(accountBeforeUpdate.getAccountId(), perThreadWithdrawalAmount));
                    } catch (Exception e) {
                        System.out.println("#######Error occurred while withdrawal: "+e.getMessage());
                    } finally {
                        latch.countDown();
                    }
            }).start();
        }

        latch.await();

        Account accountAfterWithdraw = getAccountById(accountBeforeUpdate.getAccountId());
        BigDecimal totalWithdrawalAmount = perThreadWithdrawalAmount.multiply(new BigDecimal(THREAD_COUNT));

        assertTrue(accountAfterDeposit.getBalance().subtract(totalWithdrawalAmount).longValue() == accountAfterWithdraw.getBalance().longValue());
    }

    /*
      Test withdrawal with 1000 parallel threads to test server concurrency control with some threads failing due to zero balance.
      First make account balance to zero by withdrawing entire amount.
      Then deposit 65,000 bucks so that exact balance in the account is 65,000.
      Start 1000 threads each withdrawing 100 bucks.
      Exactly 650 threads should succeed and remaining 350 threads should fail.
    */
    @Test
    public void testWithdrawMultiThreadedWithSomeThreadsFail() throws IOException, URISyntaxException, InterruptedException, ExecutionException {

        Account accountBeforeUpdate = getAccountById(3L);

        Account accountZeroBalance = withdrawMoney(accountBeforeUpdate.getAccountId(), accountBeforeUpdate.getBalance());
        assertTrue(accountZeroBalance.getBalance().longValue() == 0);

        BigDecimal depositAmount = new BigDecimal(65000L);
        Account accountAfterDeposit = depositMoney(accountBeforeUpdate.getAccountId(), depositAmount);
        assertTrue(accountAfterDeposit.getBalance().longValue() == depositAmount.longValue());

        BigDecimal perThreadWithdrawalAmount = new BigDecimal(100L);
        int successThreadCount = 0;
        List<Callable<Integer>> callableList = new ArrayList<>();
        for (int i = 1; i <= THREAD_COUNT; i++) {
            callableList.add(()-> {
                try {
                    String responseJson = testPost("/account/withdraw", new TransactionRequest(accountBeforeUpdate.getAccountId(), perThreadWithdrawalAmount));
                    if(StringUtil.isNotBlank(responseJson)) {
                        ErrorResponse errorResponse = new ObjectMapper().readValue(responseJson, ErrorResponse.class);
                        if(!Objects.isNull(errorResponse)) {
                            return 0;
                        } else {
                            return 1;
                        }
                    } else {
                        return 1;
                    }
                } catch (Exception e) {
                    System.out.println("#######Error occurred while withdrawal: "+e.getMessage());
                    return 1;
                }
            });
        }

        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        List<Future<Integer>> futureList = executorService.invokeAll(callableList);

        for(Future<Integer> integerFuture : futureList) {
            successThreadCount += integerFuture.get();
        }

        int expectedSuccessfulThreadCount = Math.min(THREAD_COUNT, depositAmount.divide(perThreadWithdrawalAmount).intValue());
        assertTrue(successThreadCount == expectedSuccessfulThreadCount);

        Account accountAfterWithdraw = getAccountById(accountBeforeUpdate.getAccountId());
        long availableBalance = depositAmount.subtract(perThreadWithdrawalAmount.multiply(new BigDecimal(successThreadCount))).intValue();
        assertTrue(accountAfterWithdraw.getBalance().longValue() == availableBalance);
    }

}
