package app;

import app.handler.CustomerHandler;
import app.handler.VehicleHandler;
import app.service.RentalService;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {
    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(
                new InetSocketAddress(PORT),
                0
        );

        RentalService rentalService = new RentalService();

        server.createContext(
                "/vehicles",
                new VehicleHandler(rentalService)
        );

        server.createContext(
                "/customers",
                new CustomerHandler(rentalService)
        );

        server.createContext(
                "/ping",
                new PingHandler()
        );

        server.setExecutor(null);
        server.start();

        System.out.println(
                "Сервер запущен: http://localhost:" + PORT
        );
    }
}