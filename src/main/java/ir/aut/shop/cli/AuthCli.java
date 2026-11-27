package ir.aut.shop.cli;

import ir.aut.shop.model.Email;
import ir.aut.shop.model.PhoneNumber;
import ir.aut.shop.model.User;
import ir.aut.shop.service.PasswordResetService;
import ir.aut.shop.service.UserService;

/**
 * Opening an account, signing in and out, recovering a password
 * (user stories 1 to 4).
 */
public class AuthCli {

    private final ConsoleIo io;
    private final Session session;
    private final UserService userService;
    private final PasswordResetService passwordReset;

    /**
     * REQUIRES: every argument is non-null.
     * EFFECTS: Builds the screen over the given console, session and service.
     *
     * @param io where questions are asked and answers shown
     * @param session who is signed in
     * @param userService the accounts behind the screen
     * @param passwordReset the recovery behind the screen
     */
    public AuthCli(ConsoleIo io, Session session, UserService userService,
            PasswordResetService passwordReset) {
        this.io = io;
        this.session = session;
        this.userService = userService;
        this.passwordReset = passwordReset;
    }

    /**
     * MODIFIES: the account store and the session.
     * EFFECTS: Asks for the details of a new account, opens it and signs the customer in.
     * A rejected detail is reported and the attempt is abandoned (user story 1).
     */
    public void signUp() {
        io.printTitle("Sign up");
        try {
            Email email = new Email(io.readLine("Email address"));
            PhoneNumber phoneNumber = new PhoneNumber(io.readLine("Mobile number"));
            String fullName = io.readLine("Full name");
            String password = io.readLine("Password (at least 6 characters)");

            User user = userService.signUp(email, phoneNumber, fullName, password);
            session.logIn(user);
            io.printSuccess("Welcome, " + user.fullName() + ". Your account is ready.");
        } catch (IllegalArgumentException problem) {
            io.printError(problem.getMessage());
        }
    }

    /**
     * MODIFIES: the session.
     * EFFECTS: Asks for credentials and signs the user in, or reports that they do not
     * match an account (user story 2).
     */
    public void logIn() {
        io.printTitle("Log in");
        try {
            String emailOrPhone = io.readLine("Email or mobile number");
            String password = io.readLine("Password");

            User user = userService.logIn(emailOrPhone, password);
            session.logIn(user);
            io.printSuccess("Signed in as " + user.fullName() + ".");
        } catch (IllegalArgumentException problem) {
            io.printError(problem.getMessage());
        }
    }

    /**
     * MODIFIES: the session.
     * EFFECTS: Signs the current user out, after which the customer-only menus disappear
     * (user story 3).
     */
    public void logOut() {
        session.logOut();
        io.printSuccess("Signed out.");
    }

    /**
     * MODIFIES: the matching account.
     * EFFECTS: Issues a reset token for a registered address and lets the customer set a
     * new password with it, or reports that the address is unknown (user story 4).
     */
    public void recoverPassword() {
        io.printTitle("Forgotten password");
        try {
            Email email = new Email(io.readLine("Email address of the account"));
            String token = passwordReset.requestReset(email);
            io.print("A reset link was sent. For this exercise it is shown here:");
            io.print("  " + token);

            String typedToken = io.readLine("Reset token");
            String newPassword = io.readLine("New password (at least 6 characters)");
            passwordReset.completeReset(typedToken, newPassword);
            io.printSuccess("Your password has been changed. You can sign in now.");
        } catch (IllegalArgumentException problem) {
            io.printError(problem.getMessage());
        }
    }
}
