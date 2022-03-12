package com.example.socialnetworkfinal.service;

import com.example.socialnetworkfinal.domain.Event;
import com.example.socialnetworkfinal.domain.User;
import com.example.socialnetworkfinal.domain.exceptions.EventException;
import com.example.socialnetworkfinal.domain.exceptions.UserException;
import com.example.socialnetworkfinal.repository.database.EventDBRepository;
import com.example.socialnetworkfinal.repository.paging.PageRepo;
import com.example.socialnetworkfinal.repository.paging.PageableRepo;
import com.example.socialnetworkfinal.repository.paging.PageableRepoImplementation;

import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.time.temporal.ChronoUnit.DAYS;

public class EventService {
    private EventDBRepository repo;
    private int page = 0;
    private int size = 1;
    private PageableRepo pageable;

    public EventService(EventDBRepository repo) {
        this.repo = repo;
    }

    public Event getEvent(Long id){
        Optional<Event> event = repo.findOne(id);
        if (event.isEmpty())
            throw new UserException("There is no event with the given id!\n");
        return event.get();
    }

    public List<Event> getAllEvents() {
        return StreamSupport.stream(repo.findAll().spliterator(), false).collect(Collectors.toList());
    }
    public List<Event> getAllNotifiableEvents() {
        //getAllEvents().forEach(x -> System.out.println(ChronoUnit.DAYS.between( LocalDate.now().atStartOfDay(),x.getStartDate().atStartOfDay())));
        return getAllEvents().stream().filter(x -> (x.getStartDate().isBefore(LocalDate.now()) && x.getEndDate().isAfter(LocalDate.now()) ) || (ChronoUnit.DAYS.between( LocalDate.now(),x.getStartDate()) <= 6L && DAYS.between( LocalDate.now(), x.getStartDate()) >= 0L) ).collect(Collectors.toList());
    }

    public List<Event> getAllUsersNotifiableEvents(Long userId) {
        return getAllNotifiableEvents().stream().filter(x -> x.getNotifications().containsKey(userId) ).filter(x -> x.getNotifications().get(userId).equals(1)).collect(Collectors.toList());
    }

    public void addEvent(Event event) {
        if(!repo.save(event).isEmpty())
            throw new EventException("The event already exists!\n");
    }

    public void updateEvent(Event event){
        if(!repo.update(event).isEmpty())
            throw new EventException("The user does not exist!\n");
    }
    public void  addAttendance(Long id_user, Long id_event){
        if(!repo.saveAttendance(id_event, id_user).isEmpty())
            throw new EventException("Attendance already exists!\n");
    }

    public void  deleteAttendance(Long id_user, Long id_event){
        if(repo.deleteAttendance(id_user, id_event).isEmpty())
            throw new EventException("Attendance doesn't exist!\n");
    }

    public void  updateAttendance(Long id_user, Long id_event, Integer value){
        if(!repo.updateNotification(id_user,id_event,value).isEmpty())
            throw new EventException("The attendance does not exist!\n");
    }

    public void setPageSize(int size) {
        this.size = size;
    }

    public void setPageable(PageableRepo pageable) {
        this.pageable = pageable;
    }

    public Set<Event> getEventsOnPage(int page) {
        this.page=page;
        PageableRepo pageable = new PageableRepoImplementation(page, this.size);
        PageRepo<Event> userPage = repo.findAll(pageable);
        return userPage.getContent().collect(Collectors.toSet());
    }

    public Set<Event> getNextEvents() {
        Set<Event> users = getEventsOnPage(this.page + 1);
        if(users.isEmpty())
            this.page--;
        return users;
    }


    public Set<Event> getPrevEvents() {
        if(page - 1 < 0)
            return new HashSet<Event>();
        return getEventsOnPage(this.page - 1);
    }
    public Event getLastUsersEvent(Long userId){
            return getAllEvents().stream()
                    .sorted(new Comparator<Event>() {
                        @Override
                        public int compare(Event o1, Event o2) {
                            return o2.getId().compareTo(o1.getId());
                        }
                    })
                    .collect(Collectors.toList()).get(0);
    }
}
