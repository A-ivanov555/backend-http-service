package app.model;

public abstract class Vehicle {

    private int id;
    private String model;
    private double pricePerHour;
    private Customer currentRenter;

    public Vehicle(int id, String model, double pricePerHour) {
        this.id = id;
        this.model = model;
        this.pricePerHour = pricePerHour;
    }

    public int getId() {
        return id;
    }

    public String getModel() {
        return model;
    }

    public double getPricePerHour() {
        return pricePerHour;
    }

    public Customer getCurrentRenter() {
        return currentRenter;
    }

    public boolean isAvailable() {
        return currentRenter == null;
    }

    public void rentTo(Customer customer) {
        currentRenter = customer;
    }

    public void returnVehicle() {
        currentRenter = null;
    }

    public abstract double getPrice(int hours);
    public abstract boolean canRent(Customer customer);
    public void updateDetails(String model, double pricePerHour) {
        this.model = model;
        this.pricePerHour = pricePerHour;
    }
}