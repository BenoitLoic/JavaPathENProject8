package tourGuide.service;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import tourGuide.dto.GetNearbyAttractionDto;
import tourGuide.model.Location;
import tourGuide.model.VisitedLocation;
import tourGuide.user.User;

/** Service for Location features. Contain methods that create/read location and attractions. */
public interface LocationService {

  /**
   * This method get the last location where the given user as gone, or the actual location if the
   * list is empty.
   *
   * @param user the user
   * @return the last visited location if exists, or the actual location
   */
  VisitedLocation getUserLocation(User user);

  /**
   * This method wait for the active threads from executorService to end gracefully or else shutdown.
   * after the timeout
   */
  void awaitTerminationAfterShutdown();

  /**
   * This method add the actual location as visitedLocation for the user.
   *
   * @param user the user
   */
  void trackUserLocation(User user);

  /**
   * This method find all attractions in range.
   *
   * @param userName the username
   * @return a list with the 5 closest attraction in range
   */
  Map<Location, Collection<GetNearbyAttractionDto>> getNearbyAttractions(String userName);

  /**
   * This method call location-service to retrieve the last known location of all user.
   *
   * @return a map with the user id : last location saved
   */
  Map<UUID, Location> getAllCurrentLocations();
}
