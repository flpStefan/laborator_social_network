package com.example.lab7.repository.RepoDB;


import com.example.lab7.domain.PasswordEncoder;
import com.example.lab7.domain.Utilizator;
import com.example.lab7.domain.validators.UtilizatorValidator;
import com.example.lab7.domain.validators.Validator;
import com.example.lab7.repository.Page;
import com.example.lab7.repository.Pageable;
import com.example.lab7.repository.PagingRepository;

import java.sql.*;
import java.util.*;
import java.security.KeyPair;


public class UserRepoDB implements PagingRepository<Long, Utilizator> {

    private String url;
    private String user;
    private String password;
    private Validator<Utilizator> validator;

    public UserRepoDB(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
        validator = new UtilizatorValidator();
    }

    @Override
    public Optional<Utilizator> findOne(Long aLong) {
        if(aLong == null)
            throw new IllegalArgumentException("Eroare! Id-ul nu poate sa fie null!");

        try(Connection connection = DriverManager.getConnection(url, user, password);
            PreparedStatement statement = connection.prepareStatement("select * from users " +
                    "where id = ?");

        ){
            statement.setInt(1, Math.toIntExact(aLong));
            ResultSet resultSet = statement.executeQuery();

            if(resultSet.next()){
                String prenume = resultSet.getString("prenume");
                String nume = resultSet.getString("nume");
                String email = resultSet.getString("email");
                String password = resultSet.getString("password");


                Utilizator user = new Utilizator(prenume, nume, email, password);
                user.setId(aLong);
                return Optional.ofNullable(user);
            }
        }
        catch (SQLException e){
            throw new RuntimeException(e);
        }

        return Optional.empty();
    }

