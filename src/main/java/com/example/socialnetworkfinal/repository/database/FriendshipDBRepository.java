package com.example.socialnetworkfinal.repository.database;


import com.example.socialnetworkfinal.domain.Friendship;
import com.example.socialnetworkfinal.domain.Tuple;
import com.example.socialnetworkfinal.domain.User;
import com.example.socialnetworkfinal.domain.validators.Validator;
import com.example.socialnetworkfinal.repository.Repository;
import com.example.socialnetworkfinal.repository.paging.PageRepo;
import com.example.socialnetworkfinal.repository.paging.PageableRepo;
import com.example.socialnetworkfinal.repository.paging.Paginator;
import com.example.socialnetworkfinal.repository.paging.PagingRepository;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

public class FriendshipDBRepository implements PagingRepository<Tuple<Long, Long>, Friendship> {
    private String url;
    private String username;
    private String password;
    private Validator<Friendship> validator;

    public FriendshipDBRepository(String url, String username, String password, Validator<Friendship> validator) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.validator = validator;
    }

    @Override
    public Optional<Friendship> findOne(Tuple<Long, Long> id) {
        if (id==null)
            throw new IllegalArgumentException("Id must be not null\n");
        Friendship friendship;

        Long id_friend1 = id.getLeft();
        Long id_friend2 = id.getRight();
        try(Connection connection = DriverManager.getConnection(url, username, password);
            ResultSet resultSet = connection.createStatement().executeQuery(String.format("select * from friendships where id_friend1 = %d and   id_friend2 = %d", id_friend1, id_friend2))) {

            if(resultSet.next()){
                LocalDate date = LocalDate.parse(resultSet.getString("date"));
                String state = resultSet.getString("state");
                friendship = new Friendship(id_friend1, id_friend2, state);
                friendship.setDate(date);
                friendship.setFriend1(findUser(id_friend1, connection));
                friendship.setFriend2(findUser(id_friend2, connection));

                return Optional.ofNullable(friendship);

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public Iterable<Friendship> findFriendshipsByState(String state) {
        Set<Friendship> friendships = new HashSet<>();
        Map<Long, User> users = new HashMap<>();
        try(Connection connection = DriverManager.getConnection(url, username, password);
            ResultSet resultSet = connection.createStatement().executeQuery(String.format("select id_friend1, id_friend2, date from friendships where state = '%s'", state))){

            while (resultSet.next()){
                Long id1 = resultSet.getLong("id_friend1");
                Long id2 = resultSet.getLong("id_friend2");
                LocalDate date = LocalDate.parse(resultSet.getString("date"));
                Friendship friendship = new Friendship(id1, id2, state);
                friendship.setDate(date);
                if (users.containsKey(id1))
                    friendship.setFriend1(users.get(id1));
                else {
                    User user = findUser(id1, connection);
                    friendship.setFriend1(user);
                    users.put(id1, user);
                }
                if (users.containsKey(id2))
                    friendship.setFriend2(users.get(id2));
                else {
                    User user = findUser(id2, connection);
                    friendship.setFriend2(user);
                    users.put(id2, user);
                }
                friendships.add(friendship);
            }
            return friendships;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return friendships;
    }

    @Override
    public Iterable<Friendship> findAll() {
        Set<Friendship> friendships = new HashSet<>();
        Map<Long, User> users = new HashMap<>();
        try(Connection connection = DriverManager.getConnection(url, username, password);
            ResultSet resultSet = connection.createStatement().executeQuery(String.format("select * from friendships"))){

            while (resultSet.next()){
                Long id1 = resultSet.getLong("id_friend1");
                Long id2 = resultSet.getLong("id_friend2");
                LocalDate date = LocalDate.parse(resultSet.getString("date"));
                String state = resultSet.getString("state");
                Friendship friendship = new Friendship(id1, id2, state);
                friendship.setDate(date);
                if (users.containsKey(id1))
                    friendship.setFriend1(users.get(id1));
                else {
                    User user = findUser(id1, connection);
                    friendship.setFriend1(user);
                    users.put(id1, user);
                }
                if (users.containsKey(id2))
                    friendship.setFriend2(users.get(id2));
                else {
                    User user = findUser(id2, connection);
                    friendship.setFriend2(user);
                    users.put(id2, user);
                }
                friendships.add(friendship);
            }
            return friendships;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return friendships;
    }

    @Override
    public Optional<Friendship> save(Friendship friendship) {
        validator.validate(friendship);
        String sql = "insert into friendships (id_friend1, id_friend2, date, state ) values (?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            Long id1 = friendship.getId().getLeft();
            Long id2 = friendship.getId().getRight();
            ps.setLong(1, id1);
            ps.setLong(2, id2);
            ps.setDate(3, Date.valueOf(LocalDate.now()));
            ps.setString(4,friendship.getState());
            ps.executeUpdate();
            return Optional.empty();
        } catch (SQLException e) {
            return Optional.ofNullable(friendship);
        }
    }

    @Override
    public Optional<Friendship> delete(Tuple<Long, Long> id) {
        String sql = "delete from friendships where id_friend1 = ? and id_friend2 = ?";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            Long id1 = id.getLeft();
            Long id2 = id.getRight();
            ps.setLong(1, id1);
            ps.setLong(2, id2);
            Optional<Friendship> f = findOne(id);
            if (ps.executeUpdate() == 0)
                return Optional.empty();
            return f;

        } catch (SQLException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Friendship> update(Friendship friendship) {
        if(friendship == null)
            throw new IllegalArgumentException("entity must be not null!");
        validator.validate(friendship);
        String sql = "update friendships set date = ? , state = ? where id_friend1 = ? and id_friend2 = ?";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {

            Long id1 = friendship.getId().getLeft();
            Long id2 = friendship.getId().getRight();
            ps.setString(2,friendship.getState());
            ps.setLong(3, id1);
            ps.setLong(4, id2);
            ps.setDate(1, Date.valueOf(LocalDate.now()));

            if( ps.executeUpdate() > 0 )
                return Optional.empty();
            return Optional.ofNullable(friendship);

        } catch (SQLException e) {
            Optional.empty();
        }
        return Optional.empty();
    }

    /**
     * @param id - Long, id of the user
     * @return User - user with the specified id
     */
    private User findUser(Long id, Connection connection) {
        User user;
        try (//Connection connection = DriverManager.getConnection(url, username, password);
             ResultSet resultSet = connection.createStatement().executeQuery(String.format("select * from users U where U.id = '%d'", id))) {
            if (resultSet.next()) {
                String email = resultSet.getString("email");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                String userPassword = resultSet.getString("password");
                String picture_url = resultSet.getString("picture_url");

                user = new User(email, firstName, lastName, userPassword);
                user.setId(id);
                user.setProfilePictureUrl(picture_url);
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Iterable<Friendship> getUserRequests(Long id) {
        Set<Friendship> friendships = new HashSet<>();
        Map<Long, User> users = new HashMap<>();
        try(Connection connection = DriverManager.getConnection(url, username, password);
            ResultSet resultSet = connection.createStatement().executeQuery(String.format("select id_friend1, id_friend2, state, date from friendships where (id_friend1 = '%d' or id_friend2 = '%d') and (state = '%s' or state = '%s')", id, id, "pending", "rejected"))){

            while (resultSet.next()){
                Long id1 = resultSet.getLong("id_friend1");
                Long id2 = resultSet.getLong("id_friend2");
                String state = resultSet.getString("state");
                LocalDate date = LocalDate.parse(resultSet.getString("date"));
                Friendship friendship = new Friendship(id1, id2, state);
                friendship.setDate(date);
                if (users.containsKey(id1))
                    friendship.setFriend1(users.get(id1));
                else {
                    User user = findUser(id1, connection);
                    friendship.setFriend1(user);
                    users.put(id1, user);
                }
                if (users.containsKey(id2))
                    friendship.setFriend2(users.get(id2));
                else {
                    User user = findUser(id2, connection);
                    friendship.setFriend2(user);
                    users.put(id2, user);
                }
                friendships.add(friendship);
            }
            return friendships;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return friendships;

    }


    public Iterable<Friendship> getUsersApprovedRequests(Long id) {
        Set<Friendship> friendships = new HashSet<>();
        try(Connection connection = DriverManager.getConnection(url, username, password);
            ResultSet resultSet = connection.createStatement().executeQuery(String.format("select id_friend1, id_friend2, state, date from friendships where (id_friend1 = '%d' or id_friend2 = '%d') and (state = '%s')", id, id, "approved"))){

            while (resultSet.next()){
                Long id1 = resultSet.getLong("id_friend1");
                Long id2 = resultSet.getLong("id_friend2");
                String state = resultSet.getString("state");
                LocalDate date = LocalDate.parse(resultSet.getString("date"));
                Friendship friendship = new Friendship(id1, id2, state);
                if(id1.equals(id))
                    friendship.setFriend2(findUser(id2, connection));
                else
                    friendship.setFriend1(findUser(id1, connection));
                friendship.setDate(date);
                friendships.add(friendship);
            }
            return friendships;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return friendships;
    }

    public Iterable<Friendship> getUserReceivedRequests(Long id) {
        Set<Friendship> friendships = new HashSet<>();
        Map<Long, User> users = new HashMap<>();
        try(Connection connection = DriverManager.getConnection(url, username, password);
            ResultSet resultSet = connection.createStatement().executeQuery(String.format("select id_friend1, id_friend2, state, date from friendships where (id_friend2 = '%d') and (state = '%s' or state = '%s')", id,"pending", "rejected"))){

            while (resultSet.next()){
                Long id1 = resultSet.getLong("id_friend1");
                Long id2 = resultSet.getLong("id_friend2");
                String state = resultSet.getString("state");
                LocalDate date = LocalDate.parse(resultSet.getString("date"));
                Friendship friendship = new Friendship(id1, id2, state);
                friendship.setDate(date);
                if (users.containsKey(id1))
                    friendship.setFriend1(users.get(id1));
                else {
                    User user = findUser(id1, connection);
                    friendship.setFriend1(user);
                    users.put(id1, user);
                }
                if (users.containsKey(id2))
                    friendship.setFriend2(users.get(id2));
                else {
                    User user = findUser(id2, connection);
                    friendship.setFriend2(user);
                    users.put(id2, user);
                }
                friendships.add(friendship);
            }
            return friendships;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return friendships;

    }

    public Iterable<Friendship> getUserSentRequests(Long id) {
        Set<Friendship> friendships = new HashSet<>();
        Map<Long, User> users = new HashMap<>();
        try(Connection connection = DriverManager.getConnection(url, username, password);
            ResultSet resultSet = connection.createStatement().executeQuery(String.format("select id_friend1, id_friend2, state, date from friendships where (id_friend1 = '%d') and (state = '%s' or state = '%s')", id,"pending", "rejected"))){

            while (resultSet.next()){
                Long id1 = resultSet.getLong("id_friend1");
                Long id2 = resultSet.getLong("id_friend2");
                String state = resultSet.getString("state");
                LocalDate date = LocalDate.parse(resultSet.getString("date"));
                Friendship friendship = new Friendship(id1, id2, state);
                friendship.setDate(date);
                if (users.containsKey(id1))
                    friendship.setFriend1(users.get(id1));
                else {
                    User user = findUser(id1, connection);
                    friendship.setFriend1(user);
                    users.put(id1, user);
                }
                if (users.containsKey(id2))
                    friendship.setFriend2(users.get(id2));
                else {
                    User user = findUser(id2, connection);
                    friendship.setFriend2(user);
                    users.put(id2, user);
                }
                friendships.add(friendship);
            }
            return friendships;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return friendships;

    }

    @Override
    public PageRepo<Friendship> findAll(PageableRepo pageable) {
        Paginator<Friendship> paginator = new Paginator(pageable, this.findAll());
        return paginator.paginate();
    }

    public PageRepo<Friendship> findAllReceivedRequests(PageableRepo pageable, Long id) {
        Paginator<Friendship> paginator = new Paginator(pageable, this.getUserReceivedRequests(id));
        return paginator.paginate();
    }

    public PageRepo<Friendship> findAllSentRequests(PageableRepo pageable, Long id) {
        Paginator<Friendship> paginator = new Paginator(pageable, this.getUserSentRequests(id));
        return paginator.paginate();
    }
}
