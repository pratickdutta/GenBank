package api;

import com.sun.net.httpserver.HttpExchange;
import util.DBConnection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HealthHandler extends BaseHandler {

    private static final Logger LOG = Logger.getLogger(HealthHandler.class.getName());

    @Override
    public void handle(HttpExchange ex) throws IOException {
        if (handleOptions(ex)) return;

        if (!"/health".equals(ex.getRequestURI().getPath())) {
            sendError(ex, 404, "Not found");
            return;
        }

        if (!"GET".equals(ex.getRequestMethod())) {
            ex.getResponseHeaders().set("Allow", "GET, OPTIONS");
            sendError(ex, 405, "Method not allowed");
            return;
        }

        try (Connection connection = DBConnection.getConnection()) {
            if (!connection.isValid(2)) {
                throw new SQLException("Health check database connection is invalid.");
            }
            sendJson(ex, 200, Map.of("status", "ok"));
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Health check database connection failed.", e);
            sendJson(ex, 503, Map.of("status", "unavailable"));
        }
    }
}
