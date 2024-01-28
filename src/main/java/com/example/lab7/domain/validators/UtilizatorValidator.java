package com.example.lab7.domain.validators;

import com.example.lab7.domain.Utilizator;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;


public class UtilizatorValidator implements Validator<Utilizator> {
    @Override
    public void validate(Utilizator entity) throws ValidationException {
        List<String> errors = new ArrayList<>();
        Predicate<String> validString = x ->x == null || x.isEmpty() || x.trim().isEmpty();

        //if(entity.getId() < 0) errors.add("ID invalid! ");
        if(validString.test(entity.getFirstName())) errors.add("Prenume invalid! ");
        if(validString.test(entity.getFirstName())) errors.add("Nume invalid! ");
        if(entity.getEmail() == null || entity.getEmail() == "") errors.add("E-mail invalid! ");
        if(entity.getPassword() == null || entity.getPassword() == "") errors.add("Parola invalida");

        if(!errors.isEmpty()) throw new ValidationException("Eroare! " + String.join("",errors));
    }
}

