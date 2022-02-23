package com.fisglobal.offerservice.resource;

import com.fisglobal.offerservice.model.Error;
import com.fisglobal.offerservice.model.Offer;
import com.fisglobal.offerservice.model.OfferRequest;
import com.fisglobal.offerservice.exception.CanceledOfferException;
import com.fisglobal.offerservice.exception.ExpiredOfferException;
import com.fisglobal.offerservice.service.OfferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.springframework.http.ResponseEntity.badRequest;

@RestController
@RequestMapping("/api/v1/offer")
public class OfferResource {

    @Autowired
    OfferService offerService;

    @PostMapping
    public ResponseEntity<?> save(@Valid @RequestBody OfferRequest offerRequest) {

        if (offerRequest.getStartDate().after(offerRequest.getEndDate())) {
            return badRequest().body(new Error("Start date can not be greater than end date", Error.Type.ValidationError));
        }
        Offer offer = offerService.save(offerRequest);
        return new ResponseEntity<>(offer, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable("id") long id) {

        Optional<Offer> offer = offerService.get(id);
        if (offer.isPresent()) {
            return new ResponseEntity<>(offer.get(), HttpStatus.OK);
        } else {
            return badRequest().body(new Error("Id is not valid: " + id, Error.Type.OfferNotFoundError));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancel(@PathVariable("id") long id) {

        try {
            Optional<Offer> offer = offerService.cancel(id);
            if (offer.isPresent()) {
                return new ResponseEntity<>(offer.get(), HttpStatus.OK);
            } else {
                return badRequest().body(new Error("Id is not valid: " + id, Error.Type.OfferNotFoundError));
            }
        } catch (ExpiredOfferException ex) {
            return badRequest().body(new Error(ex.getMessage(), Error.Type.ExpiredOfferError));
        } catch (CanceledOfferException ex) {
            return badRequest().body(new Error(ex.getMessage(), Error.Type.CancelledOfferError));
        }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }
}
