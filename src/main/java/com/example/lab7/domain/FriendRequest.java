package com.example.lab7.domain;

import java.time.LocalDateTime;
import java.util.Objects;

public class FriendRequest extends Entity<Tuple<Long,Long>>{
    private FriendRequestStatus status;
    private LocalDateTime date;

    public FriendRequest(FriendRequestStatus status) {
        this.status = status;
        this.date = LocalDateTime.now();
    }

    public FriendRequestStatus getStatus() {
        return status;
    }

    public void setStatus(FriendRequestStatus status) {
        this.status = status;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        FriendRequest that = (FriendRequest) o;
        return Objects.equals(status, that.status) && Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), status, date);
    }

    @Override
    public String toString() {
        return "FriendRequest{" +
                "status='" + status + '\'' +
                ", date=" + date +
                ", id=" + id +
                '}';
    }
}
