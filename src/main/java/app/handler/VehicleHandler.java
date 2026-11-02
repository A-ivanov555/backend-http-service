package app.handler;

import app.dto.RentRequest;
import app.dto.VehicleRequest;
import app.exception.InvalidOperationException;
import app.exception.NotFoundException;
import app.model.Car;
import app.model.Scooter;
import app.model.Vehicle;
import app.service.RentalService;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;
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
        try {
            route(exchange);
        } catch (NotFoundException e) {
            sendError(exchange, 404, e.getMessage());
        } catch (InvalidOperationException e) {
            sendError(exchange, 409, e.getMessage());
        } catch (IllegalArgumentException e) {
            sendError(exchange, 400, e.getMessage());
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            sendError(exchange, 500, "Внутренняя ошибка сервера");
        }
    }

    private void route(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        if (path.equals("/vehicles")) {
            switch (method) {
                case "GET" -> getVehicles(exchange);
                case "POST" -> createVehicle(exchange);
                default -> sendError(
                        exchange,
                        405,
                        "HTTP-метод не поддерживается"
                );
            }

            return;
        }

        if (!path.startsWith("/vehicles/")) {
            sendError(exchange, 404, "Маршрут не найден");
            return;
        }

        String remaining =
                path.substring("/vehicles/".length());

        if (remaining.isBlank()) {
            sendError(exchange, 404, "Маршрут не найден");
            return;
        }

        String[] parts = remaining.split("/");

        if (parts.length == 1) {
            int id = parseId(parts[0]);

            switch (method) {
                case "GET" -> getVehicle(exchange, id);
                case "PUT" -> updateVehicle(exchange, id);
                case "DELETE" -> deleteVehicle(exchange, id);
                default -> sendError(
                        exchange,
                        405,
                        "HTTP-метод не поддерживается"
                );
            }

            return;
        }

        if (parts.length == 2) {
            int id = parseId(parts[0]);

            if (parts[1].equals("rent")) {
                if (!method.equals("POST")) {
                    sendError(
                            exchange,
                            405,
                            "HTTP-метод не поддерживается"
                    );
                    return;
                }

                rentVehicle(exchange, id);
                return;
            }

            if (parts[1].equals("return")) {
                if (!method.equals("POST")) {
                    sendError(
                            exchange,
                            405,
                            "HTTP-метод не поддерживается"
                    );
                    return;
                }

                returnVehicle(exchange, id);
                return;
            }
        }

        sendError(exchange, 404, "Маршрут не найден");
    }

    private void getVehicles(HttpExchange exchange)
            throws IOException {

        String query = exchange.getRequestURI().getRawQuery();

        Boolean available = null;
        String type = null;

        if (query != null && !query.isBlank()) {
            for (String parameter : query.split("&")) {
                String[] parts = parameter.split("=", 2);

                if (parts.length != 2) {
                    throw new IllegalArgumentException(
                            "Некорректный параметр фильтрации"
                    );
                }

                String key = URLDecoder.decode(
                        parts[0],
                        StandardCharsets.UTF_8
                );

                String value = URLDecoder.decode(
                        parts[1],
                        StandardCharsets.UTF_8
                );

                switch (key) {
                    case "available" -> {
                        if (value.equalsIgnoreCase("true")) {
                            available = true;
                        } else if (value.equalsIgnoreCase("false")) {
                            available = false;
                        } else {
                            throw new IllegalArgumentException(
                                    "available должен быть true или false"
                            );
                        }
                    }

                    case "type" -> type = value;

                    default -> throw new IllegalArgumentException(
                            "Неизвестный параметр фильтрации"
                    );
                }
            }
        }

        ArrayList<Vehicle> vehicles =
                rentalService.getVehicles(available, type);

        sendJson(exchange, 200, vehicles);
    }

    private void getVehicle(
            HttpExchange exchange,
            int id
    ) throws IOException {

        Vehicle vehicle = rentalService.getVehicle(id);

        sendJson(exchange, 200, vehicle);
    }

    private void createVehicle(HttpExchange exchange)
            throws IOException {

        VehicleRequest request =
                readJson(exchange, VehicleRequest.class);

        if (request.id == null
                || request.type == null
                || request.model == null
                || request.pricePerHour == null) {

            throw new IllegalArgumentException(
                    "Не указаны обязательные поля транспорта"
            );
        }

        Vehicle vehicle = switch (
                request.type.toLowerCase(Locale.ROOT)
                ) {
            case "car" -> new Car(
                    request.id,
                    request.model,
                    request.pricePerHour
            );

            case "scooter" -> new Scooter(
                    request.id,
                    request.model,
                    request.pricePerHour
            );

            default -> throw new IllegalArgumentException(
                    "Неизвестный тип транспорта"
            );
        };

        rentalService.addVehicle(vehicle);

        sendJson(exchange, 201, vehicle);
    }

    private void updateVehicle(
            HttpExchange exchange,
            int id
    ) throws IOException {

        VehicleRequest request =
                readJson(exchange, VehicleRequest.class);

        if (request.model == null
                || request.pricePerHour == null) {

            throw new IllegalArgumentException(
                    "Не указаны model или pricePerHour"
            );
        }

        rentalService.updateVehicle(
                id,
                request.model,
                request.pricePerHour
        );

        sendJson(
                exchange,
                200,
                rentalService.getVehicle(id)
        );
    }

    private void deleteVehicle(
            HttpExchange exchange,
            int id
    ) throws IOException {

        rentalService.deleteVehicle(id);

        sendNoContent(exchange);
    }

    private void rentVehicle(
            HttpExchange exchange,
            int id
    ) throws IOException {

        RentRequest request =
                readJson(exchange, RentRequest.class);

        if (request.customerId == null) {
            throw new IllegalArgumentException(
                    "Не указан customerId"
            );
        }

        rentalService.rentVehicle(
                id,
                request.customerId
        );

        sendJson(
                exchange,
                200,
                rentalService.getVehicle(id)
        );
    }

    private void returnVehicle(
            HttpExchange exchange,
            int id
    ) throws IOException {

        rentalService.returnVehicle(id);

        sendJson(
                exchange,
                200,
                rentalService.getVehicle(id)
        );
    }

    private int parseId(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Некорректный id транспорта"
            );
        }
    }

    private <T> T readJson(
            HttpExchange exchange,
            Class<T> type
    ) {
        try {
            return objectMapper.readValue(
                    exchange.getRequestBody(),
                    type
            );
        } catch (JacksonException e) {
            throw new IllegalArgumentException(
                    "Некорректный JSON"
            );
        }
    }

    private void sendJson(
            HttpExchange exchange,
            int statusCode,
            Object data
    ) throws IOException {

        String response =
                objectMapper.writeValueAsString(data);

        byte[] responseBytes =
                response.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set(
                "Content-Type",
                "application/json; charset=UTF-8"
        );

        exchange.sendResponseHeaders(
                statusCode,
                responseBytes.length
        );

        try (OutputStream responseBody =
                     exchange.getResponseBody()) {

            responseBody.write(responseBytes);
        }
    }

    private void sendError(
            HttpExchange exchange,
            int statusCode,
            String message
    ) throws IOException {

        sendJson(
                exchange,
                statusCode,
                Map.of("error", message)
        );
    }

    private void sendNoContent(HttpExchange exchange)
            throws IOException {

        exchange.sendResponseHeaders(204, -1);
        exchange.close();
    }
}