package tourGuide.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import gpsUtil.GpsUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TourGuideModule {

  @Bean
  public GpsUtil gpsUtil() {
    return new GpsUtil();
  }

  @Bean
  public ObjectMapper objectMapper(){
    return new ObjectMapper();
  }

}
