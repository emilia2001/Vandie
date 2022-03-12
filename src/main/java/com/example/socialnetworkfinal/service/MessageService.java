package com.example.socialnetworkfinal.service;


import com.example.socialnetworkfinal.domain.DTOMessage;
import com.example.socialnetworkfinal.domain.Message;
import com.example.socialnetworkfinal.domain.User;
import com.example.socialnetworkfinal.domain.exceptions.MessageException;
import com.example.socialnetworkfinal.repository.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class MessageService {
    private Repository<Long, Message> repo;

    public MessageService(Repository<Long, Message> repo) {
        this.repo = repo;
    }

    public Message findMessage(Long id) {
        Optional<Message> message = repo.findOne(id);
        if( message.isPresent() )
            return message.get();
        throw new MessageException("There is no message with the given id!\n");
    }

    public void sendMessage(User from, List<User> to, String message) {
//        if ( !from.getFriends().containsAll(to) )
//            throw new MessageException("You can't send messages to users you aren't friend with!\n");
        repo.save(new Message(from , to, message));
    }

    public void replyToMessage(User from, String messageText, Long idReply) {
        Message reply = findMessage(idReply);
        if( !reply.getTo().contains(from) )
            throw new MessageException("Stop talking alone...!\n");
        List<User> to = reply.getTo();
        to.remove(from);
        to.add(reply.getFrom());
        Message message = new Message(from, to, messageText);
        message.setReply(reply);
        repo.save(message);
    }

    public List<Message> getConversation(Long id1, Long id2) {
        List<Message> messages = new ArrayList<>();
        repo.findAll().forEach(messages::add);
        List<Message> result;
        result = messages.stream()
                .filter(m -> ( m.getFrom().getId().equals(id1) && m.getTo().size() == 1 && m.getTo().get(0).getId().equals(id2) )
                        || (m.getFrom().getId().equals(id2) && m.getTo().size() == 1 && m.getTo().get(0).getId().equals(id1) ))
                .sorted(Comparator.comparing(Message::getDate))
                .collect(Collectors.toList());
        return result;
    }

    public List<Message> getGroupConversation(List<Long> ids){
        List<Message> messages = new ArrayList<>();
        repo.findAll().forEach(messages::add);

        List<Message> result = messages.stream()
                .filter(m -> (ids.contains(m.getFrom().getId())))
                .filter(m -> m.getTo().size()==ids.size() - 1)
                .filter(m -> (m.getTo().stream().allMatch(x -> ids.contains(x.getId()))))
                .sorted(Comparator.comparing(Message::getDate))
                .collect(Collectors.toList());
        for (Message elems:result){
            System.out.println(elems);
        }
        return result;
    }
    public List<Message> getUserMessages(Long id) {

        return StreamSupport.stream(repo.findAll().spliterator(), false)
                .filter(m -> m.getFrom().getId().equals(id) || m.getTo().stream().filter(u -> u.getId().equals(id)).count() == 1)
                .collect(Collectors.toList());
    }

    public Message getLastMessageSent(Long id) {

        return StreamSupport.stream(repo.findAll().spliterator(), false)
                .filter(m -> m.getFrom().getId().equals(id))
                .sorted(new Comparator<Message>() {
                    @Override
                    public int compare(Message o1, Message o2) {
                        return o2.getDate().compareTo(o1.getDate());
                    }
                })
                .toList().get(0);
    }

    public List<DTOMessage> getReceivedMessagesInInterval(Long id, LocalDateTime begin, LocalDateTime end) {
        Predicate<Message> received = m -> m.getTo().stream().filter(u -> u.getId().equals(id)).count() == 1;
        Predicate<Message> interval = m -> m.getDate().isBefore(end) && m.getDate().isAfter(begin);
        return StreamSupport.stream(repo.findAll().spliterator(), false)
                .filter(received.and(interval))
                .map(m -> new DTOMessage(m.getFrom().toString(), m.getMessage(), m.getDate()))
                .collect(Collectors.toList());
    }

    public List<DTOMessage> getReceivedByUserInInterval(Long id1, Long id2, LocalDateTime begin, LocalDateTime end) {
        Predicate<Message> received = m -> m.getFrom().getId().equals(id2);
        Predicate<Message> interval = m -> m.getDate().isBefore(end) && m.getDate().isAfter(begin);
        return getConversation(id1, id2)
                .stream()
                .filter(received.and(interval))
                .map(m -> new DTOMessage(m.getFrom().toString(), m.getMessage(), m.getDate()))
                .collect(Collectors.toList());
    }
}
