package com.example.whatsapp.user_contact;

import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Setter
@Getter
public class UserContactId implements Serializable {

    private Long user;
    private Long contacts;

    // Override equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserContactId that = (UserContactId) o;
        return Objects.equals(user, that.user) &&
                Objects.equals(contacts, that.contacts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, contacts);
    }
}
