package tourGuide.client;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import tourGuide.model.Attraction;
import tourGuide.model.Location;
import tourGuide.model.VisitedLocation;

/**
 * Feign client interface for LocationService Api. Contains method to request data from
 * LocationService REST Api.
 */
@FeignClient(value = "${location-service.name}", url = "${location-service.url}")
public interface LocationClient {
  @RequestMapping(method = RequestMethod.POST, value = "/location/add")
  VisitedLocation getLocation(@RequestParam UUID userId);

  @RequestMapping(method = RequestMethod.GET, value = "/attraction/getNearby")
  Collection<Attraction> getNearbyAttractions(
      @RequestParam Double latitude, @RequestParam Double longitude);

  @RequestMapping(method = RequestMethod.GET, value = "/getStats")
  Map<UUID, Location> getAllLastLocation();
}
