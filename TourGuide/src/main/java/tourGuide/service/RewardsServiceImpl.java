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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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

      try {
        CompletableFuture.supplyAsync(
                () -> rewardClient.addUserReward(user.getUserId(), visitedLocation), threadPool)
            .thenAccept(
                userReward -> {
                  userRewardsReturnList.add(userReward);
                  if (!attractionIds.contains(userReward.attraction().attractionId())) {
                    user.addUserReward(userReward);
                  }
                });
      } catch (FeignException.FeignClientException fce) {
        logger.error("Error, Feign client failed." + fce);
        throw new ResourceNotFoundException("Error, cant reach service.");
      }
    }

    return userRewardsReturnList;
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
