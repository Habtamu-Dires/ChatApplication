package com.example.whatsapp.exception;

public class ResourceNotFoundException extends RuntimeException{

    public ResourceNotFoundException(String message){
         super(message);
    }
}
