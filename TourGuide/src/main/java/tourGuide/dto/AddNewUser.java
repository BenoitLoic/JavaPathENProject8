package tourGuide.dto;

/**
 * record for user creation with user client.
 *
 * @param userName the username
 * @param phoneNumber the phone number
 * @param emailAddress the email
 */
public record AddNewUser(String userName, String phoneNumber, String emailAddress) {
}
