//package tourGuide.service;
//
//import java.util.List;
//
//import org.springframework.stereotype.Service;
//
//import gpsUtil.GpsUtil;
//import gpsUtil.location.Attraction;
//import gpsUtil.location.Location;
//import gpsUtil.location.VisitedLocation;
//import rewardCentral.RewardCentral;
//import tourGuide.user.User;
//import tourGuide.user.UserReward;
//
//@Service
//public class RewardsService {
//  private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;
//
//  // proximity in miles
//  private final int defaultProximityBuffer = 10;
//  private       int proximityBuffer        = defaultProximityBuffer;
//  private final int     attractionProximityRange = 200;
//  private final GpsUtil gpsUtil;
//  private final RewardCentral rewardsCentral;
//
//  public RewardsService(GpsUtil gpsUtil, RewardCentral rewardCentral) {
//    this.gpsUtil        = gpsUtil;
//    this.rewardsCentral = rewardCentral;
//  }
//
//  /**
//   * Setter for proximity buffer
//   */
//  public void setProximityBuffer(int proximityBuffer) {
//    this.proximityBuffer = proximityBuffer;
//  }
//
//
//  public void setDefaultProximityBuffer() {
//    proximityBuffer = defaultProximityBuffer;
//  }
//
//  public void calculateRewards(User user) {
//    List<VisitedLocation> userLocations = user.getVisitedLocations();
//    List<Attraction>      attractions   = gpsUtil.getAttractions();
//
//    for (VisitedLocation visitedLocation : userLocations) {
//      for (Attraction attraction : attractions) {
//        if (user.getUserRewards().stream().filter(
//            r -> r.attraction.attractionName.equals(attraction.attractionName)).count() == 0) {
//          if (nearAttraction(visitedLocation, attraction)) {
//            user.addUserReward(
//                new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
//          }
//        }
//      }
//    }
//  }
//
//  /**
//   * This method check if the attraction is within the proximity range of the location.
//   * @param attraction the attraction
//   * @param location the location
//   * @return true if they are in range, else return false
//   */
//  public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
//    return !(getDistance(attraction, location) > attractionProximityRange);
//  }
//
//  /**
//   * Check if the visitedLocation is within the range of the attraction.
//   * range defined by proximityBuffer
//   * @param visitedLocation the visitedLocation
//   * @param attraction the attraction
//   * @return true if they are in range, else return false
//   */
//  private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
//    return !(getDistance(attraction, visitedLocation.location) > proximityBuffer);
//  }
//
//  /**
//   * This method get the reward points won by the user by visiting the attraction.
//   * @param attraction the attraction
//   * @param user the user
//   * @return number of points (integer) won by the user
//   */
//  private int getRewardPoints(Attraction attraction, User user) {
//    return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
//  }
//
//  /**
//   * Calculate distance between 2 coordinate.
//   * @param loc1 location 1 : latitude, longitude
//   * @param loc2 location 2 : latitude, longitude
//   * @return distance en Miles
//   */
//  public double getDistance(Location loc1, Location loc2) {
//    double lat1 = Math.toRadians(loc1.latitude);
//    double lon1 = Math.toRadians(loc1.longitude);
//    double lat2 = Math.toRadians(loc2.latitude);
//    double lon2 = Math.toRadians(loc2.longitude);
//
//    double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
//        + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));
//
//    double nauticalMiles = 60 * Math.toDegrees(angle);
//    double statuteMiles  = STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
//    return statuteMiles;
//  }
//
//}
