package com.example.lab7.repository.RepoFile;

import com.example.lab7.domain.Prietenie;
import com.example.lab7.domain.Tuple;
import com.example.lab7.domain.validators.Validator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PrietenieFileRepository extends AbstractFileRepository<Tuple<Long,Long>, Prietenie> {
    public PrietenieFileRepository(String fileName, Validator<Prietenie> validator) {
        super(fileName, validator);
    }

    @Override
    protected Prietenie extractEntity(List<String> attributes) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        Prietenie prietenie = new Prietenie(LocalDateTime.parse(attributes.get(2), formatter));
        prietenie.setId(new Tuple<>(Long.parseLong(attributes.get(0)), Long.parseLong(attributes.get(1))));

        return prietenie;
    }

    @Override
    protected String createEntityAsString(Prietenie entity) {
        return entity.getId().getLeft() + ";" + entity.getId().getRight() + ";" + entity.getDate();
    }

    public void deleteNonexistent(Long id){
        for(Prietenie prietenie : findAll()){
            if(prietenie.getId().getLeft().equals(id) || prietenie.getId().getRight().equals(id)) {
                super.delete(prietenie.getId());
            }
        }
    }
}
