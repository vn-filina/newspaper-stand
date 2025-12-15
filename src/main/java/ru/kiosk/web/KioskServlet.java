package ru.kiosk.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.kiosk.config.AppConfig;
import ru.kiosk.db.ConnectionFactory;
import ru.kiosk.db.DriverManagerConnectionFactory;
import ru.kiosk.repository.InventoryRepository;
import ru.kiosk.repository.JdbcInventoryRepository;
import ru.kiosk.service.InventoryService;
import ru.kiosk.dto.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@WebServlet(name = "KioskServlet", urlPatterns = {"/api/*"})
public class KioskServlet extends HttpServlet {

    private transient InventoryService service;
    private transient Gson gson;

    @Override
    public void init() throws ServletException {
        try {
            String url = AppConfig.jdbcUrl(); // ENV KIOSK_JDBC_URL или -Dkiosk.jdbc.url или fallback в data/
            ConnectionFactory cf = new DriverManagerConnectionFactory(url);
            InventoryRepository repo = new JdbcInventoryRepository(cf);
            this.service = new InventoryService(repo);
            this.gson = new GsonBuilder().disableHtmlEscaping().create();
        } catch (Exception e) {
            throw new ServletException("Failed to init KioskServlet", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");

        String path = path(req);
        switch (path) {
            case "/products" -> {
                List<String> all = service.describeAll(); // уже готовые human-строки
                writeJson(resp, all);
            }
            default -> notFound(resp, "GET " + path + " not found");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");

        String path = path(req);
        String body = readBody(req);

        switch (path) {
            case "/receipt" -> {
                ReceiptRequest r = gson.fromJson(body, ReceiptRequest.class);
                var result = service.receipt(r);
                writeJson(resp, result);
            }
            case "/sale" -> {
                SaleRequest r = gson.fromJson(body, SaleRequest.class);
                var result = service.sale(r);
                writeJson(resp, result);
            }
            case "/update" -> {
                UpdateRequest r = gson.fromJson(body, UpdateRequest.class);
                var result = service.update(r);
                writeJson(resp, result);
            }
            default -> notFound(resp, "POST " + path + " not found");
        }
    }

    private static String path(HttpServletRequest req) {
        String p = req.getPathInfo();
        return (p == null || p.isBlank()) ? "/" : p;
    }

    private String readBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = req.getReader()) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
        }
        return sb.toString();
    }

    private void writeJson(HttpServletResponse resp, Object obj) throws IOException {
        String json = gson.toJson(obj);
        resp.getOutputStream().write(json.getBytes(StandardCharsets.UTF_8));
    }

    private void notFound(HttpServletResponse resp, String msg) throws IOException {
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        writeJson(resp, new ProductResponse(false, msg));
    }
}
