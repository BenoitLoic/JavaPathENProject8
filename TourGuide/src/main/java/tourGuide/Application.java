package tourGuide;

import java.util.Locale;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class Application {

  public static void main(String[] args) {
    Locale.setDefault(Locale.US);
    SpringApplication.run(Application.class, args);
  }

}
