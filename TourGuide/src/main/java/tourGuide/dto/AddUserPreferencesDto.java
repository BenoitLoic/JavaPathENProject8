package tourGuide.dto;

import javax.validation.constraints.NotBlank;

public record AddUserPreferencesDto(@NotBlank String username,
                                    int attractionProximity,
                                    int lowerPricePoint,
                                    int highPricePoint,
                                    int tripDuration,
                                    int ticketQuantity,
                                    int numberOfAdults,
                                    int numberOfChildren
) {
}
