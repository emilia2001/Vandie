package com.example.socialnetworkfinal.service;


import com.example.socialnetworkfinal.domain.User;
import com.example.socialnetworkfinal.domain.exceptions.UserException;
import com.example.socialnetworkfinal.repository.database.UserDBRepository;
import com.example.socialnetworkfinal.repository.paging.PageRepo;
import com.example.socialnetworkfinal.repository.paging.PageableRepo;
import com.example.socialnetworkfinal.repository.paging.PageableRepoImplementation;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class UserService {
    private UserDBRepository repo;
    private int page = 0;
    private int size = 1;
    private PageableRepo pageable;

    public UserService(UserDBRepository repoUser) {
        this.repo = repoUser;
    }

    /**
     * @param id - Long, the id of a user
     * @return User - the user with the given id
     * @throws UserException - if there is no user with that id
     */
    public User getUser(Long id){
        Optional<User> user = repo.findOne(id);
        if (user.isEmpty())
            throw new UserException("There is no user with the given id!\n");
        return user.get();
    }

    /**
     * @return all the users
     */
    public List<User> getAllUsers() {
        return StreamSupport.stream(repo.findAll().spliterator(), false).collect(Collectors.toList());
    }

    /**
     * @param user - user to be saved
     * @throws UserException - if the user exists already
     */
    public void addUser(User user) {
        if(!repo.save(user).isEmpty())
            throw new UserException("The user already exists!\n");
    }

    /**
     * @param id - String, id of the user to be deleted
     * @throws UserException - if the id doesn't belong to a user
     */
    public void removeUser(Long id){
        if (repo.delete(id).isEmpty())
            throw new UserException("There is no user with the given id!\n");
    }

    public void updateUser(User user){
        if(!repo.update(user).isEmpty())
            throw new UserException("The user does not exist!\n");
    }

    public User getUserByEmail(String email, String password) {
        Optional<User> user = repo.getUserByEmail(email);
        if ( !user.isEmpty() ){
            if(!user.get().getPassword().equals(password))
                throw new UserException("Incorrect password\n");
            User user1 = user.get();
            getFriendsOnPage(0, user1.getId()).forEach(user1.getFriends()::add);
            return user1;
        }
        throw new UserException("Incorrect email\n");
    }

    public User getUserByName(String firstName, String lastName){
        Optional<User> user = repo.getUserByName(firstName, lastName);
        if ( !user.isEmpty() ) {
            return user.get();
        }
        throw new UserException("Incorrect name\n");
    }

    public void setPageSize(int size) {
        this.size = size;
    }

    public void setPageable(PageableRepo pageable) {
        this.pageable = pageable;
    }

    public Set<User> getUsersOnPage(int page) {
        this.page=page;
        PageableRepo pageable = new PageableRepoImplementation(page, this.size);
        PageRepo<User> userPage = repo.findAll(pageable);
        return userPage.getContent().collect(Collectors.toSet());
    }

    public Set<User> getNextUsers() {
        Set<User> users = getUsersOnPage(this.page + 1);
        if(users.isEmpty())
            this.page--;
        return users;
    }

    public Set<User> getPrevUsers() {
        if(page - 1 < 0)
            return new HashSet<User>();
        return getUsersOnPage(this.page - 1);
    }

    public Set<User> getFriendsOnPage(int page, Long id) {
        this.page=page;
        PageableRepo pageable = new PageableRepoImplementation(page, this.size);
        PageRepo<User> userPage = repo.findAllFriends(pageable, id);
        return userPage.getContent().collect(Collectors.toSet());
    }

    public Set<User> getNextFriends(Long id) {
        Set<User> users = getFriendsOnPage(this.page + 1, id);
        if(users.isEmpty())
            this.page--;
        return users;
    }

    public Set<User> getPrevFriends(Long id) {
        if(page - 1 < 0)
            return new HashSet<User>();
        return getFriendsOnPage(this.page - 1, id);
    }

    public List<User> getAllFriends(Long id) {
        return repo.getFriendsList(id);
    }

}
