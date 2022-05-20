package tourGuide.client;

import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import tourGuide.model.UserReward;
import tourGuide.model.VisitedLocation;

import javax.validation.Valid;

@FeignClient(value = "${reward-service.name}", url = "${reward-service.url}")
public interface RewardClient {

  @RequestMapping(method = RequestMethod.GET, value = "/rewards/getPoint")
  Integer getReward(@RequestParam UUID attractionId, @RequestParam UUID userId);

  @RequestMapping(method = RequestMethod.POST, value = "/rewards/add")
  UserReward addUserReward(@RequestParam UUID userId, @RequestBody VisitedLocation visitedLocation);
}
