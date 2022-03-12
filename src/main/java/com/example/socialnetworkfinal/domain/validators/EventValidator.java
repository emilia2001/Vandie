package com.example.socialnetworkfinal.domain.validators;

import com.example.socialnetworkfinal.domain.Event;
import com.example.socialnetworkfinal.domain.exceptions.EventException;
import com.example.socialnetworkfinal.domain.exceptions.ValidationException;

import java.time.LocalDate;

public class EventValidator implements Validator<Event> {
    @Override
    public void validate(Event entity) throws ValidationException {
        String err= "";
        if (entity.getName().isEmpty() || entity.getDescription().isEmpty() || entity.getCategory().isEmpty() || entity.getLocation().isEmpty() || entity.getEndDate() == null || entity.getStartDate() == null)
            err+="All fields are required! \n";
        LocalDate lt = LocalDate.now();
        if(!(entity.getEndDate() == null)) {
            if (entity.getEndDate().isBefore(lt))
                err += "Incorrect date! \n";
        }
        if(!(entity.getStartDate() == null ) && !(entity.getEndDate() == null )  ){
            if(entity.getStartDate().isAfter(entity.getEndDate()))
                err += "Incorrect date chronology! \n";
        }
        if(!err.isEmpty())
            throw  new EventException(err);
    }
}
