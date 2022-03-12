package com.example.socialnetworkfinal.repository.database;


import com.example.socialnetworkfinal.domain.Message;
import com.example.socialnetworkfinal.domain.User;
import com.example.socialnetworkfinal.domain.validators.Validator;
import com.example.socialnetworkfinal.repository.Repository;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MessageDBRepository implements Repository<Long, Message> {
    private String url;
    private String username;
    private String password;
    private Validator<Message> validator;

    public MessageDBRepository(String url, String username, String password, Validator<Message> validator) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.validator = validator;
    }

    /**
     * @param id - Long, id of user
     * @return User, the user with the specified id
     */
    private Optional<User> findUser(Long id,Connection connection) {
        User user;
        try(//Connection connection = DriverManager.getConnection(url, username, password);
            ResultSet resultSet = connection.createStatement().executeQuery(String.format("select * from users U where U.id = '%d'", id))) {

            if(resultSet.next()) {
                Long idd = resultSet.getLong("id");
                String email = resultSet.getString("email");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                String userPassword = resultSet.getString("password");
                String url = resultSet.getString("picture_url");

                user = new User(email, firstName, lastName, password);
                user.setId(idd);
                user.setProfilePictureUrl(url);
                return Optional.ofNullable(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();

    }

    private Optional<Message> findRepliedTo(Long id, Connection connection) {
        Message message;
        String sql = "select * from (select * from messages where id = '%d') as M inner join recipients on M.id = id_message";
        try(//Connection connection = DriverManager.getConnection(url, username, password);
            ResultSet resultSet = connection.createStatement().executeQuery(String.format(sql, id))) {
            List<User> to = new ArrayList<>();
            if(resultSet.next()) {
                User from = findUser(resultSet.getLong("id_from"),connection).get();
                to.add(findUser(resultSet.getLong("id_to"),connection).get());
                String messageText = resultSet.getString("message");
                String dateAux = resultSet.getString("date").split("[.]")[0];
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime date = LocalDateTime.parse(dateAux, formatter);
                while(resultSet.next())
                    to.add(findUser(resultSet.getLong("id_to"),connection).get());

                message = new Message(from , to, messageText);
                message.setId(id);
                message.setDate(date);
                return Optional.ofNullable(message);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<Message> findOne(Long id) {
        if( id == null )
            throw new IllegalArgumentException("Id must be not null\n");
        Message message;
        String sql = "select * from (select * from messages where id = '%d') as M inner join recipients on M.id = id_message";
        try(Connection connection = DriverManager.getConnection(url, username, password);
            ResultSet resultSet = connection.createStatement().executeQuery(String.format(sql, id))) {
            List<User> to = new ArrayList<>();
            if(resultSet.next()) {

                User from = findUser(resultSet.getLong("id_from"),connection).get();
                to.add(findUser(resultSet.getLong("id_to"),connection).get());
                String messageText = resultSet.getString("message");
                String dateAux = resultSet.getString("date").split("[.]")[0];
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime date = LocalDateTime.parse(dateAux, formatter);
                Long reply = resultSet.getLong("reply");
                while(resultSet.next())
                    to.add(findUser(resultSet.getLong("id_to"),connection).get());

                message = new Message(from , to, messageText);
                message.setId(id);
                message.setDate(date);
                if( reply.longValue() != 0 )
                    message.setReply(findRepliedTo(reply,connection).get());
                return Optional.ofNullable(message);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public Optional<Message> findOneConnection(Long id,Connection connection) {
        if( id == null )
            throw new IllegalArgumentException("Id must be not null\n");
        Message message;
        String sql = "select * from (select * from messages where id = '%d') as M inner join recipients on M.id = id_message";
        try(//Connection connection = DriverManager.getConnection(url, username, password);
            ResultSet resultSet = connection.createStatement().executeQuery(String.format(sql, id))) {
            List<User> to = new ArrayList<>();
            if(resultSet.next()) {
                User from = findUser(resultSet.getLong("id_from"),connection).get();
                to.add(findUser(resultSet.getLong("id_to"),connection).get());
                String messageText = resultSet.getString("message");
                String dateAux = resultSet.getString("date").split("[.]")[0];
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime date = LocalDateTime.parse(dateAux, formatter);
                Long reply = resultSet.getLong("reply");
                while(resultSet.next())
                    to.add(findUser(resultSet.getLong("id_to"),connection).get());

                message = new Message(from , to, messageText);
                message.setId(id);
                message.setDate(date);
                if( reply.longValue() != 0 )
                    message.setReply(findRepliedTo(reply,connection).get());
                return Optional.ofNullable(message);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Iterable<Message> findAll() {
        List<Message> messages = new ArrayList<>();
        try(Connection connection = DriverManager.getConnection(url, username, password);
            ResultSet resultSet = connection.createStatement().executeQuery("select id from messages");){

            while (resultSet.next()) {
                messages.add(findOneConnection(resultSet.getLong("id"),connection).get());
            }
            return messages;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    @Override
    public Optional<Message> save(Message message) {
        validator.validate(message);
        String sql = "insert into messages (id_from, message, date, reply ) values (?, ?, ?, ?)";
        String sql3 = "select id from messages where id_from = ? and message = ? and date = ? order by id desc limit 1";
        String sql2 = "insert into recipients (id_message, id_to) values (?, ?)";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql);
             PreparedStatement ps2 = connection.prepareStatement(sql2);
             PreparedStatement ps3 = connection.prepareStatement(sql3)) {

            LocalDateTime dateAux = LocalDateTime.now();
            ps.setLong(1, message.getFrom().getId());
            ps.setString(2, message.getMessage());
            ps.setTimestamp(3, Timestamp.valueOf(dateAux));
            if(message.getReply() != null)
                ps.setLong(4, message.getReply().getId());
            else
                ps.setLong(4, 0);
            ps.executeUpdate();

            ps3.setLong(1, message.getFrom().getId());
            ps3.setString(2, message.getMessage());
            ps3.setTimestamp(3, Timestamp.valueOf(dateAux));
            ResultSet resultSet = ps3.executeQuery();
            if(resultSet.next())
                ps2.setLong(1, resultSet.getLong("id"));
                message.getTo().forEach(t -> {
                    try {
                        ps2.setLong(2, t.getId());
                        ps2.executeUpdate();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } });

            return Optional.empty();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.ofNullable(message);
    }

    @Override
    public Optional<Message> delete(Long aLong) {
        return Optional.empty();
    }

    @Override
    public Optional<Message> update(Message entity) {
        return Optional.empty();
    }
}
