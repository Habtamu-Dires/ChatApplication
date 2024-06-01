package com.example.whatsapp.contacts;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ContactsRepository extends JpaRepository<Contacts, Long> {
    @Query("SELECT c FROM Contacts c WHERE c.owner.id=:id")
    Optional<Contacts> findByOwner(Long id);
}
