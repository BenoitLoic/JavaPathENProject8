package tourGuide.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jsoniter.output.JsonStream;

import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import tourGuide.config.Url;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import tripPricer.Provider;
import static tourGuide.config.Url.*;

@RestController
public class TourGuideController {

  @Autowired
  public TourGuideService tourGuideService;

  /**
   * @return
   */
  @RequestMapping(value = INDEX)
  public String index() {
    return "Greetings from TourGuide!";
  }


  /**
   * This method get the location of the user.
   * @param userName
   * @return
   */
  @GetMapping(value = GETLOCATION)
  public Location getLocation(@RequestParam String userName) {

    if (userName == null || userName.isBlank()){
      throw new IllegalArgumentException();
    }

    VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(userName));
    return visitedLocation.location;
  }

  //  TODO: Change this method to no longer return a List of Attractions.
  //  Instead: Get the closest five tourist attractions to the user - no matter how far away they are.
  //  Return a new JSON object that contains:
  // Name of Tourist attraction,
  // Tourist attractions lat/long,
  // The user's location lat/long,
  // The distance in miles between the user's location and each of the attractions.
  // The reward points for visiting each Attraction.
  //    Note: Attraction reward points can be gathered from RewardsCentral
  @GetMapping(value = GETNEARBYATTRACTIONS)
  public String getNearbyAttractions(@RequestParam String userName) {
    VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(userName));
    return JsonStream.serialize(tourGuideService.getNearByAttractions(visitedLocation));
  }

  /**
   * This method get the reward points owned by the user.
   * @param userName the user's userName
   * @return the reward points as Json
   */
  @GetMapping(value = GETREWARDS)
  public String getRewards(@RequestParam String userName) {
    return JsonStream.serialize(tourGuideService.getUserRewards(getUser(userName)));
  }

  /**
   * This method get a list of every user's most recent location as JSON
   * @return JSON mapping of userId to Locations
   */
  @GetMapping(value = GETALLCURRENTLOCATIONS)
  public String getAllCurrentLocations() {
    // TODO: Get a list of every user's most recent location as JSON
    //- Note: does not use gpsUtil to query for their current location,
    //        but rather gathers the user's current location from their stored location history.
    //
    // Return object should be the just a  similar to:
    //     {
    //        "019b04a9-067a-4c76-8817-ee75088c3822": {"longitude":-48.188821,"latitude":74.84371}
    //        ...
    //     }

    return JsonStream.serialize("");
  }

  /**
   * This method get a list of TripDeals (providers) for given user.
   * TripDeals are based on user preferences.
   * @param userName the user's userName
   * @return list of providers as JSON
   */
  @RequestMapping(value = GETTRIPDEALS)
  public String getTripDeals(@RequestParam String userName) {
    List<Provider> providers = tourGuideService.getTripDeals(getUser(userName));
    return JsonStream.serialize(providers);
  }

  /**
   * This method get the User object in data storage with its userName.
   * @param userName the user's userName
   * @return the User
   */
  private User getUser(String userName) {
    return tourGuideService.getUser(userName);
  }


}