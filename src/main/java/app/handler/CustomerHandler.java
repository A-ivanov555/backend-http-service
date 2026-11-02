package app.handler;

import app.dto.CustomerRequest;
import app.exception.InvalidOperationException;
import app.exception.NotFoundException;
import app.model.Customer;
import app.service.RentalService;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class CustomerHandler implements HttpHandler {
    private final RentalService rentalService;
    private final ObjectMapper objectMapper;

    public CustomerHandler(RentalService rentalService) {
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

    private void route(HttpExchange exchange)
            throws IOException {

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        if (path.equals("/customers")) {
            switch (method) {
                case "GET" -> sendJson(
                        exchange,
                        200,
                        rentalService.getAllCustomers()
                );

                case "POST" -> createCustomer(exchange);

                default -> sendError(
                        exchange,
                        405,
                        "HTTP-метод не поддерживается"
                );
            }

            return;
        }

        if (!path.startsWith("/customers/")) {
            sendError(exchange, 404, "Маршрут не найден");
            return;
        }

        String idPart =
                path.substring("/customers/".length());

        if (idPart.isBlank() || idPart.contains("/")) {
            sendError(exchange, 404, "Маршрут не найден");
            return;
        }

        int id = parseId(idPart);

        switch (method) {
            case "GET" -> sendJson(
                    exchange,
                    200,
                    rentalService.getCustomer(id)
            );

            case "PUT" -> updateCustomer(exchange, id);

            case "DELETE" -> {
                rentalService.deleteCustomer(id);
                sendNoContent(exchange);
            }

            default -> sendError(
                    exchange,
                    405,
                    "HTTP-метод не поддерживается"
            );
        }
    }

    private void createCustomer(HttpExchange exchange)
            throws IOException {

        CustomerRequest request =
                readJson(exchange, CustomerRequest.class);

        if (request.id == null
                || request.name == null
                || request.age == null) {

            throw new IllegalArgumentException(
                    "Не указаны обязательные поля клиента"
            );
        }

        Customer customer = new Customer(
                request.id,
                request.name,
                request.age
        );

        rentalService.addCustomer(customer);

        sendJson(exchange, 201, customer);
    }

    private void updateCustomer(
            HttpExchange exchange,
            int id
    ) throws IOException {

        CustomerRequest request =
                readJson(exchange, CustomerRequest.class);

        if (request.name == null || request.age == null) {
            throw new IllegalArgumentException(
                    "Не указаны name или age"
            );
        }

        rentalService.updateCustomer(
                id,
                request.name,
                request.age
        );

        sendJson(
                exchange,
                200,
                rentalService.getCustomer(id)
        );
    }

    private int parseId(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Некорректный id клиента"
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