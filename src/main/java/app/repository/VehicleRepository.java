
package app.repository;

import app.model.Vehicle;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class VehicleRepository {

    private final Map<Integer, Vehicle> vehicles = new HashMap<>();

    public Vehicle findById(int id) {
        return vehicles.get(id);
    }

    public List<Vehicle> findAll() {
        return new ArrayList<>(vehicles.values());
    }

    public boolean existsById(int id) {
        return vehicles.containsKey(id);
    }

    public void save(Vehicle vehicle) {
        vehicles.put(vehicle.getId(), vehicle);
    }

    public void deleteById(int id) {
        vehicles.remove(id);
    }
}
