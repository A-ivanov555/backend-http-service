package app.handler;

import app.exception.NotFoundException;
import app.model.Vehicle;
import app.service.RentalService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;

public class VehicleHandler implements HttpHandler {
    private final RentalService rentalService;
    private final ObjectMapper objectMapper;

    public VehicleHandler(RentalService rentalService) {
        this.rentalService = rentalService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            if (method.equals("GET") && path.equals("/vehicles")) {
                ArrayList<Vehicle> vehicles = rentalService.getAllVehicles();
                sendJson(exchange, 200, vehicles);
                return;
            }

            if (method.equals("GET") && path.startsWith("/vehicles/")) {
                String idPart = path.substring("/vehicles/".length());
                int id = Integer.parseInt(idPart);

                Vehicle vehicle = rentalService.getVehicle(id);
                sendJson(exchange, 200, vehicle);
                return;
            }

            sendError(exchange, 405, "Метод не поддерживается");
        } catch (NumberFormatException e) {
            sendError(exchange, 400, "Некорректный id транспорта");
        } catch (NotFoundException e) {
            sendError(exchange, 404, e.getMessage());
        }
    }

    private void sendJson(HttpExchange exchange, int statusCode, Object data)
            throws IOException {

        String response = objectMapper.writeValueAsString(data);
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders()
                .set("Content-Type", "application/json; charset=UTF-8");

        exchange.sendResponseHeaders(statusCode, responseBytes.length);

        try (OutputStream responseBody = exchange.getResponseBody()) {
            responseBody.write(responseBytes);
        }
    }

    private void sendError(HttpExchange exchange, int statusCode, String message)
            throws IOException {

        Map<String, String> error = Map.of("error", message);
        sendJson(exchange, statusCode, error);
    }
}