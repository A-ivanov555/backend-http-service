package app.model;

public abstract class Vehicle {
    private int id;
    private String model;
    private double  pricePerHour;
    private boolean available;
    public Vehicle(int id, String model, double  pricePerHour) {
        this.id = id;
        this.model = model;
        this.pricePerHour = pricePerHour;
        this.available =  true;
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
    public boolean isAvailable() {
        return available;
    }
    public abstract  double getPrice(int hours);
}
