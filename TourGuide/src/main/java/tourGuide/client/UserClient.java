package tourGuide.client;

import tourGuide.user.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "${user-service.name}", url = "${user-service.url}")
public interface UserClient {

    @RequestMapping(method = RequestMethod.GET,value = "/getUserByUsername")
    User getUserByUsername(@RequestParam String username);


}
