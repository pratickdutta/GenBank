package api;

import com.sun.net.httpserver.HttpExchange;
import dao.AccountDAO;
import model.Account;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * AccountHandler — GET /api/account?accountNumber=XXXXXXXXXX
 *
 * Returns the current account info (balance, status, etc.) for a given account number.
 */
public class AccountHandler extends BaseHandler {

    private final AccountDAO accountDAO = new AccountDAO();

    @Override
    public void handle(HttpExchange ex) throws IOException {
        if (handleOptions(ex)) return;

        if (!"GET".equals(ex.getRequestMethod())) {
            sendError(ex, 405, "Method not allowed");
            return;
        }

        String query = ex.getRequestURI().getQuery();
        String accountNumber = null;
        if (query != null) {
            for (String param : query.split("&")) {
                String[] kv = param.split("=", 2);
                if ("accountNumber".equals(kv[0]) && kv.length == 2) {
                    accountNumber = kv[1];
                }
            }
        }

        if (accountNumber == null || accountNumber.isBlank()) {
            sendError(ex, 400, "accountNumber query parameter is required");
            return;
        }

        try {
            Account account = accountDAO.findByAccountNumber(accountNumber);
            if (account == null) {
                sendError(ex, 404, "Account not found: " + accountNumber);
                return;
            }

            Map<String, Object> data = new HashMap<>();
            data.put("accountId",     account.getAccountId());
            data.put("accountNumber", account.getAccountNumber());
            data.put("accountType",   account.getAccountType().name());
            data.put("balance",       account.getBalance());
            data.put("customerName",  account.getCustomerName());
            data.put("status",        account.getStatus().name());

            sendSuccess(ex, data);

        } catch (SQLException e) {
            sendError(ex, 500, "Database error: " + e.getMessage());
        }
    }
}
