package com.revolut.fundstransfer.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revolut.app.rest.fundstransfer.model.Account;
import com.revolut.app.rest.fundstransfer.model.ErrorResponse;
import com.revolut.app.rest.fundstransfer.model.TransactionRequest;
import com.revolut.app.rest.fundstransfer.model.TransferRequest;
import com.revolut.core.fundstransfer.persist.conn.ConnectionHelper;
import com.revolut.core.fundstransfer.persist.to.BankAccountTO;
import org.apache.commons.dbutils.DbUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertTrue;

/**
 * This is integration testing for FundsTransferService Rest APIs
 * Please refer resources/prepareh2.sql for test data
 */
public class FundsTransferServiceTest extends FTServiceTest {

    private static final int THREAD_COUNT = 1000;

    private static final String SELECT_FOR_UPDATE = "SELECT * FROM BANK_ACCOUNT WHERE BANK_ACCOUNT_ID = ? FOR UPDATE";

   /*
       test case for funds transfer positive scenario i.e. with required balance in source account
    */
    @Test
    public void testFundsTransfer() throws IOException, URISyntaxException {

        Account sourceAccount = getAccountById(3L);
        Account destinationAccount = getAccountById(4L);

        BigDecimal transferAmount = new BigDecimal(100L);
        transfer(sourceAccount.getAccountId(), destinationAccount.getAccountId(), transferAmount);

        Account updatedSourceAccount = getAccountById(sourceAccount.getAccountId());
        Account updatedDestinationAccount = getAccountById(destinationAccount.getAccountId());

        //check if correct amount is deducted from source account
        assertTrue(sourceAccount.getBalance().subtract(transferAmount).longValue() == updatedSourceAccount.getBalance().longValue());

        //check if correct amount is credited to destination account
        assertTrue(destinationAccount.getBalance().add(transferAmount).longValue() == updatedDestinationAccount.getBalance().longValue());
    }

    /*
        test case for funds transfer negative scenario i.e. source account with insufficient funds
     */
    @Test
    public void testFundsTransferInsufficientFunds() throws IOException, URISyntaxException {

        Account sourceAccount = getAccountById(3L);

        BigDecimal transferAmount = sourceAccount.getBalance().add(new BigDecimal(100L));
        TransferRequest transferRequest = new TransferRequest(transferAmount, sourceAccount.getAccountId(), 4L);

        String responseJsonBody = testPost("/transfer", transferRequest, 500);

        ErrorResponse errorResponse = new ObjectMapper().readValue(responseJsonBody, ErrorResponse.class);
        assertTrue(errorResponse.getErrorCode().equals("105"));
    }

