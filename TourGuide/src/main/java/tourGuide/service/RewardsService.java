package tourGuide.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import tourGuide.client.RewardClient;
import tourGuide.dto.GetNearbyAttractionDto;
import tourGuide.model.Attraction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RewardsService {
  private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

  // proximity in miles
  private final int defaultProximityBuffer = 10;
  private int proximityBuffer = defaultProximityBuffer;
  private final int attractionProximityRange = 200;

  @Autowired RewardClient rewardClient;
  @Autowired ObjectMapper mapper;

  /** Setter for proximity buffer */
  public void setProximityBuffer(int proximityBuffer) {
    this.proximityBuffer = proximityBuffer;
  }

  public void setDefaultProximityBuffer() {
    proximityBuffer = defaultProximityBuffer;
  }

  public Collection<GetNearbyAttractionDto> calculateRewards(Collection<Attraction> attractionCollection, UUID userId) {

    Collection<GetNearbyAttractionDto> dtoCollection = new ArrayList<>(5);

    attractionCollection.parallelStream()
        .forEach(
            attraction -> {
              Integer point = rewardClient.getReward(attraction.attractionId(), userId);
              GetNearbyAttractionDto dto =
                  mapper.convertValue(attraction, GetNearbyAttractionDto.class);
              dto.setRewardPoint(point);
              dtoCollection.add(dto);
            });

    return dtoCollection;

  }

  /**
   * This method check if the attraction is within the proximity range of the location.
   *
   * @param attraction the attraction
   * @param location the location
   * @return true if they are in range, else return false
   */
  //  public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
  //    return !(getDistance(attraction, location) > attractionProximityRange);
  //  }

  /**
   * Check if the visitedLocation is within the range of the attraction. range defined by
   * proximityBuffer
   *
   * @param visitedLocation the visitedLocation
   * @param attraction the attraction
   * @return true if they are in range, else return false
   */
  //  private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
  //    return !(getDistance(attraction, visitedLocation.location) > proximityBuffer);
  //  }

  /**
   * This method get the reward points won by the user by visiting the attraction.
   *
   * @param attraction the attraction
   * @param user the user
   * @return number of points (integer) won by the user
   */
  //  private int getRewardPoints(Attraction attraction, User user) {
  //    return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
  //  }

}