    @Override
    public Iterable<Utilizator> findAll() {
        Set<Utilizator> users = new HashSet<>();

        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement statement = connection.prepareStatement("select * from users");
             ResultSet resultSet = statement.executeQuery()
        ){
            while (resultSet.next())
            {
                Long id = resultSet.getLong("id");
                String prenume = resultSet.getString("prenume");
                String nume = resultSet.getString("nume");
                String email = resultSet.getString("email");
                String password = resultSet.getString("password");

                Utilizator user = new Utilizator(prenume, nume, email, password);
                user.setId(id);
                users.add(user);

            }
            return users;
        }
        catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Utilizator> save(Utilizator entity) {
        validator.validate(entity);

        /*if(findOne(entity.getId()).isPresent())
            return Optional.of(entity);*/

        try(Connection connection = DriverManager.getConnection(url, user, password);
            PreparedStatement statement = connection.prepareStatement("insert into users(prenume, nume, email, password)" +
                    "values(?,?,?,?)");
        ){

            statement.setString(1, entity.getFirstName());
            statement.setString(2, entity.getLastName());
            statement.setString(3, entity.getEmail());
            statement.setString(4, PasswordEncoder.encodePassword(entity.getPassword()));
            int affectedRows = statement.executeUpdate();
            return affectedRows == 0 ? Optional.ofNullable(entity) : Optional.empty();
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Utilizator> delete(Long aLong) {
        if(aLong == null)
            throw new IllegalArgumentException("Eroare! ID-ul nu poate sa fie null!");

        try(Connection connection = DriverManager.getConnection(url, user, password);
            PreparedStatement statement = connection.prepareStatement("delete from users " +
                    "where id = ?");
        ){
            Optional<Utilizator> user = findOne(aLong);

            statement.setInt(1, aLong.intValue());
            int affectedRows = statement.executeUpdate();
            return affectedRows == 0 ? Optional.empty() : Optional.ofNullable(user.get());
        }
        catch(SQLException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Utilizator> update(Utilizator entity) {
        if(entity.getId() == null)
            throw new IllegalArgumentException("Eroare! ID-ul nu poate sa fie null!");
        validator.validate(entity);

        try(Connection connection = DriverManager.getConnection(url, user, password);
            PreparedStatement statement = connection.prepareStatement("update users " +
                    "set prenume= ?,nume = ?, email = ?, password = ? where id = ?");
        ){

            statement.setString(1, entity.getFirstName());
            statement.setString(2, entity.getLastName());
            statement.setString(3, entity.getEmail());
            statement.setString(4, PasswordEncoder.encodePassword(entity.getPassword()));
            statement.setInt(5, entity.getId().intValue());
            int affectedRows = statement.executeUpdate();
            return affectedRows == 0 ? Optional.empty() : Optional.ofNullable(entity);
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public List<Long> getFriendsIds(Long id){

        List<Long> idList = new ArrayList<>();

        try(Connection connection = DriverManager.getConnection(url, user, password);
            PreparedStatement statement = connection.prepareStatement(
                    "select users.id, friendship.id1, friendship.id2 " +
                            "from users " +
                            "INNER JOIN friendship on (users.id = friendship.id1 or users.id = friendship.id2) " +
                            "where users.id = ?");
        ){
            statement.setInt(1,id.intValue());
            ResultSet resultSet = statement.executeQuery();

            while(resultSet.next()){
                Long id1 = resultSet.getLong("id1");
                Long id2 = resultSet.getLong("id2");

                if(id1 != id) idList.add(id1);
                else idList.add(id2);
            }
        }
        catch(SQLException e){
            throw new RuntimeException(e);
        }

        return idList;
    }

    public Page<Long> getFriendsIds(Long id, Pageable pageable){
        List<Long> idList = new ArrayList<>();

        try(Connection connection = DriverManager.getConnection(url, user, password);
            PreparedStatement statement = connection.prepareStatement(
                    "select users.id, friendship.id1, friendship.id2 " +
                            "from users " +
                            "INNER JOIN friendship on (users.id = friendship.id1 or users.id = friendship.id2) " +
                            "where users.id = ?" +
                            " LIMIT ? OFFSET ?");

            PreparedStatement countPreparedStatement = connection.prepareStatement
                    ("SELECT COUNT(*) as count " +
                            "FROM users " +
                            "INNER JOIN friendship ON (users.id = friendship.id1 OR users.id = friendship.id2) " +
                            "where users.id = ?");
        ){
            statement.setInt(1,id.intValue());
            statement.setInt(2, pageable.getPageSize());
            statement.setInt(3, pageable.getPageSize() * pageable.getPageNumber());

            countPreparedStatement.setInt(1,id.intValue());
            ResultSet resultSet = statement.executeQuery();

            while(resultSet.next()){
                Long id1 = resultSet.getLong("id1");
                Long id2 = resultSet.getLong("id2");

                if(id1 != id) idList.add(id1);
                else idList.add(id2);
            }
            ResultSet countResultSet = countPreparedStatement.executeQuery();
            int totalCount = 0;
            if(countResultSet.next()) {
                totalCount = countResultSet.getInt("count");
                if(totalCount == 0) totalCount = 1;
            }
            return new Page<>(idList, totalCount);
        }
        catch(SQLException e){
            throw new RuntimeException(e);
        }
    }

    public Optional<Utilizator> findOneByEmail(String email) {
        if(email == null)
            throw new IllegalArgumentException("Eroare! E-mail-ul nu poate sa fie null!");

        try(Connection connection = DriverManager.getConnection(url, user, password);
            PreparedStatement statement = connection.prepareStatement("select * from users " +
                    "where email = ?");

        ){
            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();

            if(resultSet.next()){
                String prenume = resultSet.getString("prenume");
                String nume = resultSet.getString("nume");
                String password = resultSet.getString("password");

                Integer integer = resultSet.getInt("id");
                Long id = integer.longValue();
                Utilizator user = new Utilizator(prenume, nume, email, password);
                user.setId(id);
                return Optional.ofNullable(user);
            }
        }
        catch(SQLException e){
            throw new RuntimeException(e);
        }

        return Optional.empty();
    }

    public List<Long> getFriendsIdsForFriendRequest(Long id){

        List<Long> idList = new ArrayList<>();

        try(Connection connection = DriverManager.getConnection(url, user, password);
            PreparedStatement statement = connection.prepareStatement(
                    "select users.id, friendship_request.id1, friendship_request.id2 " +
                            "from users " +
                            "INNER JOIN friendship_request on users.id = friendship_request.id2 " +
                            "where (users.id = ? and friendship_request.status = 'PENDING')");
        ){
            statement.setInt(1,id.intValue());
            ResultSet resultSet = statement.executeQuery();

            while(resultSet.next()){
                Long idBun = resultSet.getLong("id1");

                idList.add(idBun);
            }
        }
        catch(SQLException e){
            throw new RuntimeException(e);
        }

        return idList;
    }

    public Page<Long> getFriendsIdsForFriendRequest(Long id, Pageable pageable) {
        List<Long> idList = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement statement = connection.prepareStatement(
                     "select users.id, friendship_request.id1, friendship_request.id2 " +
                             "from users " +
                             "INNER JOIN friendship_request on users.id = friendship_request.id2 " +
                             "where (users.id = ? and friendship_request.status = 'PENDING')" +
                             " LIMIT ? OFFSET ?");

             PreparedStatement countPreparedStatement = connection.prepareStatement
                     ("SELECT COUNT(*) as count " +
                             "FROM users " +
                             "INNER JOIN friendship_request ON users.id = friendship_request.id2 " +
                             "WHERE (users.id = ? AND friendship_request.status = 'PENDING') ");
        ) {
            statement.setInt(1, id.intValue());
            statement.setInt(2, pageable.getPageSize());
            statement.setInt(3, pageable.getPageSize() * pageable.getPageNumber());

            countPreparedStatement.setInt(1, id.intValue());
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Long idBun = resultSet.getLong("id1");
                idList.add(idBun);
            }
            ResultSet countResultSet = countPreparedStatement.executeQuery();
            int totalCount = 0;
            if (countResultSet.next()) {
                totalCount = countResultSet.getInt("count");
                if(totalCount == 0) totalCount = 1;
            }
            return new Page<>(idList, totalCount);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Page<Utilizator> findAll(Pageable pageable) {
        List<Utilizator> users = new ArrayList<>();
        try(Connection connection= DriverManager.getConnection(url, user, password);
            PreparedStatement pagePreparedStatement=connection.prepareStatement("SELECT * FROM users " +
                    "LIMIT ? OFFSET ?");

            PreparedStatement countPreparedStatement = connection.prepareStatement
                    ("SELECT COUNT(*) AS count FROM users ");

        ) {
            pagePreparedStatement.setInt(1, pageable.getPageSize());
            pagePreparedStatement.setInt(2, pageable.getPageSize() * pageable.getPageNumber());
            try (ResultSet resultSet = pagePreparedStatement.executeQuery();
                 ResultSet countResultSet = countPreparedStatement.executeQuery(); ) {
                while (resultSet.next()) {
                    Long id = resultSet.getLong("id");
                    String prenume = resultSet.getString("prenume");
                    String nume = resultSet.getString("nume");
                    String email = resultSet.getString("email");
                    String password = resultSet.getString("password");

                    Utilizator user = new Utilizator(prenume, nume, email, password);

                    user.setId(id);
                    users.add(user);
                }
                int totalCount = 0;
                if(countResultSet.next()) {
                    totalCount = countResultSet.getInt("count");
                }

                return new Page<>(users, totalCount);

            }
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }
    }

}
