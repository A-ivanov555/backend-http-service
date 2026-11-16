
package app.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class RentRequest {

    @NotNull
    @Positive
    public Integer customerId;
}
