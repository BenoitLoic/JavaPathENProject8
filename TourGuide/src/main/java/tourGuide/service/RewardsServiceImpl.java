package tourGuide.service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tourGuide.client.RewardClient;
import tourGuide.dto.GetNearbyAttractionDto;
import tourGuide.exception.ResourceNotFoundException;
import tourGuide.model.Attraction;
import tourGuide.model.UserReward;
import tourGuide.model.VisitedLocation;
import tourGuide.user.User;

/** Reward service implementation. */
@Service
public class RewardsServiceImpl implements RewardsService {

  private final Logger logger = LoggerFactory.getLogger(RewardsServiceImpl.class);
  private final ExecutorService threadPool = Executors.newFixedThreadPool(200);
  private final RewardClient rewardClient;

  @Autowired
  public RewardsServiceImpl(RewardClient rewardClient) {
    this.rewardClient = rewardClient;
  }

  /**
   * Get the reward point that could be won by the user for the given list of attractions.
   *
   * @param attractionCollection the list of attraction
   * @param userId the user id
   * @return a list of attraction dto with rewardPoint field
   */
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

  /**
   * Check the user visited location and add the rewards accordingly.
   *
   * @param user the user
   */
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
                      user.addUserReward(userReward);
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
   * Check if there is a reward for the last visitedLocation.
   *
   * @param user the user
   */
  @Override
  public void addRewardsForLastLocation(User user) {

    List<UserReward> userRewards = user.getUserRewards();

    List<UUID> attractionIds = Collections.synchronizedList(new ArrayList<>());
    userRewards.forEach(ur -> attractionIds.add(ur.attraction().attractionId()));

    CompletableFuture.supplyAsync(
            () -> rewardClient.addUserReward(user.getUserId(), user.getLastVisitedLocation()),
            threadPool)
        .thenAccept(
            userReward -> {
              synchronized (attractionIds) {
                if (!attractionIds.contains(userReward.attraction().attractionId())) {
                  user.addUserReward(userReward);
                  attractionIds.add(userReward.attraction().attractionId());
                }
              }
            });
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
