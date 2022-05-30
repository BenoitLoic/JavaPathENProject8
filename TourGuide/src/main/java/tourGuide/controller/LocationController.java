package tourGuide.controller;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import tourGuide.dto.GetNearbyAttractionDto;
import tourGuide.model.Location;

/** Rest Controller for Location features. */
public interface LocationController {

  /**
   * Get the location of the user.
   *
   * @param userName the user's username
   * @return the location
   */
  Location getLocation(String userName);

  /**
   * Get the closest attractions based on the user location.
   *
   * @param userName the user's username
   * @return a map with the user location and the list of attraction ordered by distance
   */
  Map<Location, Collection<GetNearbyAttractionDto>> getNearbyAttractions(String userName);

  /**
   * This method get a list of every user's most recent location as JSON
   *
   * @return JSON mapping of userId : Locations
   */
  Map<UUID, Location> getAllCurrentLocations();
}
