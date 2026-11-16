
package app.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class CustomerRequest {

    @Positive
    public Integer id;

    @NotBlank
    public String name;

    @NotNull
    @Min(0)
    public Integer age;
}
