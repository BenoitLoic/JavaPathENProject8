package tourGuide.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tourGuide.model.Location;
import tourGuide.model.VisitedLocation;
import tourGuide.user.User;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

@Component
public class InternalTestRepository {

    private static final Logger logger = LoggerFactory.getLogger(InternalTestRepository.class);
    protected static final Map<String, User> internalUserMap = new ConcurrentHashMap<>();


    public InternalTestRepository() {
        initializeInternalUsers();
    }

    public static Map<String,User> getInternalUserMap(){
        return internalUserMap;
    }

    /**********************************************************************************
     *
     * Methods Below: For Internal Testing
     *
     **********************************************************************************/

    // Database connection will be used for external users, but for testing purposes internal users
    // are provided and stored in memory


    protected void initializeInternalUsers() {
        IntStream.range(0, InternalTestHelper.getInternalUserNumber())
                .forEach(
                        i -> {
                            String userName = "internalUser" + i;
                            String phone = "000";
                            String email = userName + "@tourGuide.com";
                            String userNumber = String.format("%06d", i);
                            UUID userId = UUID.fromString("0000-00-00-00-" + userNumber);

                            User user = new User(userId, userName, phone, email);
                            generateUserLocationHistory(user);

                            internalUserMap.put(userName, user);
                        });
        logger.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
    }

    private void generateUserLocationHistory(User user) {
        IntStream.range(0, 3)
                .forEach(
                        i ->
                                user.addToVisitedLocations(
                                        new VisitedLocation(
                                                user.getUserId(),
                                                new Location(generateRandomLatitude(), generateRandomLongitude()),
                                                getRandomTime())));
    }

    private double generateRandomLongitude() {
        double leftLimit = -180;
        double rightLimit = 180;
        return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
    }

    private double generateRandomLatitude() {
        double leftLimit = -85.05112878;
        double rightLimit = 85.05112878;
        return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
    }

    private Date getRandomTime() {
        LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
        return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
    }

}
