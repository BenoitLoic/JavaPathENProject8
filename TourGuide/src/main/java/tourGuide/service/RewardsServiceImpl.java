package tourGuide.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import io.netty.util.internal.logging.InternalLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tourGuide.client.RewardClient;
import tourGuide.dto.GetNearbyAttractionDto;
import tourGuide.exception.ResourceNotFoundException;
import tourGuide.model.Attraction;

import java.util.*;
import java.util.concurrent.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tourGuide.model.UserReward;
import tourGuide.model.VisitedLocation;
import tourGuide.user.User;

@Service
public class RewardsServiceImpl implements RewardsService {
  private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

  private final Logger logger = LoggerFactory.getLogger(RewardsServiceImpl.class);
  private final ExecutorService threadPool = Executors.newFixedThreadPool(200);

  @Autowired RewardClient rewardClient;
  @Autowired ObjectMapper mapper;

  @Override
  public Collection<GetNearbyAttractionDto> calculateRewardsPoints(
      Collection<Attraction> attractionCollection, UUID userId) {

    Collection<GetNearbyAttractionDto> dtoCollection = new ArrayList<>(5);

    attractionCollection.parallelStream()
        .forEach(
            attraction -> {
              Integer point = rewardClient.getReward(attraction.attractionId(), userId);
              GetNearbyAttractionDto dto =
                  new GetNearbyAttractionDto(
                      attraction.attractionName(),
                      attraction.city(),
                      attraction.state(),
                      attraction.attractionId(),
                      attraction.location().latitude(),
                      attraction.location().longitude(),
                      attraction.distance(),
                      point);
              dtoCollection.add(dto);
            });

    return dtoCollection;
  }

  @Override
  public void addRewards(User user) {

    List<UserReward> userRewards = user.getUserRewards();

    List<UUID> attractionIds = Collections.synchronizedList(new ArrayList<>());
    userRewards.forEach(ur -> attractionIds.add(ur.attraction().attractionId()));

    for (VisitedLocation visitedLocation : user.getVisitedLocations()) {

      try {
        CompletableFuture.supplyAsync(
                () -> rewardClient.addUserReward(user.getUserId(), visitedLocation), threadPool)
            .thenAccept(
                userReward -> {
                  synchronized (attractionIds) {
                    if (!attractionIds.contains(userReward.attraction().attractionId())) {
                      userRewards.add(userReward);
                      attractionIds.add(userReward.attraction().attractionId());
                    }
                  }
                });
      } catch (feign.FeignException fce) {
        logger.error("Error, Feign client failed." + fce);
        throw new ResourceNotFoundException("Error, cant reach service.");
      }
    }
  }

  /**
   * Get all the rewards already owned by a user.
   *
   * @param user the user
   * @return a list of user's rewards
   */
  @Override
  public Collection<UserReward> getRewards(User user) {
    return user.getUserRewards();
  }

  public void awaitTerminationAfterShutdown() {
    threadPool.shutdown();
    try {
      if (!threadPool.awaitTermination(30, TimeUnit.MINUTES)) {
        threadPool.shutdownNow();
      }
    } catch (InterruptedException ex) {
      threadPool.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }
}
