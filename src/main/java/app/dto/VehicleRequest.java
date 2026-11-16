
package app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public class VehicleRequest {

    @Positive
    public Integer id;

    @Pattern(regexp = "(?i)(car|scooter)")
    public String type;

    @NotBlank
    public String model;

    @NotNull
    @Positive
    public Double pricePerHour;
}
