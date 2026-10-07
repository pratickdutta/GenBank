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
 * WithdrawHandler — POST /api/withdraw
 *
 * Expected JSON body:
 * { "accountNumber": "1012345678", "amount": "2000.00" }
 */
public class WithdrawHandler extends BaseHandler {

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
            String accountNumber = body.get("accountNumber");
            String amountStr     = body.get("amount");

            if (accountNumber == null || amountStr == null) {
                sendError(ex, 400, "accountNumber and amount are required");
                return;
            }

            BigDecimal amount;
            try {
                amount = new BigDecimal(amountStr);
            } catch (NumberFormatException e) {
                sendError(ex, 400, "Invalid amount format");
                return;
            }

            Account account = accountDAO.findByAccountNumber(accountNumber);
            if (account == null) {
                sendError(ex, 404, "Account not found");
                return;
            }

            bankingService.withdraw(account, amount);

            Map<String, Object> data = new HashMap<>();
            data.put("newBalance",    account.getBalance());
            data.put("accountNumber", account.getAccountNumber());
            data.put("message",       String.format("Withdrawn ₹%,.2f successfully", amount));

            sendSuccess(ex, data);

        } catch (IllegalArgumentException e) {
            sendError(ex, 400, e.getMessage());
        } catch (SQLException e) {
            sendError(ex, 500, "Database error: " + e.getMessage());
        }
    }
}
