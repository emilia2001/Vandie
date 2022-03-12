package com.example.socialnetworkfinal.domain.validators;


import com.example.socialnetworkfinal.domain.User;
import com.example.socialnetworkfinal.domain.exceptions.ValidationException;

/**
 * Validator for user
 */
public class UserValidator implements Validator<User> {

    /**
     * @param entity - User, user to be validated
     * @throws ValidationException - if email doesn't match the structure of an email (lettersAndSimbols@letters.letters)
     *                               if the names contains anything else than letters and spaces
     */
    @Override
    public void validate(User entity) throws ValidationException {
        String email = entity.getEmail();
        String firstName = entity.getFirstName();
        String lastName = entity.getLastName();
        String err = "";
        if (!email.matches("[a-zA-Z0-9._-]+@[a-z]+[.][a-z]{2,4}"))
            err += "Invalid email\n";
        if (!firstName.matches("[A-Z][a-z]+([ -][A-Z][a-z]+)?"))
            err += "Invalid first name\n";
        if (!lastName.matches("[A-Z][a-z]+"))
            err += "Invalid last name\n";
        if (!err.isEmpty())
            throw new ValidationException(err);
    }
}
