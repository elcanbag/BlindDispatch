package com.blinddispatch.repository;

import com.blinddispatch.model.Message;
import com.blinddispatch.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findBySenderAndRecipientOrSenderAndRecipient(
            User sender1, User recipient1, User sender2, User recipient2);

    @Query("SELECT DISTINCT m.sender FROM Message m WHERE m.recipient = :user")
    List<User> findDistinctSendersByRecipient(@Param("user") User user);
}
