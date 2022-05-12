package tourGuide.model;


import java.util.UUID;

/**
 * Attraction record. used to map feign client response of type Attraction.
 *
 * @param attractionName the attraction name
 * @param city           the city
 * @param state          the state
 * @param attractionId   the attraction id
 * @param latitude       the latitude
 * @param longitude      the longitude
 * @param distance       the distance
 */
public record Attraction(
        String attractionName,
        String city,
        String state,
        UUID attractionId,
        Double latitude,
        Double longitude,
        Double distance
) {

}
