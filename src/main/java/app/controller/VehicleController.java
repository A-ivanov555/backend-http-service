
package app.controller;

import jakarta.validation.Valid;
import app.dto.RentRequest;
import app.dto.VehicleRequest;
import app.model.Car;
import app.model.Scooter;
import app.model.Vehicle;
import app.service.RentalService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/vehicles")
public class VehicleController {

    private final RentalService rentalService;

    public VehicleController(RentalService rentalService) {
        this.rentalService = rentalService;
    }

    @GetMapping
    public List<Vehicle> getVehicles(
            @RequestParam(required = false) Boolean available,
            @RequestParam(required = false) String type
    ) {
        return rentalService.getVehicles(available, type);
    }

    @GetMapping("/{id}")
    public Vehicle getVehicle(@PathVariable int id) {
        return rentalService.getVehicle(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Vehicle createVehicle(@Valid @RequestBody VehicleRequest request) {
        if (request == null
                || request.id == null
                || request.type == null
                || request.model == null
                || request.pricePerHour == null) {
            throw new IllegalArgumentException(
                    "Не указаны обязательные поля транспорта"
            );
        }

        Vehicle vehicle = switch (request.type.toLowerCase(Locale.ROOT)) {
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
        return vehicle;
    }

    @PutMapping("/{id}")
    public Vehicle updateVehicle(
            @PathVariable int id,
            @Valid @RequestBody VehicleRequest request
    ) {
        if (request == null
                || request.model == null
                || request.pricePerHour == null) {
            throw new IllegalArgumentException(
                    "Не указаны обязательные поля транспорта"
            );
        }

        return rentalService.updateVehicle(
                id,
                request.model,
                request.pricePerHour
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteVehicle(@PathVariable int id) {
        rentalService.deleteVehicle(id);
    }

    @PostMapping("/{id}/rent")
    public Vehicle rentVehicle(
            @PathVariable int id,
            @Valid @RequestBody RentRequest request
    ) {
        if (request == null || request.customerId == null) {
            throw new IllegalArgumentException(
                    "Не указан id клиента"
            );
        }

        rentalService.rentVehicle(id, request.customerId);
        return rentalService.getVehicle(id);
    }

    @PostMapping("/{id}/return")
    public Vehicle returnVehicle(@PathVariable int id) {
        rentalService.returnVehicle(id);
        return rentalService.getVehicle(id);
    }

    @GetMapping("/{id}/price")
    public Map<String, Double> getRentalPrice(
            @PathVariable int id,
            @RequestParam int hours
    ) {
        double price = rentalService.getRentalPrice(id, hours);
        return Map.of("totalPrice", price);
    }
}
