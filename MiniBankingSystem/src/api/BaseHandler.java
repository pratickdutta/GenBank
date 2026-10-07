package api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * BaseHandler — shared utilities for all REST API handlers.
 *
 * Provides JSON parsing, response writing, CORS headers, and error helpers.
 */
public abstract class BaseHandler implements HttpHandler {

    protected static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    // ── CORS + response helpers ────────────────────────────────────────────

    protected void addCorsHeaders(HttpExchange ex) {
        ex.getResponseHeaders().add("Access-Control-Allow-Origin",  "*");
        ex.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        ex.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
        ex.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
    }

    protected void sendJson(HttpExchange ex, int statusCode, Object body) throws IOException {
        String json = GSON.toJson(body);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        addCorsHeaders(ex);
        ex.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    protected void sendSuccess(HttpExchange ex, Object data) throws IOException {
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("data", data);
        sendJson(ex, 200, resp);
    }

    protected void sendError(HttpExchange ex, int code, String message) throws IOException {
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", false);
        resp.put("error", message);
        sendJson(ex, code, resp);
    }

    protected String readBody(HttpExchange ex) throws IOException {
        try (InputStream is = ex.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @SuppressWarnings("unchecked")
    protected Map<String, String> parseBody(HttpExchange ex) throws IOException {
        String body = readBody(ex);
        return GSON.fromJson(body, Map.class);
    }

    protected boolean handleOptions(HttpExchange ex) throws IOException {
        if ("OPTIONS".equals(ex.getRequestMethod())) {
            addCorsHeaders(ex);
            ex.sendResponseHeaders(204, -1);
            return true;
        }
        return false;
    }
}
