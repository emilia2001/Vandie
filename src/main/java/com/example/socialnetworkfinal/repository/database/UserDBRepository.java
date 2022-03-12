package com.example.socialnetworkfinal.repository.database;

import com.example.socialnetworkfinal.domain.User;
import com.example.socialnetworkfinal.domain.validators.Validator;
import com.example.socialnetworkfinal.repository.paging.PageRepo;
import com.example.socialnetworkfinal.repository.paging.PageableRepo;
import com.example.socialnetworkfinal.repository.paging.Paginator;
import com.example.socialnetworkfinal.repository.paging.PagingRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDBRepository implements PagingRepository<Long, User> {
    private String url;
    private String username;
    private String password;
    private Validator<User> validator;

    public UserDBRepository(String url, String username, String password, Validator<User> validator) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.validator = validator;
    }

    /**
     * @param id - long, the id of a user to found
     * @return Optional<User> - the user with the given id
     *                        -Optional.empty() otherwise
     */
    private Optional<User> findUser(Long id) {
        User user;
        try(Connection connection = DriverManager.getConnection(url, username, password);
            ResultSet resultSet = connection.createStatement().executeQuery(String.format("select * from users U where U.id = '%d'", id))) {
            if(resultSet.next()){
                  user = createUserFromResultSet(resultSet);
                return Optional.ofNullable(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    private User createUserFromResultSet(ResultSet resultSet) {
        try {
            String email = resultSet.getString("email");
            String firstName = resultSet.getString("first_name");
            String lastName = resultSet.getString("last_name");
            String password = resultSet.getString("password");
            String pictureUrl = resultSet.getString("picture_url");
            String coverUrl = resultSet.getString("cover_url");
            Long idd = resultSet.getLong("id");
            User user = new User(email, firstName, lastName, password);
            user.setId(idd);
            user.setProfilePictureUrl(pictureUrl);
            user.setCoverPictureUrl(coverUrl);
            return user;
        } catch (SQLException e) {
            return null;
        }
    }

    /**
     * @param id - the id of the user to be searched in friendships
     * @return List<User> - friends with the user with the given id
     */
    public List<User> getFriendsList(Long id) {
        List<User> users = new ArrayList<>();
        String state = "approved";
        try(Connection connection = DriverManager.getConnection(url, username, password);
            ResultSet resultSet = connection.createStatement().executeQuery(String.format("select * from Users U inner join (select id_friend1 from friendships where id_friend2 = '%d' and state = '%s') as F on U.id = F.id_friend1", id, state))){
                while( resultSet.next() ){
                    users.add(createUserFromResultSet(resultSet));
                }
                ResultSet resultSet2 = connection.createStatement().executeQuery(String.format("select * from Users U inner join (select id_friend2 from friendships where id_friend1 = '%d' and state = '%s') as F on U.id = F.id_friend2", id, state));
                while( resultSet2.next() ){
                    users.add(createUserFromResultSet(resultSet2));
                }
                return users;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    @Override
    public Optional<User> findOne(Long id) {
        if (id==null)
            throw new IllegalArgumentException("Id must be not null\n");
        Optional<User> user = findUser(id);
        if(!user.isEmpty())
            getFriendsList(id).forEach(user.get().getFriends()::add);
        return user;
    }

    @Override
    public Iterable<User> findAll() {
        List<User> users = new ArrayList<>();
        try(Connection connection = DriverManager.getConnection(url, username, password);

            PreparedStatement statement = connection.prepareStatement("select * from users");
            ResultSet resultSet = statement.executeQuery()){
            while( resultSet.next() ){
                User user = createUserFromResultSet(resultSet);
                getFriendsList(user.getId()).forEach(user.getFriends()::add);
                users.add(user);
            }
            return users;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    @Override
    public Optional<User> save(User user) {
        validator.validate(user);
        String sql = "insert into users (email, first_name, last_name, password, picture_url, cover_url ) values (?, ?, ?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getFirstName());
            ps.setString(3, user.getLastName());
            ps.setString(4, user.getPassword());
            ps.setString(5, user.getProfilePictureUrl());
            ps.setString(6, user.getCoverPictureUrl());
            ps.executeUpdate();
            return Optional.empty();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.ofNullable(user);
    }

    @Override
    public Optional<User> delete(Long id) {
        String sql = "delete from users where id = ?";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {
                Optional<User> user = findUser(id);
                if(!user.isEmpty()) {
                    getFriendsList(id).forEach(user.get().getFriends()::add);
                    ps.setLong(1, id);
                    ps.executeUpdate();
                }
            return user;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> update(User user) {
        if(user == null)
            throw new IllegalArgumentException("entity must be not null!");
        validator.validate(user);
        String sql = "update users set picture_url = ?, cover_url = ? where id = ?";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1,user.getProfilePictureUrl());
                ps.setString(2, user.getCoverPictureUrl());
                ps.setLong(3, user.getId());
                if( ps.executeUpdate() > 0 )
                    return Optional.empty();
                return Optional.ofNullable(user);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public Optional<User> getUserByEmail(String email) {
        User user;
        try(Connection connection = DriverManager.getConnection(url, username, password);
            ResultSet resultSet = connection.createStatement().executeQuery(String.format("select * from users U where U.email = '%s'", email))) {
            if(resultSet.next()){
                user = createUserFromResultSet(resultSet);
                return Optional.ofNullable(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public Optional<User> getUserByName(String firstName, String lastName) {
        User user;
        try(Connection connection = DriverManager.getConnection(url, username, password);
            ResultSet resultSet = connection.createStatement().executeQuery(String.format("select U.id, U.email, U.password, U.picture_url, U.cover_url from users U where U.first_name = '%s' and U.last_name = '%s'", firstName, lastName))) {
            if(resultSet.next()){
                Long idd = resultSet.getLong("id");
                String email = resultSet.getString("email");
                String userPassword = resultSet.getString("password");
                String pictureUrl = resultSet.getString("picture_url");
                String cover_url = resultSet.getString("cover_url");

                user = new User(email, firstName, lastName, userPassword);
                user.setId(idd);
                user.setProfilePictureUrl(pictureUrl);
                user.setCoverPictureUrl(cover_url);

                return Optional.ofNullable(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public PageRepo<User> findAll(PageableRepo pageable) {
        Paginator<User> paginator = new Paginator(pageable, this.findAll());
        return paginator.paginate();
    }

    public PageRepo<User> findAllFriends(PageableRepo pageable, Long id) {
        Paginator<User> paginator = new Paginator(pageable, this.getFriendsList(id));
        return paginator.paginate();
    }
}
