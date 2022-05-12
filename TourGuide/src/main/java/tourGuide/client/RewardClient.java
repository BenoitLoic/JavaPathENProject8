package tourGuide.client;

import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "${reward-service.name}", url = "${reward-service.url}")
public interface RewardClient {

    @RequestMapping(method = RequestMethod.GET,value = "/rewards/getPoint")
    Integer getReward(@RequestParam UUID attractionId,@RequestParam UUID userId);

}
