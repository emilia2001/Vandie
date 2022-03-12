package com.example.socialnetworkfinal.domain.validators;


import com.example.socialnetworkfinal.domain.Friendship;
import com.example.socialnetworkfinal.domain.exceptions.ValidationException;

/**
 * Validator for friendship
 */
public class FriendshipValidator implements Validator<Friendship> {

    /**
     * @param entity - Friendship, the friendship to be validates
     * @throws ValidationException - if the id of the users are equal
     *                              - if the state is not in (approved, approved, rejected)
     */
    @Override
    public void validate(Friendship entity) throws ValidationException {
        Long id1 = entity.getId().getLeft();
        Long id2 = entity.getId().getRight();
        String err = "";
        if (id1 == id2)
            err += "That is you!\n";
        if (!err.isEmpty())
            throw new ValidationException(err);

    }
}
