package api;

import com.sun.net.httpserver.HttpExchange;
import model.Account;
import service.AuthService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * RegisterHandler — POST /api/register
 *
 * Expected JSON body:
 * {
 *   "name": "John Doe",
 *   "email": "john@example.com",
 *   "phone": "9876543210",
 *   "address": "123 Main St",
 *   "dob": "15-06-1995",
 *   "password": "secret123",
 *   "accountType": "SAVINGS"
 * }
 */
public class RegisterHandler extends BaseHandler {

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

            Account account = authService.register(
                    body.get("name"),
                    body.get("email"),
                    body.get("phone"),
                    body.get("address"),
                    body.get("dob"),
                    body.get("password"),
                    body.getOrDefault("accountType", "SAVINGS")
            );

            Map<String, Object> data = new HashMap<>();
            data.put("accountId",     account.getAccountId());
            data.put("accountNumber", account.getAccountNumber());
            data.put("accountType",   account.getAccountType().name());
            data.put("balance",       account.getBalance());
            data.put("customerName",  account.getCustomerName());
            data.put("status",        account.getStatus().name());

            sendSuccess(ex, data);

        } catch (IllegalArgumentException e) {
            sendError(ex, 400, e.getMessage());
        } catch (SQLException e) {
            sendError(ex, 500, "Database error: " + e.getMessage());
        }
    }
}
