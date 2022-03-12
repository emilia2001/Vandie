package com.example.socialnetworkfinal.service;


import com.example.socialnetworkfinal.PageController;
import com.example.socialnetworkfinal.domain.DTOFriendship;
import com.example.socialnetworkfinal.domain.Friendship;
import com.example.socialnetworkfinal.domain.Tuple;
import com.example.socialnetworkfinal.domain.User;
import com.example.socialnetworkfinal.domain.exceptions.FriendshipException;
import com.example.socialnetworkfinal.repository.Repository;
import com.example.socialnetworkfinal.repository.database.FriendshipDBRepository;
import com.example.socialnetworkfinal.repository.paging.PageRepo;
import com.example.socialnetworkfinal.repository.paging.PageableRepo;
import com.example.socialnetworkfinal.repository.paging.PageableRepoImplementation;
import com.example.socialnetworkfinal.utils.observer.FriendshipEvent;
import com.example.socialnetworkfinal.utils.observer.FriendshipEventsType;
import com.example.socialnetworkfinal.utils.observer.Observable;
import com.example.socialnetworkfinal.utils.observer.Observer;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class FriendshipService implements Observable<FriendshipEvent> {
    private FriendshipDBRepository repo;
    private List<Observer<FriendshipEvent>> observers=new ArrayList<>();
    private int page = 0;
    private int size = 1;
    private PageableRepo pageable;

    public FriendshipService(FriendshipDBRepository repoFriendship) {
        this.repo = repoFriendship;
    }

    /**
     * @param id1 - Tuple <Long, Long> ,the id of a friendship
     * @return Friendship - the friendship with de given id
     * @throws FriendshipException - if there is no friendship with that id
     */
    public Friendship getFriendship(Long id1, Long id2){
        //am modificat functia astfel incat sa imi returneze ce e in tabel intre id1 si id2, nu sa returneze doar cererea daca e acceptata
        //cum era inainte, dar oricum inainte nu o mai foloseam
        //nu e numele prea sugestiv, da am zis sa nu il schimb inca
        Long id_friend1 = id1;
        Long id_friend2 = id2;
        Optional<Friendship> friendship = repo.findOne(new Tuple<>(id_friend1, id_friend2));
        if( friendship.isPresent()) {
            if (!friendship.isEmpty())
                return friendship.get();
        }
        throw new FriendshipException("There is no friendship between these users!\n");

    }

    /**
     * @return all the friendships
     */
    public List<Friendship> getAllFriendships() {
        return StreamSupport.stream(repo.findAll().spliterator(), false)
                .filter(f -> f.getState().equals("approved"))
                .collect(Collectors.toList());
    }
    /**
     * @param id1 - String, id of the first friend
     * @param id2 - String, id of the second friend
     * @throws FriendshipException - if the friendship exists
     *                             - if one or both of the users don't exist
     */
    public void addFriendship(Long id1, Long id2) {
        if(!repo.save(new Friendship(id1, id2,"approved")).isEmpty())
            throw new FriendshipException("The friendship can not be created!\n");
    }

    public Friendship handleRemoveCancel(Long id1, Long id2) {
        Optional<Friendship> friendship1 = repo.delete(new Tuple<>(id1, id2));
        Optional<Friendship> friendship2 = repo.delete(new Tuple<>(id2, id1));
        if( friendship1.isEmpty() && friendship2.isEmpty())
            throw new FriendshipException("This friendship doesn't exist!\n");
        if(friendship1.isEmpty())
            return friendship2.get();
        return friendship1.get();
    }

    /**
     * @param id1 - String, id of the first friend
     * @param id2 - String, id of the second friend
     * @throws FriendshipException - if the friendship doesn't exist
     */
    public void removeFriendship(Long id1, Long id2) {
        Friendship friendship = handleRemoveCancel(id1, id2);
        notifyObservers(new FriendshipEvent(FriendshipEventsType.REMOVE, friendship));
    }

    public void cancelFriendsip(Long id1, Long id2) {
        Friendship friendship = handleRemoveCancel(id1, id2);
        notifyObservers(new FriendshipEvent(FriendshipEventsType.CANCEL, friendship));
    }

    public void updateFriendship(Friendship friendship) {
        if( !repo.update(friendship).isEmpty() )
            throw new FriendshipException("The friendship does not exist!\n");
    }

    public List<DTOFriendship> getUserFriends(Long id) {
        List<DTOFriendship> friends;
        friends = getAllFriendships()
                .stream()
                .filter(f -> f.getId().getRight().equals(id) || f.getId().getLeft().equals(id))
                .map(f -> new DTOFriendship(f.getFriend1().getId().equals(id) ? f.getFriend2().getLastName() : f.getFriend1().getLastName(),
                                            f.getFriend1().getId().equals(id) ? f.getFriend2().getFirstName() : f.getFriend1().getFirstName(),
                                            f.getDate(),
                                            f.getState(),
                                            f.getFriend1().getId().equals(id) ? f.getFriend2().getProfilePictureUrl() : f.getFriend1().getProfilePictureUrl()))
                .collect(Collectors.toList());
        return friends;
    }


    public List<DTOFriendship> getUserFriendsDate(Long id, int month) {
        List<DTOFriendship> friends;
        friends = getAllFriendships()
                .stream()
                .filter(f -> f.getId().getRight().equals(id) || f.getId().getLeft().equals(id))
                .filter(f -> f.getDate().getMonthValue() == month)
                .map(f -> new DTOFriendship(f.getFriend1().getId().equals(id) ? f.getFriend2().getLastName() : f.getFriend1().getLastName(),
                        f.getFriend1().getId().equals(id) ? f.getFriend2().getFirstName() : f.getFriend1().getFirstName(),
                        f.getDate(),
                        f.getState(),
                        f.getFriend1().getId().equals(id) ? f.getFriend2().getProfilePictureUrl() : f.getFriend1().getProfilePictureUrl()))
                .collect(Collectors.toList());
        return friends;
    }

    public void requestFriendship(Long id1, Long id2) {
        if ( !repo.findOne(new Tuple<>(id2,id1)).isEmpty())
            throw  new FriendshipException("You can t send a friendship request to this user!\n");
        if(!repo.save(new Friendship(id1, id2,"pending")).isEmpty())
            throw new FriendshipException("You have already sent a request to this user!\n");
        this.observers.forEach(System.out::println);
        notifyObservers(new FriendshipEvent(FriendshipEventsType.ADD, new Friendship(id1, id2, "pending")));
    }

    public List<Friendship> getAllRequests(Long id) {
        System.out.println("Requests service");
        Long start = System.currentTimeMillis();
        List<Friendship> requests =
                StreamSupport.stream(repo.getUserRequests(id).spliterator(), false)
                .collect(Collectors.toList());
        System.out.println(System.currentTimeMillis() - start);
        return requests;

    }

    public List<DTOFriendship> getApprovedRequestsInInterval(Long id, LocalDate begin, LocalDate end) {
        System.out.println(repo.getUsersApprovedRequests(id));
        return StreamSupport.stream(repo.getUsersApprovedRequests(id).spliterator(), false)
                .filter(x -> x.getDate().equals(end) || x.getDate().equals(begin) || (x.getDate().isBefore(end) && x.getDate().isAfter(begin)))
                .map(f -> new DTOFriendship(f.getId().getLeft().equals(id) ? f.getFriend2().getLastName() : f.getFriend1().getLastName(),
                        f.getId().getLeft().equals(id) ? f.getFriend2().getFirstName() : f.getFriend1().getFirstName(),
                        f.getDate(),
                        f.getState(),
                        f.getId().getLeft().equals(id) ? f.getFriend2().getProfilePictureUrl() : f.getFriend1().getProfilePictureUrl()))
                .collect(Collectors.toList());
    }

    public List<DTOFriendship> getReceivedRequests(Long id) {
        Long start = System.currentTimeMillis();
        List<Friendship> friendships = new ArrayList<>();
        List<DTOFriendship> requests =  StreamSupport.stream(repo.getUserRequests(id).spliterator(), false)
                .filter(x -> x.getId().getRight().equals(id))
                .filter(x -> !x.getState().equals("approved"))
                .map(f -> new DTOFriendship(f.getFriend1().getLastName(),
                        f.getFriend1().getFirstName(),
                        f.getDate(),
                        f.getState(),
                        f.getFriend1().getId().equals(id) ? f.getFriend2().getProfilePictureUrl() : f.getFriend1().getProfilePictureUrl()))
                .collect(Collectors.toList());
        System.out.println(System.currentTimeMillis() - start);
        return requests;

    }

    public List<DTOFriendship> getSentRequests(Long id) {
        List<Friendship> friendships = new ArrayList<>();
        repo.findFriendshipsByState("pending").forEach(friendships::add);
        repo.findFriendshipsByState("rejected").forEach(friendships::add);
        return friendships
                .stream()
                .filter(x -> x.getId().getLeft().equals(id))
                .filter(x -> !x.getState().equals("approved"))
                .map(f -> new DTOFriendship(f.getFriend2().getLastName(),
                        f.getFriend2().getFirstName(),
                        f.getDate(),
                        f.getState(),
                        f.getFriend1().getId().equals(id) ? f.getFriend2().getProfilePictureUrl() : f.getFriend1().getProfilePictureUrl()))
                .collect(Collectors.toList());
    }

    public void handleAcceptDecline(Friendship friendship) {

        Optional<Friendship> optionalFriendshipSearched = repo.findOne(new Tuple<>(friendship.getId().getLeft(),friendship.getId().getRight()));
        Friendship friendshipSearched;
        if(optionalFriendshipSearched.isPresent())
            friendshipSearched = optionalFriendshipSearched.get();
        else
            throw new FriendshipException("The request does not exist!\n");
        if ( !friendshipSearched.getState().equals("pending") )
            throw new FriendshipException("Unexisting request!\n");
        if( !repo.update(friendship).isEmpty() )
            throw new FriendshipException("The request does not exist!\n");
    }

    public void acceptFriendship(Friendship friendship) {
        handleAcceptDecline(friendship);
        notifyObservers(new FriendshipEvent(FriendshipEventsType.ACCEPT, friendship));
    }

    public void declineFriendship(Friendship friendship) {
        handleAcceptDecline(friendship);
        notifyObservers(new FriendshipEvent(FriendshipEventsType.DECLINE, friendship));
    }

    @Override
    public void addObserver(Observer<FriendshipEvent> e) {
        observers.add(e);
    }

    @Override
    public void notifyObservers(FriendshipEvent t) {
        observers.stream().forEach(o -> o.update(t));
    }

    @Override
    public void removeObserver(Observer<FriendshipEvent> e) {
        System.out.println(this.observers.remove(e));
    }

    @Override
    public void removeAAllObservers() {
        this.observers.clear();
    }


    public void setPageSize(int size) {
        this.size = size;
    }

    public void setPageable(PageableRepo pageable) {
        this.pageable = pageable;
    }

    public Set<Friendship> getReceivedOnPage(int page, Long id) {
        this.page=page;
        PageableRepo pageable = new PageableRepoImplementation(page, this.size);
        PageRepo<Friendship> requestsPage = repo.findAllReceivedRequests(pageable, id);
        return requestsPage.getContent().collect(Collectors.toSet());
    }

    public Set<Friendship> getNextReceived(Long id) {
        Set<Friendship> users = getReceivedOnPage(this.page + 1, id);
        if(users.isEmpty())
            this.page--;
        return users;
    }

    public Set<Friendship> getPrevReceived(Long id) {
        if(page - 1 < 0)
            return new HashSet<Friendship>();
        return getReceivedOnPage(this.page - 1, id);
    }

    public Set<Friendship> getSentOnPage(int page, Long id) {
        this.page=page;
        PageableRepo pageable = new PageableRepoImplementation(page, this.size);
        PageRepo<Friendship> requestsPage = repo.findAllSentRequests(pageable, id);
        return requestsPage.getContent().collect(Collectors.toSet());
    }

    public Set<Friendship> getNextSent(Long id) {
        Set<Friendship> users = getSentOnPage(this.page + 1, id);
        if(users.isEmpty())
            this.page--;
        return users;
    }

    public Set<Friendship> getPrevSent(Long id) {
        if(page - 1 < 0)
            return new HashSet<Friendship>();
        return getSentOnPage(this.page - 1, id);
    }
}
