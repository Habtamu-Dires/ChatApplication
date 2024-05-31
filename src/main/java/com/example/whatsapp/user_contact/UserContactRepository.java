package com.example.whatsapp.user_contact;

import com.example.whatsapp.contact.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserContactRepository extends JpaRepository<UserContact,UserContactId> {
    @Query( """
        SELECT uc FROM UserContact uc 
        WHERE uc.user.id=:user_id 
        AND uc.contact.id=:contact_id
        """
    )
    Optional<UserContact> findByUserAndContact(Long user_id, Long contact_id);

    @Query("SELECT uc FROM UserContact uc WHERE uc.contact.id=:contact_id")
    List<UserContact> findByContact(Long contact_id);
}
