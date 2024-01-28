package com.example.lab7.domain;

import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;


public class Prietenie extends Entity<Tuple<Long,Long>> {

    LocalDateTime friendsFrom;
    //DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");


    public Prietenie(LocalDateTime friendsFrom) {
        //friendsFrom.format(formatter);
        this.friendsFrom = friendsFrom;
    }

    /**
     *
     * @return the date when the friendship was created
     */
    public LocalDateTime getDate() {
        return friendsFrom;
    }

    @Override
    public String toString() {
        return "Prietenie{" +
                "id=" + id +
                ", friendsFrom=" + friendsFrom +
                '}';
    }
}
