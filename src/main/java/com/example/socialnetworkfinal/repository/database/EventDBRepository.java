package com.example.socialnetworkfinal.repository.database;

import com.example.socialnetworkfinal.domain.Event;
import com.example.socialnetworkfinal.domain.Message;
import com.example.socialnetworkfinal.domain.User;
import com.example.socialnetworkfinal.domain.validators.Validator;
import com.example.socialnetworkfinal.repository.Repository;
import com.example.socialnetworkfinal.repository.paging.PageRepo;
import com.example.socialnetworkfinal.repository.paging.PageableRepo;
import com.example.socialnetworkfinal.repository.paging.Paginator;
import com.example.socialnetworkfinal.repository.paging.PagingRepository;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class EventDBRepository implements PagingRepository<Long, Event> {
    private String url;
    private String username;
    private String password;
    private Validator<Event> validator;

    public EventDBRepository(String url, String username, String password, Validator<Event> validator) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.validator = validator;
    }



public Optional<Event> findOne(Long id) {
    if( id == null )
        throw new IllegalArgumentException("Id must be not null\n");
    String sql = "select * from (select * from events where id = '%d') as E left join attendance on E.id = id_event";
    try(Connection connection = DriverManager.getConnection(url, username, password);
        ResultSet resultSet = connection.createStatement().executeQuery(String.format(sql, id))) {
        HashMap<Long,Integer> notifications = new HashMap<>();
        if(resultSet.next()) {
            if(resultSet.getInt("notification") != 0)
                notifications.put(resultSet.getLong("id_user"), resultSet.getInt("notification"));
            String name = resultSet.getString("name");
            LocalDate startDate = LocalDate.parse(resultSet.getString("startdate"));
            LocalDate endDate = LocalDate.parse(resultSet.getString("enddate"));
            String category = resultSet.getString("category");
            String location = resultSet.getString("location");
            String description = resultSet.getString("description");
            String pictureUrl = resultSet.getString("picture_url");
            while(resultSet.next()) {
                notifications.put(resultSet.getLong("id_user"), resultSet.getInt("notification"));
            }
            Event event = new Event(name, startDate, endDate, location, category, description);
            event.setEventPictureUrl(pictureUrl);
            event.setId(id);
            event.setNotifications(notifications);
            return Optional.ofNullable(event);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    return Optional.empty();
}

    public Optional<Event> findOne(Long id, Connection connection) {
        if( id == null )
            throw new IllegalArgumentException("Id must be not null\n");
        String sql = "select * from (select * from events where id = '%d') as E left join attendance on E.id = id_event";
        try(//Connection connection = DriverManager.getConnection(url, username, password);
            ResultSet resultSet = connection.createStatement().executeQuery(String.format(sql, id))) {
            HashMap<Long,Integer> notifications = new HashMap<>();
            if(resultSet.next()) {
                if(resultSet.getInt("notification") != 0)
                    notifications.put(resultSet.getLong("id_user"), resultSet.getInt("notification"));
                String name = resultSet.getString("name");
                LocalDate startDate = LocalDate.parse(resultSet.getString("startdate"));
                LocalDate endDate = LocalDate.parse(resultSet.getString("enddate"));
                String category = resultSet.getString("category");
                String location = resultSet.getString("location");
                String description = resultSet.getString("description");
                String pictureUrl = resultSet.getString("picture_url");
                while(resultSet.next()) {
                    notifications.put(resultSet.getLong("id_user"), resultSet.getInt("notification"));
                }
                Event event = new Event(name, startDate, endDate, location, category, description);
                event.setEventPictureUrl(pictureUrl);
                event.setId(id);
                event.setNotifications(notifications);
                return Optional.ofNullable(event);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

//    public Optional<Long> findId(LocalDate sDate, LocalDate eDate, String category, String description) {
//        String sql = "select id from events where startdate = ? and enddate = ? and category = ? and descriptiom = ? order by id desc limit 1";
//        try(Connection connection = DriverManager.getConnection(url, username, password)){
//            PreparedStatement ps = connection.prepareStatement(sql);
//            ps.setLong(1, message.getFrom().getId());
//            ps.setString(2, message.getMessage());
//            ps.setTimestamp(1, Timestamp.valueOf(sDate));
//            ps.setTimestamp(1, Timestamp.valueOf(eDate));
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//
//        return Optional.empty();
//    }


//    @Override
//    public Iterable<Event> findAll() {
//        List<Event> events = new ArrayList<>();
//        try(Connection connection = DriverManager.getConnection(url, username, password);
//
//            PreparedStatement statement = connection.prepareStatement("select * from events");
//            ResultSet resultSet = statement.executeQuery()){
//            while( resultSet.next() ){
//                Long id = resultSet.getLong("id");
//                String name = resultSet.getString("name");
//                String category = resultSet.getString("category");
//                String location = resultSet.getString("location");
//                String description = resultSet.getString("description");
//                LocalDate startDate = LocalDate.parse(resultSet.getString("startdate"));
//                LocalDate endDate = LocalDate.parse(resultSet.getString("enddate"));
//
//                Event event = new Event(name, startDate,endDate, location,category,description);
//                event.setId(id);
//                events.add(event);
//            }
//            return events;
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return events;
//    }

    @Override
    public Iterable<Event> findAll() {
        List<Event> events = new ArrayList<>();
        try(Connection connection = DriverManager.getConnection(url, username, password);
            ResultSet resultSet = connection.createStatement().executeQuery("select id from events");){

            while (resultSet.next()) {
                events.add(findOne(resultSet.getLong("id"), connection).get());
            }
            return events;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return events;
    }

    @Override
    public Optional<Event> save(Event entity) {
        validator.validate(entity);
        String sql = "insert into events (name, startdate, enddate, category, location, description, picture_url ) values (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, entity.getName());
            ps.setDate(2, Date.valueOf(entity.getStartDate()));
            ps.setDate(3, Date.valueOf(entity.getEndDate()));
            ps.setString(4, entity.getCategory());
            ps.setString(5, entity.getLocation());
            ps.setString(6, entity.getDescription());
            ps.setString(7, entity.getEventPictureUrl());
            ps.executeUpdate();
            return Optional.empty();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.ofNullable(entity);
    }

    @Override
    public Optional<Event> delete(Long aLong) {
        return Optional.empty();
    }

    @Override
    public Optional<Event> update(Event entity) {
        if(entity == null)
            throw new IllegalArgumentException("entity must be not null!");
        validator.validate(entity);
        String sql = "update users set picture_url = ? where id = ?";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1,entity.getEventPictureUrl());
            ps.setLong(2, entity.getId());
            if( ps.executeUpdate() > 0 )
                return Optional.empty();
            return Optional.ofNullable(entity);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    //nu stiu daca e bn sa pun functia asta aici si nu cred ca mai
    public Optional<Long> saveAttendance(Long id_event, Long id_user) {
        String sql = "insert into attendance (id_user, id_event ) values (?, ?)";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1,id_user);
            ps.setLong(2,id_event);
            ps.executeUpdate();
            return Optional.empty();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.ofNullable(id_event);
    }


    public Optional<Long> deleteAttendance(Long id_user, Long id_event) {
        String sql = "delete from attendance where id_user  = ? and id_event = ?";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, id_user);
            ps.setLong(2, id_event);
            ps.executeUpdate();

            return Optional.ofNullable(id_event);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public Optional<Long> updateNotification(Long id_user, Long id_event, Integer value) {
        if(value != 1 && value != -1)
            throw new IllegalArgumentException("notification value wrong!");
        String sql = "update attendance set notification = ? where id_user = ? and id_event = ?";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1,value);
            ps.setLong(2, id_user);
            ps.setLong(3, id_event);
            if( ps.executeUpdate() > 0 )
                return Optional.empty();
            return Optional.ofNullable(id_user);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public PageRepo<Event> findAll(PageableRepo pageable) {
        Paginator<Event> paginator = new Paginator(pageable, this.findAll());
        return paginator.paginate();
    }
}
