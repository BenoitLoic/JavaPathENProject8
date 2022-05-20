package tourGuide.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.util.internal.logging.InternalLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tourGuide.client.RewardClient;
import tourGuide.dto.GetNearbyAttractionDto;
import tourGuide.exception.ResourceNotFoundException;
import tourGuide.model.Attraction;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tourGuide.model.UserReward;
import tourGuide.model.VisitedLocation;
import tourGuide.user.User;

@Service
public class RewardsServiceImpl implements RewardsService {
  private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

  // proximity in miles
  private final int defaultProximityBuffer = 10;
  private int proximityBuffer = defaultProximityBuffer;
  private final int attractionProximityRange = 200;

  @Autowired RewardClient rewardClient;
  @Autowired ObjectMapper mapper;
  private final Logger logger = LoggerFactory.getLogger(RewardsServiceImpl.class);


  /** Setter for proximity buffer */
  public void setProximityBuffer(int proximityBuffer) {
    this.proximityBuffer = proximityBuffer;
  }

  public void setDefaultProximityBuffer() {
    proximityBuffer = defaultProximityBuffer;
  }

  @Override
  public Collection<GetNearbyAttractionDto> calculateRewardsPoints(
      Collection<Attraction> attractionCollection, UUID userId) {

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

  /* récupère l'utilisateur
    récupère les récompenses
    renvoi les récompenses
  * */
  @Override
  public Collection<UserReward> getRewards(User user) {



    List<UserReward> userRewardsCopy = new ArrayList<>(user.getUserRewards().size());
    Collections.copy(userRewardsCopy, user.getUserRewards());

    List<UUID> attractionIds = new ArrayList<>();
    userRewardsCopy.forEach(ur -> attractionIds.add(ur.attraction().attractionId()));
    List<UserReward> userRewardsReturnList = new ArrayList<>();

    for (VisitedLocation visitedLocation : user.getVisitedLocations()) {
      UserReward userReward = rewardClient.addUserReward(user.getUserId(), visitedLocation);
      if (userReward==null){
        logger.warn("Error, reward client returned null.");
        throw new ResourceNotFoundException("Error calling client.");
      }
      userRewardsReturnList.add(userReward);
      // check if reward already exist, if not add
      if (!attractionIds.contains(userReward.attraction().attractionId())) {
        user.addUserReward(userReward);
      }
    }

    return userRewardsReturnList;
  }

}
