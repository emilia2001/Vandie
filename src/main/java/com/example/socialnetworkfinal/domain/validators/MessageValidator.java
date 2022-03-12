package com.example.socialnetworkfinal.domain.validators;


import com.example.socialnetworkfinal.domain.Message;
import com.example.socialnetworkfinal.domain.exceptions.ValidationException;

/**
 * Validator for Message
 */
public class MessageValidator implements Validator<Message> {

    /**
     * @param message - Message, the message to be validated
     * @throws ValidationException - if the text of the message is empty
     */
    @Override
    public void validate(Message message) throws ValidationException {
        String err = "";
        if (message.getMessage().isEmpty())
            err += "Invalid message text\n";
        if (message.getTo().isEmpty())
            err += "To is a required field!\n";
        if (!err.isEmpty())
            throw new ValidationException(err);
    }
}
