package com.example.lab7.repository.RepoDB;

import com.example.lab7.domain.FriendRequest;
import com.example.lab7.domain.FriendRequestStatus;
import com.example.lab7.domain.Tuple;
import com.example.lab7.domain.validators.FriendRequestValidator;
import com.example.lab7.domain.validators.Validator;
import com.example.lab7.repository.Page;
import com.example.lab7.repository.Pageable;
import com.example.lab7.repository.PagingRepository;
import com.example.lab7.repository.Repository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class FriendRequestRepoDB implements PagingRepository<Tuple<Long,Long>, FriendRequest> {

    private final String url;
    private final String user;
    private final String password;

    private final Validator<FriendRequest> validator;

    public FriendRequestRepoDB(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
        validator = new FriendRequestValidator();
    }

    @Override
    public Optional<FriendRequest> findOne(Tuple<Long, Long> longLongTuple) {
        try(Connection connection = DriverManager.getConnection(url, user, password);
            PreparedStatement statement = connection.prepareStatement("select * from friendship_request " +
                    "where (id1 = ? and id2 = ?) or (id1 = ? and id2 = ?)")
        ){
            statement.setInt(1, Math.toIntExact(longLongTuple.getLeft()));
            statement.setInt(2, Math.toIntExact(longLongTuple.getRight()));
            statement.setInt(3, Math.toIntExact(longLongTuple.getRight()));
            statement.setInt(4, Math.toIntExact(longLongTuple.getLeft()));

            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()){
                Timestamp data = resultSet.getTimestamp("data");

                String status = resultSet.getString("status");
                FriendRequestStatus friendRequestStatus;
                if(status.equals("PENDING")) friendRequestStatus = FriendRequestStatus.PENDING;
                else if(status.equals("APPROVED")) friendRequestStatus = FriendRequestStatus.APPROVED;
                else friendRequestStatus = FriendRequestStatus.REJECTED;

                FriendRequest friendRequest = new FriendRequest(friendRequestStatus);
                friendRequest.setDate(data.toLocalDateTime());
                friendRequest.setId(new Tuple<Long, Long>(resultSet.getLong("id1"), resultSet.getLong("id2")));
                return Optional.of(friendRequest);
            }
        }
        catch(SQLException e){
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    @Override
    public Iterable<FriendRequest> findAll() {
        Set<FriendRequest> friendRequests = new HashSet<>();
        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement statement = connection.prepareStatement("select * from friendship_request");
             ResultSet resultSet = statement.executeQuery()
        ){
            while (resultSet.next())
            {
                Long id1 = resultSet.getLong("id1");
                Long id2 = resultSet.getLong("id2");
                Timestamp data = resultSet.getTimestamp("data");
                String status = resultSet.getString("status");

                FriendRequestStatus friendRequestStatus;
                if(status.equals("PENDING")) friendRequestStatus = FriendRequestStatus.PENDING;
                else if(status.equals("APPROVED")) friendRequestStatus = FriendRequestStatus.APPROVED;
                else friendRequestStatus = FriendRequestStatus.REJECTED;

                FriendRequest friendRequest = new FriendRequest(friendRequestStatus);
                friendRequest.setDate(data.toLocalDateTime());
                friendRequest.setId(new Tuple<>(id1, id2));

                friendRequests.add(friendRequest);
            }
            return friendRequests;
        }
        catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<FriendRequest> save(FriendRequest entity) {
        if(entity.getId() == null)
            throw new IllegalArgumentException("Eroare! ID-urile nu pot sa fie null!");

        validator.validate(entity);

        try(Connection connection = DriverManager.getConnection(url, user, password);
            PreparedStatement statement = connection.prepareStatement("insert into friendship_request(id1, id2, status, data) " +
                    "values(?,?,?,?)");
        ){
            statement.setInt(1,entity.getId().getLeft().intValue());
            statement.setInt(2,entity.getId().getRight().intValue());
            statement.setString(3,entity.getStatus().toString());
            statement.setTimestamp(4, Timestamp.valueOf(entity.getDate()));

            int affectedRows = statement.executeUpdate();
            return affectedRows == 0 ? Optional.of(entity) : Optional.empty();
        }
        catch(SQLException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<FriendRequest> delete(Tuple<Long, Long> longLongTuple) {
        if(longLongTuple == null)
            throw new IllegalArgumentException("Eroare! ID-ul nu poate sa fie null!");

        try(Connection connection = DriverManager.getConnection(url, user, password);
            PreparedStatement statement = connection.prepareStatement("delete from friendship_request " +
                    "where (id1 = ? AND id2 = ?) OR (id1 = ? AND id2 = ?)");
        ){
            Optional<FriendRequest> friendRequest = findOne(longLongTuple);

            statement.setInt(1, longLongTuple.getLeft().intValue());
            statement.setInt(2, longLongTuple.getRight().intValue());
            statement.setInt(3, longLongTuple.getRight().intValue());
            statement.setInt(4, longLongTuple.getLeft().intValue());
            int affectedRows = statement.executeUpdate();
            return affectedRows == 0 ? Optional.empty() : Optional.ofNullable(friendRequest.get());
        }
        catch(SQLException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<FriendRequest> update(FriendRequest entity) {
        if(entity.getId() == null)
            throw new IllegalArgumentException("Eroare! ID-urile nu pot sa fie null!");
        validator.validate(entity);

        try(Connection connection = DriverManager.getConnection(url, user, password);
            PreparedStatement statement = connection.prepareStatement("update friendship_request set status = ?, data = ? " +
                    "where (id1 = ? and id2 = ?)");
        ){
            statement.setString(1, entity.getStatus().toString());
            statement.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            statement.setInt(3,entity.getId().getLeft().intValue());
            statement.setInt(4,entity.getId().getRight().intValue());
            int affectedRows = statement.executeUpdate();
            return affectedRows == 0 ? Optional.ofNullable(entity) : Optional.empty();
        }
        catch(SQLException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public Page<FriendRequest> findAll(Pageable pageable) {
        List<FriendRequest> friendRequests = new ArrayList<>();
        try(Connection connection= DriverManager.getConnection(url, user, password);
            PreparedStatement pagePreparedStatement=connection.prepareStatement("SELECT * FROM friendship_request " +
                    "LIMIT ? OFFSET ?");

            PreparedStatement countPreparedStatement = connection.prepareStatement
                    ("SELECT COUNT(*) AS count FROM friendship_request ");

        ) {
            pagePreparedStatement.setInt(1, pageable.getPageSize());
            pagePreparedStatement.setInt(2, pageable.getPageSize() * pageable.getPageNumber());
            try (ResultSet pageResultSet = pagePreparedStatement.executeQuery();
                 ResultSet countResultSet = countPreparedStatement.executeQuery(); ) {
                while (pageResultSet.next()) {
                    Long id1 = pageResultSet.getLong("id1");
                    Long id2 = pageResultSet.getLong("id2");
                    Timestamp data = pageResultSet.getTimestamp("data");
                    String status = pageResultSet.getString("status");

                    FriendRequestStatus friendRequestStatus;
                    if(status.equals("PENDING")) friendRequestStatus = FriendRequestStatus.PENDING;
                    else if(status.equals("APPROVED")) friendRequestStatus = FriendRequestStatus.APPROVED;
                    else friendRequestStatus = FriendRequestStatus.REJECTED;

                    FriendRequest friendRequest = new FriendRequest(friendRequestStatus);
                    friendRequest.setDate(data.toLocalDateTime());
                    friendRequest.setId(new Tuple<>(id1, id2));

                    friendRequests.add(friendRequest);
                }
                int totalCount = 0;
                if(countResultSet.next()) {
                    totalCount = countResultSet.getInt("count");
                }

                return new Page<>(friendRequests, totalCount);

            }
        }
        catch (SQLException e){
            throw new RuntimeException(e);
        }
    }
}
