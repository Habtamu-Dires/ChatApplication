package com.example.whatsapp.exception;

public class InvalidRequestException extends RuntimeException{

    public InvalidRequestException(String message){
        super(message);
    }
}
