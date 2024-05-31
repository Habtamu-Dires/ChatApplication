package com.example.whatsapp.contact;

import com.example.whatsapp.exception.ResourceNotFoundException;
import com.example.whatsapp.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactRepository contactRepository;


    public Contact createNewContactContainer(User user){
      return contactRepository.save(
              Contact.builder()
                  .owner(user)
                  .build());
    }
    public Contact getContactByOwner(User owner){
       return contactRepository.findByOwner(owner.getId())
       .orElseThrow(()->new ResourceNotFoundException(
               "Contact with owner name " + owner.getUsername() + " not found"
       ));
    }

    public void deleteContact(User owner){
       Contact contact = getContactByOwner(owner);
       contactRepository.delete(contact);
    }
}
