package com.example.lab7.repository.RepoInMemory;

import com.example.lab7.domain.Entity;
import com.example.lab7.domain.validators.Validator;
import com.example.lab7.repository.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryRepository<ID, E extends Entity<ID>> implements Repository<ID,E> {
    private Validator<E> validator;
    Map<ID,E> entities;

    public InMemoryRepository(Validator<E> validator) {
        this.validator = validator;
        entities=new HashMap<ID,E>();
    }

    @Override
    public Optional<E> findOne(ID id){
        if (id == null)
            throw new IllegalArgumentException("Error! Id must be not null!\n");

        if(entities.get(id) == null) return Optional.empty();
        return Optional.of(entities.get(id));
    }

    @Override
    public Iterable<E> findAll() {
        return entities.values();
    }

    @Override
    public Optional<E> save(E entity) {
        if (entity == null)
            throw new IllegalArgumentException("Error! Entity must be not null!");
        validator.validate(entity);
        if(entities.get(entity.getId()) != null) {
            return Optional.of(entity);
        }
        else entities.put(entity.getId(),entity);
        return Optional.empty();
    }

    @Override
    public Optional<E> delete(ID id) {
        E entity = null;
        if(id == null)
            throw new IllegalArgumentException("Invalid ID!");
        if (entities.containsKey(id)) {
            entity = entities.get(id);
            entities.remove(id);
        }
        if(entity == null) return Optional.empty();
        return Optional.of(entity);
    }

    @Override
    public Optional<E> update(E entity) {

        if(entity == null)
            throw new IllegalArgumentException("entity must be not null!");
        validator.validate(entity);

        entities.put(entity.getId(),entity);

        if(entities.get(entity.getId()) != null) {
            entities.put(entity.getId(),entity);
            return Optional.empty();
        }
        return Optional.of(entity);

    }
}
