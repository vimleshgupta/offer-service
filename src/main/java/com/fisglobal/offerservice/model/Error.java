package com.fisglobal.offerservice.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Error {

    private String message;
    private Type type;

    public enum Type {

        OfferNotFoundError,
        ValidationError,
        ExpiredOfferError,
        CancelledOfferError
    }
}

