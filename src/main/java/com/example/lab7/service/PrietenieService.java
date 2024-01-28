package com.example.lab7.service;

import com.example.lab7.domain.FriendRequest;
import com.example.lab7.domain.Prietenie;
import com.example.lab7.domain.Tuple;
import com.example.lab7.domain.Utilizator;
import com.example.lab7.repository.Page;
import com.example.lab7.repository.Pageable;
import com.example.lab7.repository.RepoDB.PrietenieRepoDB;
import com.example.lab7.repository.RepoDB.UserRepoDB;
import com.example.lab7.utils.events.TaskChangeEvent;
import com.example.lab7.utils.observer.Observable;
import com.example.lab7.utils.observer.Observer;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PrietenieService implements Service<Tuple<Long,Long>, Prietenie>, Observable<TaskChangeEvent> {
    //PrietenieFileRepository repo;
    //UtilizatorFileRepository repoUtilizatori;
    private PrietenieRepoDB repo;
    private UserRepoDB repoUtilizatori;

    private List<Observer<TaskChangeEvent>> observers=new ArrayList<>();

    public PrietenieService(PrietenieRepoDB repo , UserRepoDB repoUtilizatori) {
        this.repo = repo;
        this.repoUtilizatori = repoUtilizatori;
        //loadFriends();
    }

    @Override
    public Iterable<Prietenie> getAll() {
        return repo.findAll();
    }

    public  Optional<Prietenie> update(Prietenie prietenie){
        return repo.update(prietenie);
    }

    @Override
    public Optional<Prietenie> getEntityById(Tuple<Long, Long> longLongTuple) {
        return repo.findOne(longLongTuple);
    }

    @Override
    public Optional<Prietenie> add(Prietenie entity) {
        Long id1 = entity.getId().getLeft();
        Long id2 = entity.getId().getRight();

        Optional<Utilizator> user1 = repoUtilizatori.findOne(id1);
        if(user1.isEmpty()){
            throw new IllegalArgumentException("Nu exista user cu id-ul " + id1);
        }

        Optional<Utilizator> user2 = repoUtilizatori.findOne(id2);
        if(user2.isEmpty()){
            throw new IllegalArgumentException("Nu exista user cu id-ul " + id2);
        }

        user1.get().addFriend(user2.get());
        user2.get().addFriend(user1.get());
        repoUtilizatori.update(user1.get());
        repoUtilizatori.update(user2.get());
        return repo.save(entity);
    }

    @Override
    public Optional<Prietenie> delete(Tuple<Long, Long> longLongTuple) {
        Long id1 = longLongTuple.getLeft();
        Long id2 = longLongTuple.getRight();
        Optional<Utilizator> u1 = repoUtilizatori.findOne(id1);
        Optional<Utilizator> u2 = repoUtilizatori.findOne(id2);

        if(u1.isEmpty() || u2.isEmpty()){
            throw new IllegalArgumentException("User inexistent!");
        }

        Optional<Prietenie> deleted = repo.delete(longLongTuple);
        /*
        if(deleted.isPresent()) {
            u1.get().removeFriend(u2.get());
            u2.get().removeFriend(u1.get());
        }*/

        return deleted;
    }

    public List<String> friendsMadeInACertainMonth(Utilizator user, Integer luna){
        List<Long> ids = repoUtilizatori.getFriendsIds(user.getId());

        List<String> result = ids.stream()
                .map(x->repo.findOne(new Tuple<Long, Long>(x,user.getId())).get()) //transform din lista de Id-uri in lista de prietenii
                .map(x ->{
                    Utilizator utilizator;
                    if(x.getId().getLeft() == user.getId()) utilizator =  repoUtilizatori.findOne(x.getId().getRight()).get();
                    else utilizator = repoUtilizatori.findOne(x.getId().getLeft()).get();

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
                    return utilizator.getLastName() + " | " + utilizator.getFirstName() + " | "
                            + x.getDate().format(formatter);
                })//transform din lista de prietenii in lista de stringuri dupa formatul cerut
                .filter(str ->{
                    String[] parts = str.split("\\|");

                    String month = parts[2].trim().substring(3,5);
                    return Integer.parseInt(month) == luna;
                })
                .collect(Collectors.toList());
        return result;
    }

    public List<Long> getFriendsIds(Long id){ return repoUtilizatori.getFriendsIds(id); }

    public Page<Long> getFriendsIds(Long id, Pageable pageable){ return repoUtilizatori.getFriendsIds(id, pageable); }

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

    public Page<Prietenie> findAll(Pageable pageable) {
        return repo.findAll(pageable);
    }
}
