package api;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * StaticFileHandler — serves the HTML/CSS/JS frontend from the web/ resource directory.
 *
 * Maps URL paths to classpath resources under the "web/" prefix.
 * Example: GET / → classpath:web/index.html
 */
public class StaticFileHandler extends BaseHandler {

    private static final Map<String, String> MIME_TYPES = new HashMap<>();

    static {
        MIME_TYPES.put("html", "text/html; charset=UTF-8");
        MIME_TYPES.put("css",  "text/css; charset=UTF-8");
        MIME_TYPES.put("js",   "application/javascript; charset=UTF-8");
        MIME_TYPES.put("ico",  "image/x-icon");
        MIME_TYPES.put("png",  "image/png");
        MIME_TYPES.put("svg",  "image/svg+xml");
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        if (handleOptions(ex)) return;

        URI uri      = ex.getRequestURI();
        String path  = uri.getPath();

        // Default to index.html
        if (path.equals("/") || path.isEmpty()) {
            path = "/index.html";
        }

        // Security: prevent path traversal
        if (path.contains("..")) {
            sendPlain(ex, 403, "Forbidden");
            return;
        }

        String resourcePath = "web" + path;
        InputStream is = StaticFileHandler.class
                .getClassLoader()
                .getResourceAsStream(resourcePath);

        if (is == null) {
            // Fallback: serve index.html for SPA routing
            is = StaticFileHandler.class
                    .getClassLoader()
                    .getResourceAsStream("web/index.html");
            if (is == null) {
                sendPlain(ex, 404, "index.html not found on classpath");
                return;
            }
            path = "/index.html";
        }

        String ext      = getExtension(path);
        String mimeType = MIME_TYPES.getOrDefault(ext, "application/octet-stream");

        byte[] bytes = is.readAllBytes();
        is.close();

        ex.getResponseHeaders().set("Content-Type", mimeType);
        ex.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String getExtension(String path) {
        int dot = path.lastIndexOf('.');
        return (dot >= 0) ? path.substring(dot + 1).toLowerCase() : "";
    }

    private void sendPlain(HttpExchange ex, int code, String msg) throws IOException {
        byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "text/plain");
        ex.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }
}