    /*
      test funds transfer with 1000 parallel threads to test server concurrency control.
      First deposit 100000 bucks into source account to make sure enough balance.
      The same amount is supposed to be debited from source account by 1000 threads, each withdrawing 100 bucks
      The same amount is supposed to be credited to destination account by 1000 threads, each crediting 100 bucks
    */
    @Test
    public void testTransferMultiThreaded() throws IOException, URISyntaxException, InterruptedException {

        Account sourceAccountBeforeUpdate = getAccountById(3L);
        Account destinationAccountBeforeUpdate = getAccountById(4L);

        BigDecimal depositAmount = new BigDecimal(100000000000L);
        Account sourceAccountAfterDeposit = depositMoney(sourceAccountBeforeUpdate.getAccountId(), depositAmount);
        assertTrue(sourceAccountBeforeUpdate.getBalance().add(depositAmount).longValue() == sourceAccountAfterDeposit.getBalance().longValue());

        BigDecimal perThreadTransferAmount = new BigDecimal(100L);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        for (int i = 1; i <= THREAD_COUNT; i++) {
            new Thread( ()-> {
                try {
                    testPost("/transfer",
                            new TransferRequest(perThreadTransferAmount,
                                    sourceAccountBeforeUpdate.getAccountId(),
                                    destinationAccountBeforeUpdate.getAccountId())
                    );
                } catch (Exception e) {
                    System.out.println("#######Error occurred while transfer: "+e.getMessage());
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        latch.await();

        Account sourceAccountAfterTransfer = getAccountById(sourceAccountBeforeUpdate.getAccountId());
        Account destinationAccountAfterTransfer = getAccountById(destinationAccountBeforeUpdate.getAccountId());
        BigDecimal totalTransferAmount = perThreadTransferAmount.multiply(new BigDecimal(THREAD_COUNT));

        assertTrue(sourceAccountAfterDeposit.getBalance().subtract(totalTransferAmount).longValue() == sourceAccountAfterTransfer.getBalance().longValue());
        assertTrue(destinationAccountBeforeUpdate.getBalance().add(totalTransferAmount).longValue() == destinationAccountAfterTransfer.getBalance().longValue());
    }

    /*
      test funds transfer with 300 parallel threads to perform transfer, withdraw and deposit concurrently between two accounts.
    */
    @Test
    public void testTransfer_Withdraw_Deposit_AllTogether_MultiThreaded() throws IOException, URISyntaxException, InterruptedException {

        Account sourceAccountBeforeUpdate = getAccountById(3L);
        Account destinationAccountBeforeUpdate = getAccountById(4L);

        BigDecimal depositAmount = new BigDecimal(1000000000L);
        Account sourceAccountAfterDeposit = depositMoney(sourceAccountBeforeUpdate.getAccountId(), depositAmount);
        assertTrue(sourceAccountBeforeUpdate.getBalance().add(depositAmount).longValue() == sourceAccountAfterDeposit.getBalance().longValue());

        Account destinationAccountAfterDeposit = depositMoney(destinationAccountBeforeUpdate.getAccountId(), depositAmount);
        assertTrue(destinationAccountBeforeUpdate.getBalance().add(depositAmount).longValue() == destinationAccountAfterDeposit.getBalance().longValue());

        BigDecimal perThreadTransferAmount = new BigDecimal(10L);
        BigDecimal perThreadWithdrawalAmount = new BigDecimal(12L); //to be withdraw from destination account
        BigDecimal perThreadDepositAmount = new BigDecimal(14L); //to be deposit into source account

        int threadCount = 100;
        CountDownLatch latch1 = new CountDownLatch(threadCount);
        for (int i = 1; i <= threadCount; i++) {
            new Thread( ()-> {
                try {
                    testPost("/transfer",
                            new TransferRequest(perThreadTransferAmount,
                                    sourceAccountBeforeUpdate.getAccountId(),
                                    destinationAccountBeforeUpdate.getAccountId())
                    );
                } catch (Exception e) {
                    System.out.println("#######Error occurred while transfer: "+e.getMessage());
                } finally {
                    latch1.countDown();
                }
            }).start();
        }
        latch1.await();

        CountDownLatch latch2 = new CountDownLatch(threadCount);
        for (int i = 1; i <= threadCount; i++) {
            new Thread( ()-> {
                try {
                    testPost("/account/withdraw", new TransactionRequest(destinationAccountBeforeUpdate.getAccountId(), perThreadWithdrawalAmount));
                } catch (Exception e) {
                    System.out.println("#######Error occurred while transfer: "+e.getMessage());
                } finally {
                    latch2.countDown();
                }
            }).start();
        }
        latch2.await();

        CountDownLatch latch3 = new CountDownLatch(threadCount);
        for (int i = 1; i <= threadCount; i++) {
            new Thread( ()-> {
                try {
                    testPost("/account/deposit", new TransactionRequest(sourceAccountBeforeUpdate.getAccountId(), perThreadDepositAmount));
                } catch (Exception e) {
                    System.out.println("#######Error occurred while transfer: "+e.getMessage());
                } finally {
                    latch3.countDown();
                }
            }).start();
        }
        latch3.await();

        Account sourceAccountAfterTest = getAccountById(sourceAccountBeforeUpdate.getAccountId());
        Account destinationAccountAfterTest = getAccountById(destinationAccountBeforeUpdate.getAccountId());

        BigDecimal sourceAccountInitialBalance = sourceAccountAfterDeposit.getBalance();
        BigDecimal destinationAccountInitialBalance = destinationAccountAfterDeposit.getBalance();

        BigDecimal totalTransferAmount = perThreadTransferAmount.multiply(new BigDecimal(threadCount));
        BigDecimal totalWithdrawAmount = perThreadWithdrawalAmount.multiply(new BigDecimal(threadCount));
        BigDecimal totalDepositAmount = perThreadDepositAmount.multiply(new BigDecimal(threadCount));

        BigDecimal expectedSourceAccBalance = sourceAccountInitialBalance.add(totalDepositAmount).subtract(totalTransferAmount);
        BigDecimal expectedDestinationAccBalance = destinationAccountInitialBalance.add(totalTransferAmount).subtract(totalWithdrawAmount);

        assertTrue(expectedSourceAccBalance.longValue() == sourceAccountAfterTest.getBalance().longValue());
        assertTrue(expectedDestinationAccBalance.longValue() == destinationAccountAfterTest.getBalance().longValue());
    }

    /*
       test case for funds transfer with a special scenario when there is an error while crediting into destination account.
       The transfer should not be succeeded and source account should be credited back with transfer amount. This can be achieved by locking the destination
       account row in DB.
    */
    @Test
    public void testFundsTransferWithLockedDestinationAccountCreditFailed() throws IOException, URISyntaxException, SQLException {

        Account sourceAccount = getAccountById(3L);
        Account destinationAccount = getAccountById(4L);

        try (Connection connection = ConnectionHelper.createNewConnection()){

            connection.setAutoCommit(false);
            lockAccount(connection, destinationAccount.getAccountId());

            BigDecimal transferAmount = new BigDecimal(100L);
            TransferRequest transferRequest = new TransferRequest(transferAmount, sourceAccount.getAccountId(), destinationAccount.getAccountId());
            testPost("/transfer", transferRequest, 500);

            Account updatedSourceAccount = getAccountById(sourceAccount.getAccountId());
            Account updatedDestinationAccount = getAccountById(destinationAccount.getAccountId());

            //check if no amount is deducted from source account
            assertTrue(sourceAccount.getBalance().longValue() == updatedSourceAccount.getBalance().longValue());

            //check if no amount is credited to destination account
            assertTrue(destinationAccount.getBalance().longValue() == updatedDestinationAccount.getBalance().longValue());
        }

    }

    private void lockAccount(Connection connection, long accountId) throws SQLException{
        PreparedStatement lockStatement = null;
        ResultSet rs = null;
        BankAccountTO account = null;
        try {
            lockStatement = connection.prepareStatement(SELECT_FOR_UPDATE);
            lockStatement.setLong(1, accountId);
            rs = lockStatement.executeQuery();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(lockStatement);
        }
    }

}
