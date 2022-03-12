package com.example.socialnetworkfinal.service;


import com.example.socialnetworkfinal.domain.User;
import com.example.socialnetworkfinal.repository.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetworkService {
    Repository<Long, User> repoUser;

    public NetworkService(Repository<Long, User> repoUser) {
        this.repoUser = repoUser;
    }

    /**
     * @param user - user from the community to be determined
     * @param visited - Map, keeps the visited value for each user of the network
     */
    public void getUserCommunity(User user, Map<User, Boolean> visited){
        visited.replace(user, true);
        user.getFriends().forEach(u -> {
            if(!visited.get(u))
                getUserCommunity(u, visited);
        });
    }

    private void addFriends(User user){
        List<User> friends = user.getFriends();
        List<User> friends2 = new ArrayList<>();
        friends.forEach(u -> {friends2.add(repoUser.findOne(u.getId()).get());});
        user.setFriends(friends2);
    }

    public int getNrOfUsersInCommunity(User user){
        Map<User, Boolean> visited = new HashMap<>();
        repoUser.findAll().forEach(u -> visited.put(u, false));
        final Integer[] nr = {0};
        getUserCommunity(user, visited);
        visited.forEach((x,y) -> {
            if (y == true)
                nr[0]++;

        });
        return nr[0];
    }

    /**
     * @return Integer - nr of communities from the network(connected components of the graph)
     */
    public Integer getNrOfCommunities(){
        final Integer[] nr = {0};
        Map<User, Boolean> visited = new HashMap<>();
        List<User> users = new ArrayList<>();
        repoUser.findAll().forEach(users::add);
        users.forEach(u -> { addFriends(u);
                              visited.put(u, false);});
        users.forEach(u -> {
            if(!visited.get(u)){
                nr[0]++;
                getUserCommunity(u, visited);
            }
        });
        return nr[0];
    }

    public List<User> getMostSociableCommunity(){
        final Integer[] nrMax = {0};
        final Integer[] nr = {0};
        final User[] user = new User[1];
        List<User> users = new ArrayList<>();
        repoUser.findAll().forEach(users::add);
        Map<User, Boolean> visited = new HashMap<>();
        users.forEach(u -> { addFriends(u);
                             visited.put(u, false);});
        users.forEach(u -> {
            if(!visited.get(u)){
                getUserCommunity(u, visited);
                nr[0] = getNrOfUsersInCommunity(u);
                if( nr[0] > nrMax[0]){
                    nrMax[0] = nr[0];
                    user[0] = u;
                }
            }
        });
        users.forEach(u -> visited.put(u, false));
        getUserCommunity(user[0], visited);
        List<User> usersCom = new ArrayList<>();
        visited.forEach((x, y) ->{
            if(y == true)
                usersCom.add(x);
        });
        return usersCom;
    }
}
