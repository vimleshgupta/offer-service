package com.fisglobal.offerservice.service;

import com.fisglobal.offerservice.exception.CanceledOfferException;
import com.fisglobal.offerservice.exception.ExpiredOfferException;
import com.fisglobal.offerservice.model.Offer;
import com.fisglobal.offerservice.model.OfferRequest;
import com.fisglobal.offerservice.repository.OfferRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

import static com.fisglobal.offerservice.model.Offer.Status.*;

@Service
public class OfferService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OfferService.class);

    @Autowired
    OfferRepository offerRepository;

    /**
     * Save the offer and set status ACTIVE if the end date is not passed else EXPIRED
     *
     * @param offerRequest offer
     * @return saved offer
     */
    public Offer save(OfferRequest offerRequest) {

        Offer.Status status = isExpired(offerRequest.getEndDate()) ? EXPIRED : ACTIVE;
        Offer offer = Offer.builder().name(offerRequest.getName())
                .startDate(offerRequest.getStartDate())
                .endDate(offerRequest.getEndDate())
                .status(status).build();

        LOGGER.debug("Save offer - " + offer.getName());
        return offerRepository.save(offer);
    }

    /**
     * Get the offer if it exists in the DB and expire the offer if the end date is passed
     *
     * @param id id of an offer
     * @return saved offer
     */
    public Optional<Offer> get(long id) {

        LOGGER.debug("Save offer by id - " + id);
        Optional<Offer> offer = offerRepository.findById(id);
        offer.ifPresent(this::expireOfferIfEligible);
        return offer;
    }

    /**
     * Cancel the offer if the it exists in the DB and it is not expired
     *
     * @param id id of an offer
     * @return Offer
     * @throws ExpiredOfferException  when the offer is expired
     * @throws CanceledOfferException whe the offer is already canceled
     */
    public Optional<Offer> cancel(long id) throws ExpiredOfferException, CanceledOfferException {

        LOGGER.debug("Cancel offer by id - " + id);
        Optional<Offer> optionalOffer = offerRepository.findById(id);

        if (optionalOffer.isPresent()) {
            Offer offer = optionalOffer.get();
            expireOfferIfEligible(offer);

            switch (offer.getStatus()) {
                case ACTIVE:
                    offer.setStatus(CANCELLED);
                    offerRepository.save(offer);
                    break;
                case EXPIRED:
                    throw new ExpiredOfferException("Expired offer can not be cancelled: " + id);
                case CANCELLED:
                    throw new CanceledOfferException("The Offer is already cancelled: " + id);
            }
        }
        return optionalOffer;
    }

    private boolean isExpired(Date endDate) {

        return endDate.before(new Date());
    }

    /**
     * Expire the offer if the end date has passed and status is ACTIVE
     *
     * @param offer {@code Offer)
     */
    private void expireOfferIfEligible(Offer offer) {

        if (isExpired(offer.getEndDate()) && offer.getStatus() == ACTIVE) {
            LOGGER.debug("Expire offer - " + offer.getId());
            offer.setStatus(EXPIRED);
            offerRepository.save(offer);
        }
    }

}
