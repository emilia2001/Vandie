package com.example.socialnetworkfinal.domain.validators;

import com.example.socialnetworkfinal.domain.exceptions.ValidationException;

/**
 * @param <T> - the tyoe of element to be validated
 * interface for validators
 */
public interface Validator <T>{

    void validate(T entity) throws ValidationException;
}
