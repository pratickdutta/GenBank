package api;

import com.sun.net.httpserver.HttpExchange;
import model.Transaction;
import service.BankingService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TransactionsHandler — GET /api/transactions?accountId=123
 *
 * Returns a list of all transactions for the given account, newest first.
 */
public class TransactionsHandler extends BaseHandler {

    private final BankingService bankingService = new BankingService();

    @Override
    public void handle(HttpExchange ex) throws IOException {
        if (handleOptions(ex)) return;

        if (!"GET".equals(ex.getRequestMethod())) {
            sendError(ex, 405, "Method not allowed");
            return;
        }

        String query = ex.getRequestURI().getQuery();
        String accountIdStr = null;
        if (query != null) {
            for (String param : query.split("&")) {
                String[] kv = param.split("=", 2);
                if ("accountId".equals(kv[0]) && kv.length == 2) {
                    accountIdStr = kv[1];
                }
            }
        }

        if (accountIdStr == null) {
            sendError(ex, 400, "accountId query parameter is required");
            return;
        }

        int accountId;
        try {
            accountId = Integer.parseInt(accountIdStr);
        } catch (NumberFormatException e) {
            sendError(ex, 400, "accountId must be an integer");
            return;
        }

        try {
            List<Transaction> txList = bankingService.getTransactionHistory(accountId);
            List<Map<String, Object>> result = new ArrayList<>();

            for (Transaction tx : txList) {
                Map<String, Object> m = new HashMap<>();
                m.put("transactionId",   tx.getTransactionId());
                m.put("transactionType", tx.getTransactionType().name());
                m.put("amount",          tx.getAmount());
                m.put("description",     tx.getDescription());
                m.put("status",          tx.getStatus().name());
                m.put("transactionDate", tx.getTransactionDate() != null
                        ? tx.getTransactionDate().toString() : null);
                m.put("referenceId",     tx.getReferenceId());
                result.add(m);
            }

            sendSuccess(ex, result);

        } catch (SQLException e) {
            sendError(ex, 500, "Database error: " + e.getMessage());
        }
    }
}
