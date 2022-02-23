package com.fisglobal.offerservice.service

import com.fisglobal.offerservice.exception.CanceledOfferException
import com.fisglobal.offerservice.exception.ExpiredOfferException
import com.fisglobal.offerservice.model.Offer
import com.fisglobal.offerservice.model.OfferRequest
import com.fisglobal.offerservice.repository.OfferRepository
import spock.lang.Specification

import java.time.LocalDateTime

import static com.fisglobal.offerservice.model.Offer.Status.ACTIVE
import static com.fisglobal.offerservice.model.Offer.Status.CANCELLED

class OfferServiceSpec extends Specification {

    def offerRepository = Mock(OfferRepository)

    def "save should save the offer"() {

        given:
        def offerRequest = new OfferRequest(name: "offer1", startDate: new Date(), endDate: (LocalDateTime.now() + 5).toDate())
        def offer = Offer.builder().name(offerRequest.getName()).startDate(offerRequest.getStartDate()).endDate(offerRequest.getEndDate())
                .status(ACTIVE).build()

        when:
        def offerService = new OfferService(offerRepository: offerRepository)
        def actualOffer = offerService.save(offerRequest)

        then:
        1 * offerRepository.save(offer) >> offer
        actualOffer == offer
    }

    def "save should save the offer with EXPIRED status when the end date has passed"() {

        given:
        def offerRequest = new OfferRequest(name: "offer1", startDate: new Date(), endDate: (LocalDateTime.now() - 5).toDate())
        def offer = Offer.builder().name(offerRequest.getName()).startDate(offerRequest.getStartDate()).endDate(offerRequest.getEndDate())
                .status(Offer.Status.EXPIRED).build()

        when:
        def offerService = new OfferService(offerRepository: offerRepository)
        def actualOffer = offerService.save(offerRequest)

        then:
        1 * offerRepository.save(offer) >> offer
        actualOffer == offer
    }

    def "get should return the offer when id is valid"() {

        given:
        def offerRequest = new OfferRequest(name: "offer1", startDate: new Date(), endDate: new Date())
        def offer = Offer.builder().name(offerRequest.getName()).startDate(offerRequest.getStartDate()).endDate(offerRequest.getEndDate()).status(ACTIVE).build()

        when:
        def offerService = new OfferService(offerRepository: offerRepository)
        def actualOffer = offerService.get(1)

        then:
        1 * offerRepository.findById(1) >> Optional.of(offer)
        actualOffer.isPresent()
        actualOffer.get() == offer

    }

    def "get should return None when id does not exist"() {

        given:
        def offerRequest = new OfferRequest(name: "offer1", startDate: new Date(), endDate: new Date())

        when:
        def offerService = new OfferService(offerRepository: offerRepository)
        def actualOffer = offerService.get(1)

        then:
        1 * offerRepository.findById(1) >> Optional.empty()
        !actualOffer.isPresent()
    }

    def "get should return expired offer when the offer is expired"() {

        given:
        def offerRequest = new OfferRequest(name: "offer1", startDate: (LocalDateTime.now() - 10).toDate(), endDate: (LocalDateTime.now() - 5).toDate())
        def offer = Offer.builder().name(offerRequest.getName()).startDate(offerRequest.getStartDate()).endDate(offerRequest.getEndDate()).status(ACTIVE).build()

        when:
        def offerService = new OfferService(offerRepository: offerRepository)
        def actualOffer = offerService.get(1)

        then:
        1 * offerRepository.findById(1) >> Optional.of(offer)
        actualOffer.isPresent()
        actualOffer.get().status == Offer.Status.EXPIRED
    }

    def "get should return expired offer when the offer is already expired"() {

        given:
        def offerRequest = new OfferRequest(name: "offer1", startDate: (LocalDateTime.now() - 10).toDate(), endDate: (LocalDateTime.now() - 5).toDate())
        def offer = Offer.builder().name(offerRequest.getName()).startDate(offerRequest.getStartDate()).endDate(offerRequest.getEndDate())
                .status(Offer.Status.EXPIRED).build()

        when:
        def offerService = new OfferService(offerRepository: offerRepository)
        def actualOffer = offerService.get(1)

        then:
        1 * offerRepository.findById(1) >> Optional.of(offer)
        actualOffer.isPresent()
        actualOffer.get().status == Offer.Status.EXPIRED
    }

    def "get should expire only ACTIVE offer"() {

        given:
        def offerRequest = new OfferRequest(name: "offer1", startDate: (LocalDateTime.now() - 10).toDate(), endDate: (LocalDateTime.now() - 5).toDate())
        def offer = Offer.builder().name(offerRequest.getName()).startDate(offerRequest.getStartDate()).endDate(offerRequest.getEndDate())
                .status(CANCELLED).build()

        when:
        def offerService = new OfferService(offerRepository: offerRepository)
        def actualOffer = offerService.get(1)

        then:
        1 * offerRepository.findById(1) >> Optional.of(offer)
        actualOffer.isPresent()
        actualOffer.get().status == CANCELLED
    }

    def "cancel should cancel the offer when id is valid"() {

        given:
        def offerRequest = new OfferRequest(name: "offer1", startDate: new Date(), endDate: (LocalDateTime.now() + 5).toDate())
        def offer = Offer.builder().name(offerRequest.getName()).startDate(offerRequest.getStartDate()).endDate(offerRequest.getEndDate()).status(ACTIVE).build()

        when:
        def offerService = new OfferService(offerRepository: offerRepository)
        def actualOffer = offerService.cancel(1)

        then:
        1 * offerRepository.findById(1) >> Optional.of(offer)
        actualOffer.isPresent()
        actualOffer.get().status == CANCELLED

    }

    def "cancel should return None when id does not exist"() {

        when:
        def offerService = new OfferService(offerRepository: offerRepository)
        def actualOffer = offerService.cancel(1)

        then:
        1 * offerRepository.findById(1) >> Optional.empty()
        !actualOffer.isPresent()
    }

    def "cancel should not cancel the offer when the offer is already expired"() {

        given:
        def offerRequest = new OfferRequest(name: "offer1", startDate: (LocalDateTime.now() - 10).toDate(), endDate: (LocalDateTime.now() - 5).toDate())
        def offer = Offer.builder().name(offerRequest.getName()).startDate(offerRequest.getStartDate()).endDate(offerRequest.getEndDate())
                .status(Offer.Status.EXPIRED).build()

        when:
        def offerService = new OfferService(offerRepository: offerRepository)
        offerService.cancel(1)

        then:
        1 * offerRepository.findById(1) >> Optional.of(offer)
        def exception = thrown(ExpiredOfferException)
        exception.getMessage() == "Expired offer can not be cancelled: 1"
    }

    def "cancel should not cancel the offer when the offer is already cancelled"() {

        given:
        def offerRequest = new OfferRequest(name: "offer1", startDate: (LocalDateTime.now() - 10).toDate(), endDate: (LocalDateTime.now() - 5).toDate())
        def offer = Offer.builder().name(offerRequest.getName()).startDate(offerRequest.getStartDate()).endDate(offerRequest.getEndDate())
                .status(CANCELLED).build()

        when:
        def offerService = new OfferService(offerRepository: offerRepository)
        offerService.cancel(1)

        then:
        1 * offerRepository.findById(1) >> Optional.of(offer)
        def exception = thrown(CanceledOfferException)
        exception.getMessage() == "The Offer is already cancelled: 1"
    }
}