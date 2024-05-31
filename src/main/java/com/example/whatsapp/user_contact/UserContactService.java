package com.example.whatsapp.user_contact;

import com.example.whatsapp.contact.Contact;
import com.example.whatsapp.contact.ContactService;
import com.example.whatsapp.exception.ResourceNotFoundException;
import com.example.whatsapp.user.User;
import com.example.whatsapp.user.UserController;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserContactService {

    private final UserContactRepository userContactRepository;
    private final ContactService contactService;

    public void createUserContact(User user, User contactUser){
        Contact contact = contactService.getContactByOwner(user);
        UserContact userContact = UserContact.builder()
                .user(contactUser)
                .contact(contact)
                .createdAt(LocalDateTime.now())
                .build();

        userContactRepository.save(userContact);
    }

    public List<UserContact> getUserListsByContactId(Long contact_id){
      return   userContactRepository.findByContact(contact_id);
    }

    public void removeContact(User user, User contactUser) {
        Contact contact = contactService.getContactByOwner(user);
           userContactRepository
            .findByUserAndContact(contactUser.getId(), contact.getId())
            .ifPresentOrElse(userContactRepository::delete,
                    () -> { throw new ResourceNotFoundException(
                            "Contact named " + contactUser.getUsername() + " not found");
                    }
            );
    }
}
