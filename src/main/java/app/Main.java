package app;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import app.service.RentalService;
import app.handler.VehicleHandler;
import app.model.Car;
public class Main {

    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        RentalService rentalService = new RentalService();
        rentalService.addVehicle(new Car(1, "BMW", 500));
        server.createContext("/vehicles", new VehicleHandler(rentalService));

        // Каждый контекст — это префикс пути и обработчик для него.
        // Внимание: контекст "/ping" получит и запросы на "/ping/что-угодно".
        server.createContext("/ping", new PingHandler());

        // TODO: зарегистрируйте здесь обработчики своих ресурсов

        server.start();
        System.out.println("Сервер запущен: http://localhost:" + PORT + "/ping");
    }
}
