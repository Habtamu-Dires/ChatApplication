package com.example.whatsapp.contacts;

import com.example.whatsapp.exception.ResourceNotFoundException;
import com.example.whatsapp.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class ContactsService {

    private final ContactsRepository contactsRepository;


    public Contacts createNewContactContainer(User user){
      return contactsRepository.save(
              Contacts.builder()
                  .owner(user)
                  .build());
    }
    public Contacts getContactByOwner(User owner){
       return contactsRepository.findByOwner(owner.getId())
       .orElseThrow(()->new ResourceNotFoundException(
               "Contact with owner name " + owner.getUsername() + " not found"
       ));
    }

    public void deleteContact(User owner){
       Contacts contacts = getContactByOwner(owner);
       contactsRepository.delete(contacts);
    }
}
