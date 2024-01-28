package com.example.lab7.repository.RepoDB;

import com.example.lab7.domain.FriendRequest;
import com.example.lab7.domain.FriendRequestStatus;
import com.example.lab7.domain.Prietenie;
import com.example.lab7.domain.Tuple;
import com.example.lab7.domain.validators.PrietenieValidator;
import com.example.lab7.domain.validators.Validator;
import com.example.lab7.repository.Page;
import com.example.lab7.repository.Pageable;
import com.example.lab7.repository.PagingRepository;
import com.example.lab7.repository.Repository;

import java.sql.*;
import java.util.*;

public class PrietenieRepoDB implements PagingRepository<Tuple<Long, Long>, Prietenie> {

    private String url;
    private String user;
    private String password;
    private Validator<Prietenie> validator;

    public PrietenieRepoDB(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
        validator = new PrietenieValidator();
    }

    @Override
    public Optional<Prietenie> findOne(Tuple<Long, Long> longLongTuple) {
        try(Connection connection = DriverManager.getConnection(url, user, password);
            PreparedStatement statement = connection.prepareStatement("select * from friendship " +
                    "where (id1 = ? and id2 = ?) or (id1 = ? and id2 = ?)");
        ){
            statement.setInt(1, Math.toIntExact(longLongTuple.getLeft()));
            statement.setInt(2, Math.toIntExact(longLongTuple.getRight()));
            statement.setInt(3, Math.toIntExact(longLongTuple.getRight()));
            statement.setInt(4, Math.toIntExact(longLongTuple.getLeft()));

            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()){
                Timestamp friendsFrom = resultSet.getTimestamp("friendsfrom");
                Prietenie prietenie = new Prietenie(friendsFrom.toLocalDateTime());
                prietenie.setId(new Tuple<Long, Long>(resultSet.getLong("id1"), resultSet.getLong("id2")));
                return Optional.of(prietenie);
            }
        }
        catch(SQLException e){
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    @Override
    public Iterable<Prietenie> findAll() {
        Set<Prietenie> prietenii = new HashSet<>();
        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement statement = connection.prepareStatement("select * from friendship");
             ResultSet resultSet = statement.executeQuery()
        ){
            while (resultSet.next())
            {
                Long id1 = resultSet.getLong("id1");
                Long id2 = resultSet.getLong("id2");

                Timestamp friendsFrom = resultSet.getTimestamp("friendsfrom");
                Prietenie prietenie = new Prietenie(friendsFrom.toLocalDateTime());
                prietenie.setId(new Tuple<>(id1, id2));

                prietenii.add(prietenie);
            }
            return prietenii;
        }
        catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Prietenie> save(Prietenie entity) {
        if(entity.getId() == null)
            throw new IllegalArgumentException("Eroare! ID-urile nu pot sa fie null!");
        validator.validate(entity);

        try(Connection connection = DriverManager.getConnection(url, user, password);
            PreparedStatement statement = connection.prepareStatement("insert into friendship(id1, id2, friendsfrom)" +
                    "values(?,?,?)");
        ){
            statement.setInt(1,entity.getId().getLeft().intValue());
            statement.setInt(2,entity.getId().getRight().intValue());
            statement.setTimestamp(3, Timestamp.valueOf(entity.getDate()));
            int affectedRows = statement.executeUpdate();
            return affectedRows == 0 ? Optional.ofNullable(entity) : Optional.empty();
        }
        catch(SQLException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Prietenie> delete(Tuple<Long, Long> longLongTuple) {
        if(longLongTuple == null)
            throw new IllegalArgumentException("Eroare! ID-ul nu poate sa fie null!");

        try(Connection connection = DriverManager.getConnection(url, user, password);
            PreparedStatement statement = connection.prepareStatement("delete from friendship " +
                    "where (id1 = ? AND id2 = ?) OR (id1 = ? AND id2 = ?)");
        ){
            Optional<Prietenie> prietenie = findOne(longLongTuple);

            statement.setInt(1, longLongTuple.getLeft().intValue());
            statement.setInt(2, longLongTuple.getRight().intValue());
            statement.setInt(3, longLongTuple.getRight().intValue());
            statement.setInt(4, longLongTuple.getLeft().intValue());
            int affectedRows = statement.executeUpdate();
            return affectedRows == 0 ? Optional.empty() : Optional.ofNullable(prietenie.get());
        }
        catch(SQLException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Prietenie> update(Prietenie entity) {
        if(entity.getId() == null)
            throw new IllegalArgumentException("Eroare! ID-urile nu pot sa fie null!");
        validator.validate(entity);

        try(Connection connection = DriverManager.getConnection(url, user, password);
            PreparedStatement statement = connection.prepareStatement("update friendship set friendsfrom = ? " +
                    "where (id1 = ? and id2 = ?) or (id1 = ? and id2 = ?)");
        ){
            statement.setTimestamp(1,Timestamp.valueOf(entity.getDate()));
            statement.setInt(2,entity.getId().getLeft().intValue());
            statement.setInt(3,entity.getId().getRight().intValue());
            statement.setInt(4,entity.getId().getRight().intValue());
            statement.setInt(5,entity.getId().getLeft().intValue());
            int affectedRows = statement.executeUpdate();
            return affectedRows == 0 ? Optional.ofNullable(entity) : Optional.empty();
        }
        catch(SQLException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public Page<Prietenie> findAll(Pageable pageable) {
        List<Prietenie> prietenii = new ArrayList<>();
        try(Connection connection= DriverManager.getConnection(url, user, password);
            PreparedStatement pagePreparedStatement=connection.prepareStatement("SELECT * FROM friendship " +
                    "LIMIT ? OFFSET ?");

            PreparedStatement countPreparedStatement = connection.prepareStatement
                    ("SELECT COUNT(*) AS count FROM friendship ");

        ) {
            pagePreparedStatement.setInt(1, pageable.getPageSize());
            pagePreparedStatement.setInt(2, pageable.getPageSize() * pageable.getPageNumber());
            try (ResultSet pageResultSet = pagePreparedStatement.executeQuery();
                 ResultSet countResultSet = countPreparedStatement.executeQuery(); ) {
                while (pageResultSet.next()) {
                    Long id1 = pageResultSet.getLong("id1");
                    Long id2 = pageResultSet.getLong("id2");

                    Timestamp friendsFrom = pageResultSet.getTimestamp("friendsfrom");
                    Prietenie prietenie = new Prietenie(friendsFrom.toLocalDateTime());
                    prietenie.setId(new Tuple<>(id1, id2));

                    prietenii.add(prietenie);
                }
                int totalCount = 0;
                if(countResultSet.next()) {
                    totalCount = countResultSet.getInt("count");
                }

                return new Page<>(prietenii, totalCount);

            }
        }
        catch (SQLException e){
            throw new RuntimeException(e);
        }
    }
}
