package tourGuide.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import tourGuide.dto.AddNewUser;
import tourGuide.model.user.User;

@FeignClient(value = "${user-service.name}", url = "${user-service.url}")
public interface UserClient {

  @RequestMapping(method = RequestMethod.GET, value = "/getUserByUsername")
  User getUserByUsername(@RequestParam String username);

  @RequestMapping(method = RequestMethod.POST, value = "/addUser")
  User addUser(@RequestBody AddNewUser user);
}
