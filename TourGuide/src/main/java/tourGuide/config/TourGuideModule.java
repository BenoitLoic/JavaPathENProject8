package tourGuide.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TourGuideModule {



  @Bean
  public ObjectMapper objectMapper(){
    return new ObjectMapper();
  }

}
