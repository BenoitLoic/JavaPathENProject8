package tourGuide.model;

/**
 * Location record.
 *
 * @param latitude the latitude Double(-90.0, 90.0)
 * @param longitude the longitude Double(-180.0, 180.0)
 */
public record Location(Double longitude, Double latitude) {
}
