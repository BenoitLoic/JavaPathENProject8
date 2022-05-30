package tourGuide.dto;

import javax.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Record for addUserPreference feature.
 * @param username the username
 * @param attractionProximity the acceptable proximity for attraction
 * @param lowerPricePoint lower price point
 * @param highPricePoint high price point
 * @param tripDuration trip duration
 * @param ticketQuantity ticket quantity
 * @param numberOfAdults number of adult
 * @param numberOfChildren number of children
 */
public record AddUserPreferencesDto(@NotBlank String username,
                                    int attractionProximity,
                                    int lowerPricePoint,
                                    @DefaultValue("1000000") int highPricePoint,
                                    int tripDuration,
                                    int ticketQuantity,
                                    int numberOfAdults,
                                    int numberOfChildren
) {
}
