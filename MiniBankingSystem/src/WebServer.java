import api.*;
import com.sun.net.httpserver.HttpServer;
import util.DBConnection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * WebServer — entry point for the Mini Banking System web application.
 *
 * Starts an embedded HTTP server on port 8080 and serves:
 *   - Static frontend files (HTML/CSS/JS) from the /web resource directory
 *   - REST API endpoints under /api/*
 *
 * No Tomcat, no Spring Boot — just Java's built-in com.sun.net.httpserver.
 */
public class WebServer {

    private static final int    PORT   = 8080;
    private static final Logger LOG    = Logger.getLogger(WebServer.class.getName());

    public static void main(String[] args) throws IOException {

        // Trigger schema initialisation (happens in DBConnection static block)
        LOG.info("[BOOT] Initialising database...");
        try {
            // Force static initializer
            Class.forName("util.DBConnection");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // ── API endpoints ──────────────────────────────────────────────────────
        server.createContext("/api/register",     new RegisterHandler());
        server.createContext("/api/login",        new LoginHandler());
        server.createContext("/api/account",      new AccountHandler());
        server.createContext("/api/deposit",      new DepositHandler());
        server.createContext("/api/withdraw",      new WithdrawHandler());
        server.createContext("/api/transfer",     new TransferHandler());
        server.createContext("/api/transactions", new TransactionsHandler());

        // ── Static file server (serves HTML/CSS/JS from web/ resources) ───────
        server.createContext("/", new StaticFileHandler());

        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();

        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║    GenBank Mini Banking System — Web Server      ║");
        System.out.println("╠══════════════════════════════════════════════════╣");
        System.out.printf( "║  Server running at: http://localhost:%d          ║%n", PORT);
        System.out.println("║  Database: H2 Embedded (no MySQL needed!)        ║");
        System.out.println("║  Press Ctrl+C to stop.                           ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
    }
}
