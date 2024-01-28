package com.example.lab7.service;

import com.example.lab7.domain.Prietenie;
import com.example.lab7.domain.Utilizator;
import com.example.lab7.repository.Page;
import com.example.lab7.repository.Pageable;
import com.example.lab7.repository.RepoDB.UserRepoDB;
import com.example.lab7.utils.events.TaskChangeEvent;
import com.example.lab7.utils.observer.Observable;
import com.example.lab7.utils.observer.Observer;

import java.util.*;


public class UtilizatorService implements Service<Long, Utilizator>, Observable<TaskChangeEvent> {

    //private UtilizatorFileRepository repo;
    private UserRepoDB repo;
    private List<Observer<TaskChangeEvent>> observers=new ArrayList<>();

    public UtilizatorService(UserRepoDB repo){
        this.repo = repo;
    }

    @Override
    public Optional<Utilizator> add(Utilizator entity){
        return repo.save(entity);
    }

    @Override
    public Optional<Utilizator> delete(Long id){
        return repo.delete(id);
    }

    public Optional<Utilizator> update(Utilizator entity) { return repo.update(entity); }

    @Override
    public Iterable<Utilizator> getAll(){
        return repo.findAll();
    }

    @Override
    public Optional<Utilizator> getEntityById(Long id) {
        return repo.findOne(id);
    }

    public Optional<Utilizator> findUserByEmail(String email) { return repo.findOneByEmail(email);}

    private void DFS(Utilizator utilizator, Map<Utilizator, Integer> visited, int len)
    {
        visited.put(utilizator, len);
        List<Long> list = repo.getFriendsIds(utilizator.getId());
        list.forEach(userId ->{
            if(!visited.containsKey(repo.findOne(userId).get()) || visited.get(repo.findOne(userId).get()) == 0){
                DFS(repo.findOne(userId).get(), visited, len);
            }
        });

        /*visited.put(utilizator, len);
        utilizator.getFriends().forEach(user ->{
            if(!visited.containsKey(user) || visited.get(user) == 0){
                DFS(user, visited, len);
            }
        });*/
    }

    public int nrComunitati(){

        Map<Utilizator, Integer> visited = new HashMap<>();
        int numar_comunitati = 0;

        for(Utilizator user : getAll())
            if(!visited.containsKey(user) || visited.get(user) == 0){
                numar_comunitati++;
                DFS(user, visited, numar_comunitati);
            }

        return numar_comunitati;
    }

    private int BFS(Utilizator utilizator, Map<Utilizator, Integer> visited){

        int maxim = -1;
        Queue<Utilizator> coada = new LinkedList<>();
        coada.add(utilizator);
        visited.put(utilizator,1);

        while(!coada.isEmpty()){
            Utilizator nextUtilizator = coada.peek();
            coada.poll();
            for(Long userId : repo.getFriendsIds(nextUtilizator.getId())){
                if(!visited.containsKey(repo.findOne(userId).get()) || visited.get(repo.findOne(userId).get()) == 0) {
                    int nxt_value = visited.get(nextUtilizator) + 1;

                    if(nxt_value > maxim) maxim = nxt_value;

                    visited.put(repo.findOne(userId).get(), nxt_value);
                    coada.add(repo.findOne(userId).get());
                }
            }
        }

        return maxim;

        /*int maxim = -1;
        Queue<Utilizator> coada = new LinkedList<>();
        coada.add(utilizator);
        visited.put(utilizator,1);

        while(!coada.isEmpty()){
            Utilizator nextUtilizator = coada.peek();
            coada.poll();
            for(Utilizator user : nextUtilizator.getFriends()){
                if(!visited.containsKey(user) || visited.get(user) == 0) {
                    int nxt_value = visited.get(nextUtilizator) + 1;

                    if(nxt_value > maxim) maxim = nxt_value;

                    visited.put(user, nxt_value);
                    coada.add(user);
                }
            }
        }

        return maxim;*/
    }

    private void populeaza(Utilizator utilizator,List<Utilizator> list){
        List<Long> friendsId = repo.getFriendsIds(utilizator.getId());
        friendsId.forEach(userId ->{
            if(!list.contains(repo.findOne(userId).get())){
                list.add(repo.findOne(userId).get());
                populeaza(repo.findOne(userId).get(),list);
            }
        });

        /*utilizator.getFriends().forEach(user ->{
            if(!list.contains(user)){
                list.add(user);
                populeaza(user,list);
            }
        });*/
    }

    public List<Utilizator> comunitateaSociabila(){
        Map<Utilizator, Integer> visited = new HashMap<>();
        List<Utilizator> result = new ArrayList<>();
        int maxim = -1;

        for(Utilizator user : getAll())
            if(!visited.containsKey(user) || visited.get(user) == 0){
                int lung = BFS(user, visited);
                if(lung > maxim){
                    maxim = lung;
                    if(!result.isEmpty()) result.clear();
                    populeaza(user,result);
                }
            }

        return result;
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

    public Page<Utilizator> findAll(Pageable pageable) {
        return repo.findAll(pageable);
    }
}
