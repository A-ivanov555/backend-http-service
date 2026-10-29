package app.service;

import app.model.Customer;
import app.model.Vehicle;

import java.util.HashMap;

public class RentalService {
    private HashMap<Integer, Vehicle> vehicles=new HashMap<>();
    private HashMap<Integer, Customer>  customers=new HashMap<>();
    public void addVehicle(Vehicle vehicle){
        vehicles.put(vehicle.getId(), vehicle);

    }
    public void addCustomer(Customer customer){
        customers.put(customer.getId(), customer);
    }
    public Vehicle getVehicle(int id){
        return vehicles.get(id);
    }
    public Customer getCustomer(int id){
        return customers.get(id);
    }
    public Vehicle deleteVehicle(int id){
        return vehicles.remove(id);
    }
    public Customer deleteCustomer(int id){
        return customers.remove(id);
    }
}
