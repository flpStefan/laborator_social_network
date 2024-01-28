package com.example.lab7.service;


import com.example.lab7.domain.Message;
import com.example.lab7.domain.Utilizator;
import com.example.lab7.repository.Page;
import com.example.lab7.repository.Pageable;
import com.example.lab7.repository.RepoDB.MessageRepoDB;
import com.example.lab7.repository.RepoDB.UserRepoDB;
import com.example.lab7.utils.events.TaskChangeEvent;
import com.example.lab7.utils.observer.Observable;
import com.example.lab7.utils.observer.Observer;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class MessageService implements Service<Long, Message>, Observable<TaskChangeEvent> {

    private MessageRepoDB repo;
    private UserRepoDB repoUtilizatori;

    private List<Observer<TaskChangeEvent>> observers=new ArrayList<>();

    public MessageService(MessageRepoDB repo , UserRepoDB repoUtilizatori) {
        this.repo = repo;
        this.repoUtilizatori = repoUtilizatori;
    }

    public ArrayList<Message> getMessagesBetweenTwoUsers(Long userId1, Long userId2) {
        if (repoUtilizatori.findOne(userId1).isEmpty() || repoUtilizatori.findOne(userId2).isEmpty())
            return new ArrayList<>();

        Collection<Message> messages = (Collection<Message>) repo.findAll();
        return messages.stream()
                .filter(x -> (x.getFrom().getId().equals(userId1) && x.getTo().getId().equals(userId2)) ||
                        (x.getFrom().getId().equals(userId2) && x.getTo().getId().equals(userId1)))
                .sorted(Comparator.comparing(Message::getDate))
                .collect(Collectors.toCollection(ArrayList::new));
    }


    public boolean addMessage(Long fromUserId, Long toUserId, String message) {
        try {
            Utilizator from = repoUtilizatori.findOne(fromUserId).orElse(null);
            Utilizator to = repoUtilizatori.findOne(toUserId).orElse(null);

            if (from == null || to == null)
                throw new Exception("Utilizatorul nu există");
            if (Objects.equals(message, ""))
                throw new Exception("Mesajul este gol");

            Message msg = new Message(from, to, message);
            repo.save(msg);


            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }


    @Override
    public Optional<Message> add(Message entity) {
        return repo.save(entity);
    }

    @Override
    public Optional<Message> delete(Long id) {
        return repo.delete(id);
    }

    @Override
    public Optional<Message> getEntityById(Long aLong) {
        return repo.findOneWithoutReply(aLong);
    }

    @Override
    public Iterable<Message> getAll() {
        return repo.findAll();
    }

    @Override
    public void addObserver(Observer<TaskChangeEvent> e) {
        observers.add(e);
    }

    @Override
    public void removeObserver(Observer<TaskChangeEvent> e) {
        observers.remove(e);
    }

    @Override
    public void notifyObservers(TaskChangeEvent t) {
        observers.forEach(x->x.update(t));
    }

    public Page<Message> getAllBetweenUsers(Pageable pageable, Long user1, Long user2){ return repo.getAllBetweenUsers(pageable, user1, user2); }
}
