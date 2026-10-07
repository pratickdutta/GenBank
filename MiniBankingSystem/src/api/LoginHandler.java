package api;

import com.sun.net.httpserver.HttpExchange;
import model.Account;
import service.AuthService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * LoginHandler — POST /api/login
 *
 * Expected JSON body:
 * { "email": "john@example.com", "password": "secret123" }
 *
 * Returns account details on success (used as the session token for subsequent requests).
 */
public class LoginHandler extends BaseHandler {

    private final AuthService authService = new AuthService();

    @Override
    public void handle(HttpExchange ex) throws IOException {
        if (handleOptions(ex)) return;

        if (!"POST".equals(ex.getRequestMethod())) {
            sendError(ex, 405, "Method not allowed");
            return;
        }

        try {
            Map<String, String> body = parseBody(ex);

            Account account = authService.login(
                    body.get("email"),
                    body.get("password")
            );

            Map<String, Object> data = new HashMap<>();
            data.put("accountId",     account.getAccountId());
            data.put("accountNumber", account.getAccountNumber());
            data.put("accountType",   account.getAccountType().name());
            data.put("balance",       account.getBalance());
            data.put("customerName",  account.getCustomerName());
            data.put("status",        account.getStatus().name());
            data.put("customerId",    account.getCustomerId());

            sendSuccess(ex, data);

        } catch (IllegalArgumentException e) {
            sendError(ex, 401, e.getMessage());
        } catch (SQLException e) {
            sendError(ex, 500, "Database error: " + e.getMessage());
        }
    }
}
