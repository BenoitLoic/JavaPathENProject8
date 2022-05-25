package tourGuide.client;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import tourGuide.dto.AddNewUser;
import tourGuide.user.User;
import org.springframework.cloud.openfeign.FeignClient;

import javax.validation.Valid;

@FeignClient(value = "${user-service.name}", url = "${user-service.url}")
public interface UserClient {

    @RequestMapping(method = RequestMethod.GET,value = "/getUserByUsername")
    User getUserByUsername(@RequestParam String username);

    @RequestMapping(method = RequestMethod.POST,value = "/addUser")
     User addUser(@RequestBody AddNewUser user);
}
