package com.example.lab7.ui;

import com.example.lab7.domain.Prietenie;
import com.example.lab7.domain.Tuple;
import com.example.lab7.domain.Utilizator;
import com.example.lab7.repository.RepoDB.PrietenieRepoDB;
import com.example.lab7.repository.RepoDB.UserRepoDB;
import com.example.lab7.service.PrietenieService;
import com.example.lab7.service.UtilizatorService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Scanner;

public class UI {

    //UtilizatorFileRepository userFileRepo = new UtilizatorFileRepository("D:\\Laboratoare\\MAP\\lab6\\lab6\\src\\main\\java\\socialnetwork\\repository\\RepoFile\\Users.txt", new UtilizatorValidator());
    //PrietenieFileRepository prietenieFileRepository = new PrietenieFileRepository("D:\\Laboratoare\\MAP\\lab6\\lab6\\src\\main\\java\\socialnetwork\\repository\\RepoFile\\Prietenii.txt", new PrietenieValidator());
    UserRepoDB userRepoDB = new UserRepoDB("jdbc:postgresql://localhost:5432/socialnetwork","postgres","anoelf");
    PrietenieRepoDB prietenieRepoDB = new PrietenieRepoDB("jdbc:postgresql://localhost:5432/socialnetwork","postgres","anoelf");
    UtilizatorService utilizatorService = new UtilizatorService(userRepoDB);
    PrietenieService prietenieService = new PrietenieService(prietenieRepoDB, userRepoDB);

    public static UI instance;
    public static UI getInstance(){
        if(instance == null) instance = new UI();
        return instance;
    }
    private UI(){}

    public void run(){
        while(true){
            meniu();
            Scanner in = new Scanner(System.in);

            int option = in.nextInt();
            if(option == 1) addUser();
            else if(option == 2) deleteUser();
            else if(option == 3) printAllUsers();
            else if(option == 4) createFriendship();
            else if(option == 5) deleteFriendship();
            else if(option == 6) printAllPrietenii();
            else if(option == 7) System.out.println("In retea sunt " + utilizatorService.nrComunitati() + " comunitati distincte!");
            else if(option == 8) {
                System.out.println("Cea mai sociabila retea este formata din: ");
                utilizatorService.comunitateaSociabila().forEach(x -> System.out.println("Utilizatorul "
                        + x.getLastName() + " " + x.getFirstName()));
            }
            else if(option == 9) friendsFrom();
            else if (option == 0){
                break;
            }
        }
    }

    private void meniu(){
        System.out.println("##      MENIU      ##");
        System.out.println("1 -> Adauga Utilizator");
        System.out.println("2 -> Sterge Utilizator");
        System.out.println("3 -> Afiseaza Utilizatori\n");
        System.out.println("4 -> Creeaza prietenie");
        System.out.println("5 -> Sterge prietenie");
        System.out.println("6 -> Afiseaza prietenii\n");
        System.out.println("7 -> Numar comunitati");
        System.out.println("8 -> Cea mai sociabila comunitate\n");
        System.out.println("9 -> Prietenii unui utilizator dintr-o luna anume");
        System.out.println("0 -> Exit");
    }

