
package app.service;

import app.exception.InvalidOperationException;
import app.exception.NotFoundException;
import app.model.Car;
import app.model.Customer;
import app.model.Scooter;
import app.repository.CustomerRepository;
import app.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RentalServiceTest {

    private RentalService rentalService;

    @BeforeEach
    void setUp() {
        rentalService = new RentalService(
                new VehicleRepository(),
                new CustomerRepository()
        );
    }

    @Test
    void shouldCreateAndFindVehicle() {
        Car car = new Car(1, "BMW", 500);

        rentalService.addVehicle(car);

        assertSame(car, rentalService.getVehicle(1));
        assertTrue(car.isAvailable());
    }

    @Test
    void shouldRejectDuplicateVehicleId() {
        rentalService.addVehicle(new Car(1, "BMW", 500));

        assertThrows(
                InvalidOperationException.class,
                () -> rentalService.addVehicle(
                        new Scooter(1, "Ninebot", 200)
                )
        );
    }

    @Test
    void shouldRejectInvalidVehicleData() {
        assertThrows(
                IllegalArgumentException.class,
                () -> rentalService.addVehicle(
                        new Car(-1, "BMW", 500)
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> rentalService.addVehicle(
                        new Car(1, " ", 500)
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> rentalService.addVehicle(
                        new Car(1, "BMW", 0)
                )
        );
    }

    @Test
    void shouldUpdateVehicle() {
        rentalService.addVehicle(new Car(1, "BMW", 500));

        rentalService.updateVehicle(1, "Audi", 700);

        assertEquals("Audi", rentalService.getVehicle(1).getModel());
        assertEquals(700, rentalService.getVehicle(1).getPricePerHour());
    }

    @Test
    void shouldDeleteAvailableVehicle() {
        rentalService.addVehicle(new Car(1, "BMW", 500));

        rentalService.deleteVehicle(1);

        assertThrows(
                NotFoundException.class,
                () -> rentalService.getVehicle(1)
        );
    }

    @Test
    void shouldFilterVehicles() {
        rentalService.addVehicle(new Car(1, "BMW", 500));
        rentalService.addVehicle(new Car(2, "Audi", 600));
        rentalService.addVehicle(new Scooter(3, "Ninebot", 200));
        rentalService.addCustomer(new Customer(1, "Andrey", 19));

        rentalService.rentVehicle(1, 1);

        assertEquals(3, rentalService.getVehicles(null, null).size());
        assertEquals(2, rentalService.getVehicles(null, "car").size());
        assertEquals(1, rentalService.getVehicles(true, "car").size());
        assertEquals(1, rentalService.getVehicles(false, "car").size());
        assertEquals(1, rentalService.getVehicles(true, "scooter").size());
    }

    @Test
    void shouldRejectUnknownVehicleTypeFilter() {
        assertThrows(
                IllegalArgumentException.class,
                () -> rentalService.getVehicles(null, "plane")
        );
    }

    @Test
    void shouldCreateUpdateAndDeleteCustomer() {
        rentalService.addCustomer(new Customer(1, "Andrey", 19));

        assertEquals("Andrey", rentalService.getCustomer(1).getName());

        rentalService.updateCustomer(1, "Alex", 20);

        assertEquals("Alex", rentalService.getCustomer(1).getName());
        assertEquals(20, rentalService.getCustomer(1).getAge());

        rentalService.deleteCustomer(1);

        assertThrows(
                NotFoundException.class,
                () -> rentalService.getCustomer(1)
        );
    }

    @Test
    void shouldRejectInvalidCustomerData() {
        assertThrows(
                IllegalArgumentException.class,
                () -> rentalService.addCustomer(
                        new Customer(1, "", 19)
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> rentalService.addCustomer(
                        new Customer(2, "Andrey", -1)
                )
        );
    }

    @Test
    void shouldRejectDuplicateCustomerId() {
        rentalService.addCustomer(new Customer(1, "Andrey", 19));

        assertThrows(
                InvalidOperationException.class,
                () -> rentalService.addCustomer(
                        new Customer(1, "Alex", 20)
                )
        );
    }

    @Test
    void shouldRentAndReturnVehicle() {
        Car car = new Car(1, "BMW", 500);
        Customer customer = new Customer(1, "Andrey", 19);

        rentalService.addVehicle(car);
        rentalService.addCustomer(customer);

        rentalService.rentVehicle(1, 1);

        assertFalse(car.isAvailable());
        assertSame(customer, car.getCurrentRenter());

        rentalService.returnVehicle(1);

        assertTrue(car.isAvailable());
        assertNull(car.getCurrentRenter());
    }

    @Test
    void shouldRejectRentingOccupiedVehicle() {
        rentalService.addVehicle(new Car(1, "BMW", 500));
        rentalService.addCustomer(new Customer(1, "Andrey", 19));
        rentalService.addCustomer(new Customer(2, "Alex", 20));

        rentalService.rentVehicle(1, 1);

        assertThrows(
                InvalidOperationException.class,
                () -> rentalService.rentVehicle(1, 2)
        );

        assertEquals(
                1,
                rentalService.getVehicle(1).getCurrentRenter().getId()
        );
    }

    @Test
    void shouldCheckAgeRestrictions() {
        rentalService.addVehicle(new Car(1, "BMW", 500));
        rentalService.addVehicle(new Scooter(2, "Ninebot", 200));

        rentalService.addCustomer(new Customer(1, "Teenager", 17));
        rentalService.addCustomer(new Customer(2, "Child", 15));
        rentalService.addCustomer(new Customer(3, "Adult", 18));

        assertThrows(
                InvalidOperationException.class,
                () -> rentalService.rentVehicle(1, 1)
        );

        assertThrows(
                InvalidOperationException.class,
                () -> rentalService.rentVehicle(2, 2)
        );

        rentalService.rentVehicle(1, 3);
        rentalService.rentVehicle(2, 1);

        assertFalse(rentalService.getVehicle(1).isAvailable());
        assertFalse(rentalService.getVehicle(2).isAvailable());
    }

    @Test
    void shouldRejectReturningAvailableVehicle() {
        rentalService.addVehicle(new Car(1, "BMW", 500));

        assertThrows(
                InvalidOperationException.class,
                () -> rentalService.returnVehicle(1)
        );
    }

    @Test
    void shouldProtectActiveRentalsFromDeletion() {
        rentalService.addVehicle(new Car(1, "BMW", 500));
        rentalService.addCustomer(new Customer(1, "Andrey", 19));
        rentalService.rentVehicle(1, 1);

        assertThrows(
                InvalidOperationException.class,
                () -> rentalService.deleteVehicle(1)
        );

        assertThrows(
                InvalidOperationException.class,
                () -> rentalService.deleteCustomer(1)
        );
    }

    @Test
    void shouldRejectAgeChangeThatInvalidatesRental() {
        rentalService.addVehicle(new Car(1, "BMW", 500));
        rentalService.addCustomer(new Customer(1, "Andrey", 19));
        rentalService.rentVehicle(1, 1);

        assertThrows(
                InvalidOperationException.class,
                () -> rentalService.updateCustomer(1, "Andrey", 15)
        );

        assertEquals(19, rentalService.getCustomer(1).getAge());
        assertFalse(rentalService.getVehicle(1).isAvailable());
    }

    @Test
    void shouldAllowValidAgeChangeDuringRental() {
        rentalService.addVehicle(new Scooter(1, "Ninebot", 200));
        rentalService.addCustomer(new Customer(1, "Andrey", 17));
        rentalService.rentVehicle(1, 1);

        rentalService.updateCustomer(1, "Andrey", 16);

        assertEquals(16, rentalService.getCustomer(1).getAge());
        assertFalse(rentalService.getVehicle(1).isAvailable());
    }

    @Test
    void shouldCalculateDifferentRentalPrices() {
        rentalService.addVehicle(new Car(1, "BMW", 500));
        rentalService.addVehicle(new Scooter(2, "Ninebot", 200));

        assertEquals(1000.0, rentalService.getRentalPrice(1, 2));
        assertEquals(500.0, rentalService.getRentalPrice(2, 2));
    }

    @Test
    void shouldRejectInvalidRentalDuration() {
        rentalService.addVehicle(new Car(1, "BMW", 500));

        assertThrows(
                IllegalArgumentException.class,
                () -> rentalService.getRentalPrice(1, 0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> rentalService.getRentalPrice(1, -2)
        );
    }

    @Test
    void shouldRejectMissingResources() {
        assertThrows(
                NotFoundException.class,
                () -> rentalService.getVehicle(999)
        );

        assertThrows(
                NotFoundException.class,
                () -> rentalService.getCustomer(999)
        );

        assertThrows(
                NotFoundException.class,
                () -> rentalService.rentVehicle(999, 999)
        );
    }
}
