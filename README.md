# TourGuide-APP

TourGuide main application

### Start application

* copy/clone this project.
* cd PATH/TO/PROJECT/ROOT/TourGuide where build.gradle is present
* run: ./gradlew bootJar
* clone and build [reward-service](https://gitlab.com/t10016/tourGuideRewardService)
  , [location-service](https://gitlab.com/t10016/tourGuideLocationService)
  , [user-service](https://gitlab.com/t10016/tourGuideUserService)
* run: docker-compose -f docker-compose.yml up --build
* Access endpoints on localhost:8084/

## Test

* run: ./gradlew test integrationTest
    * result:
        * UT: TourGuide/build/reports/tests/test/index.html
        * IT: TourGuide/build/reports/tests/integrationTest/index.html
    * coverage:
        * UT: TourGuide/build/jacocoUTHtml/index.html
        * IT: TourGuide/build/jacocoITHtml/index.html
    * Note: test presents in integrationTest.old are disabled, they need microservices to work(run with
      docker-compose-test up, like in performanceTest)
* performance test:
    * run: docker-compose -f docker-compose-test.yml up --build
    * run: ./gradlew performanceTest

## Technical:

1. Java : 17
2. Gradle 7+
3. SpringBoot : 2.6.6
4. Docker
5. Docker-compose

## EndPoints

* GET: "/"
    * parameter: no_parameter
    * return: homepage
* GET: "/getLocation"
    * parameter: userName
    * return: Location
* GET: "/getNearbyAttractions"
    * parameter: userName
    * return: Map of Location : Collection<GetNearbyAttractionDto>
* GET: "/getAllCurrentLocations"
    * parameter: no_parameter
    * return: Map of UserId : Location
* GET: "/getRewards"
    * parameter: userName
    * return: Collection of UserReward
* GET: "/getTripDeals"
    * parameter1: userName
    * parameter2: attractionId
    * return: Collection of Provider
* POST: "/addUserPreferences"
    * body: userPreferences
    * return: Map of UserId : Location