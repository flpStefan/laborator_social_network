package com.example.lab7.service;

import com.example.lab7.domain.FriendRequest;
import com.example.lab7.domain.Prietenie;
import com.example.lab7.domain.Tuple;
import com.example.lab7.repository.Page;
import com.example.lab7.repository.Pageable;
import com.example.lab7.repository.RepoDB.FriendRequestRepoDB;
import com.example.lab7.repository.RepoDB.PrietenieRepoDB;
import com.example.lab7.repository.RepoDB.UserRepoDB;
import com.example.lab7.utils.events.TaskChangeEvent;
import com.example.lab7.utils.observer.Observable;
import com.example.lab7.utils.observer.Observer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FriendshipRequestService implements Service<Tuple<Long,Long>, FriendRequest>, Observable<TaskChangeEvent> {
    private FriendRequestRepoDB repo;
    private UserRepoDB userRepoDB;
    private PrietenieRepoDB prietenieRepoDB;
    private List<Observer<TaskChangeEvent>> observers=new ArrayList<>();

    public FriendshipRequestService(FriendRequestRepoDB repo, UserRepoDB repoUtilizatori, PrietenieRepoDB prietenieRepoDB) {
        this.repo = repo;
        this.userRepoDB = repoUtilizatori;
        this.prietenieRepoDB = prietenieRepoDB;
    }

    @Override
    public Optional<FriendRequest> add(FriendRequest entity) {
        Optional<Prietenie> friendship = prietenieRepoDB.findOne(entity.getId());
        if(friendship.isPresent())
            throw new IllegalArgumentException("Eroare! Nu se poate trimite cererea, sunteti deja prieteni!");

        return repo.save(entity);
    }

    @Override
    public Optional<FriendRequest> delete(Tuple<Long, Long> longLongTuple) {
        return repo.delete(longLongTuple);
    }

    @Override
    public Optional<FriendRequest> getEntityById(Tuple<Long, Long> longLongTuple) {
        return repo.findOne(longLongTuple);
    }

    @Override
    public Iterable<FriendRequest> getAll() {
        return repo.findAll();
    }

    public List<Long> getFriendRequestIds(Long id){ return userRepoDB.getFriendsIdsForFriendRequest(id); }

    public Page<Long> getFriendRequestIds(Long id, Pageable pageable){ return userRepoDB.getFriendsIdsForFriendRequest(id, pageable); }

    public Optional<FriendRequest> update(FriendRequest entity) { return repo.update(entity); }

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

    public Page<FriendRequest> findAll(Pageable pageable) {
        return repo.findAll(pageable);
    }
}
