package com.example.lab7.repository.RepoFile;


import com.example.lab7.domain.Entity;
import com.example.lab7.domain.validators.Validator;
import com.example.lab7.repository.RepoInMemory.InMemoryRepository;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


public abstract class AbstractFileRepository<ID, E extends Entity<ID>> extends InMemoryRepository<ID,E> {
    String fileName;

    public AbstractFileRepository(String fileName, Validator<E> validator) {
        super(validator);
        this.fileName = fileName;
        loadData();

    }

    private void loadData() { //decorator pattern
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String newLine;
            while ((newLine = reader.readLine()) != null) {
                List<String> data = Arrays.asList(newLine.split(";"));
                E entity = extractEntity(data);
                super.save(entity);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * extract entity  - template method design pattern
     * creates an entity of type E having a specified list of @code attributes
     *
     * @param attributes
     * @return an entity of type E
     */
    protected abstract E extractEntity(List<String> attributes);  //Template Method


    protected abstract String createEntityAsString(E entity); //Template Method

    @Override
    public Optional<E> save(E entity) {
        Optional<E> result = super.save(entity);
        if (result.isEmpty())
            writeToFile(entity);
        return result;

    }

    @Override
    public Optional<E> delete(ID id) {
        Optional<E> result = super.delete(id);
        if (result.isPresent())
            writeAllToFile();
        return result;

    }

    @Override
    public Optional<E> update(E entity){
        Optional<E> result = super.update(entity);
        if(result.isEmpty())
            writeAllToFile();
        return result;
    }

    protected void writeToFile(E entity) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {

            writer.write(createEntityAsString(entity));
            writer.newLine();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void writeAllToFile(){
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, false))) { // append == false -> clears the file before writing
            for( E entity : this.findAll()){
                writer.write(createEntityAsString(entity));
                writer.newLine();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