    private void addUser() {
        Scanner in = new Scanner(System.in);
        System.out.println("Prenume: ");
        String fName = in.next();
        System.out.println("Nume: ");
        String lName = in.next();
        System.out.println("Email: ");
        String email = in.next();
        System.out.println("Password: ");
        String password = in.next();
        Utilizator user = new Utilizator(fName, lName, email, password);
        try {
            Optional<Utilizator> addedUser = utilizatorService.add(user);
            if (addedUser.isEmpty()) {
                System.out.println("Utilizatorul " + fName + " " + lName + " a fost salvat cu succes!");
            } else {
                System.out.println("Exista deja un utilizator cu email-ul " + email);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void deleteUser() {
        Scanner in = new Scanner(System.in);
        System.out.println("ID: ");
        Long id = in.nextLong();
        try {

            Optional<Utilizator> deletedUser = utilizatorService.delete(id);
            if (deletedUser.isPresent()) {
                System.out.println("Utilizatorul " + deletedUser.get().getFirstName() + " " + deletedUser.get().getLastName() + " a fost sters!");
            } else {
                System.out.println("Nu exista niciun utilizatorul cu ID-ul " + id);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void printAllUsers(){
        Iterable<Utilizator> allUsers = utilizatorService.getAll();
        allUsers.forEach(user -> System.out.println("Utilizatorul: " + user.getFirstName() + " " + user.getLastName()));

        /*for(Utilizator user : allUsers) {
            System.out.println("Utilizatorul: " + user.getFirstName() + " " + user.getLastName());
        }*/
    }

    private void printAllPrietenii(){
        Iterable<Prietenie> allPrieteni = prietenieService.getAll();
        allPrieteni.forEach(prietenie -> {
            String user1 = utilizatorService.getEntityById(prietenie.getId().getLeft()).get().getFirstName() + " " + utilizatorService.getEntityById(prietenie.getId().getLeft()).get().getLastName();
            String user2 = utilizatorService.getEntityById(prietenie.getId().getRight()).get().getFirstName() + " " + utilizatorService.getEntityById(prietenie.getId().getRight()).get().getLastName();
            System.out.println("Utilizataorul " + user1 + " este prieten cu " + user2 + " din data: " + prietenie.getDate());
        });

        //for(Prietenie prietenie : allPrieteni)
        //    System.out.println(utilizatorService.getEntityById(prietenie.getId().getLeft()) + " este prieten cu " + utilizatorService.getEntityById(prietenie.getId().getRight()));
    }

    private void createFriendship(){
        Scanner in = new Scanner(System.in);
        System.out.println("ID user 1: ");
        Long id1 = in.nextLong();
        System.out.println("ID user 2: ");
        Long id2 = in.nextLong();

        in.nextLine();
        System.out.println("Data imprietenirii(yyyy-MM-dd HH:mm:ss): ");
        String date = in.nextLine();

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            Prietenie newFriendship = new Prietenie(LocalDateTime.parse(date, formatter));
            newFriendship.setId(new Tuple(id1, id2));
            Optional<Prietenie> prietenieAdd = prietenieService.add(newFriendship);
            if(prietenieAdd.isEmpty()) System.out.println("Prietenie creata cu succes!");
        }
        catch (Exception e) { System.out.println(e.getMessage()); }
    }

    private void deleteFriendship(){
        Scanner in = new Scanner(System.in);
        System.out.println("ID utilizator 1: ");
        Long id1 = in.nextLong();
        System.out.println("ID utilizator 2:");
        Long id2 = in.nextLong();
        try{
            Optional<Prietenie> deletedPrietenie = prietenieService.delete(new Tuple<>(id1, id2));

            if(deletedPrietenie.isPresent())
                System.out.println("Prietenia a fost stearsa cu succes!");
            else System.out.println("Eroare! Utilizatorii nu sunt prieteni");

        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    private void friendsFrom(){
        Scanner in = new Scanner(System.in);
        System.out.println("ID utilizatorului: ");
        Long id = in.nextLong();
        System.out.println("Numarul lunii: ");
        Integer luna = in.nextInt();

        if(luna < 0 || luna > 12){
            System.out.println("Numarul lunii este invalid!");
            return;
        }

        try{
            Optional<Utilizator> utilizator = utilizatorService.getEntityById(id);
            if(utilizator.isPresent()) {
                System.out.println("Utilizatorii cu care s-a imprietenit in luna data sunt: ");
                prietenieService.friendsMadeInACertainMonth(utilizator.get(), luna).forEach(System.out::println);
            }
            else System.out.println("Eroare! Utilizatorul nu exista!");
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }
}
