package api;

import com.sun.net.httpserver.HttpExchange;
import dao.AccountDAO;
import model.Account;
import service.BankingService;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * TransferHandler — POST /api/transfer
 *
 * Expected JSON body:
 * {
 *   "senderAccountNumber": "1012345678",
 *   "receiverAccountNumber": "1087654321",
 *   "amount": "1000.00"
 * }
 */
public class TransferHandler extends BaseHandler {

    private final AccountDAO     accountDAO     = new AccountDAO();
    private final BankingService bankingService = new BankingService();

    @Override
    public void handle(HttpExchange ex) throws IOException {
        if (handleOptions(ex)) return;

        if (!"POST".equals(ex.getRequestMethod())) {
            sendError(ex, 405, "Method not allowed");
            return;
        }

        try {
            Map<String, String> body = parseBody(ex);
            String senderNumber   = body.get("senderAccountNumber");
            String receiverNumber = body.get("receiverAccountNumber");
            String amountStr      = body.get("amount");

            if (senderNumber == null || receiverNumber == null || amountStr == null) {
                sendError(ex, 400, "senderAccountNumber, receiverAccountNumber and amount are required");
                return;
            }

            BigDecimal amount;
            try {
                amount = new BigDecimal(amountStr);
            } catch (NumberFormatException e) {
                sendError(ex, 400, "Invalid amount format");
                return;
            }

            Account senderAccount = accountDAO.findByAccountNumber(senderNumber);
            if (senderAccount == null) {
                sendError(ex, 404, "Sender account not found");
                return;
            }

            // BankingService.transfer() handles its own JDBC transaction
            bankingService.transfer(senderAccount, receiverNumber, amount);

            Map<String, Object> data = new HashMap<>();
            data.put("newBalance",    senderAccount.getBalance());
            data.put("accountNumber", senderAccount.getAccountNumber());
            data.put("message",       String.format("Transferred ₹%,.2f to %s successfully", amount, receiverNumber));

            sendSuccess(ex, data);

        } catch (IllegalArgumentException e) {
            sendError(ex, 400, e.getMessage());
        } catch (SQLException e) {
            sendError(ex, 500, "Database error: " + e.getMessage());
        }
    }
}
