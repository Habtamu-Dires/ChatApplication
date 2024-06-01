package com.example.whatsapp.user_contact;

import com.example.whatsapp.contacts.Contacts;
import com.example.whatsapp.contacts.ContactsService;
import com.example.whatsapp.exception.ResourceNotFoundException;
import com.example.whatsapp.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserContactService {

    private final UserContactRepository userContactRepository;
    private final ContactsService contactsService;

    public void createUserContact(User user, User contactUser){
        Contacts contacts = contactsService.getContactByOwner(user);
        UserContact userContact = UserContact.builder()
                .user(contactUser)
                .contacts(contacts)
                .createdAt(LocalDateTime.now())
                .build();

        userContactRepository.save(userContact);
    }

    public List<UserContact> getUserListsByContactId(Long contact_id){
      return   userContactRepository.findByContact(contact_id);
    }

    public void removeContact(User user, User contactUser) {
        Contacts contacts = contactsService.getContactByOwner(user);
           userContactRepository
            .findByUserAndContact(contactUser.getId(), contacts.getId())
            .ifPresentOrElse(userContactRepository::delete,
                    () -> { throw new ResourceNotFoundException(
                            "Contact named " + contactUser.getUsername() + " not found");
                    }
            );
    }
}
