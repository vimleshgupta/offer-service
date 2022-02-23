package com.fisglobal.offerservice.resource

import com.fisglobal.offerservice.model.Error
import com.fisglobal.offerservice.model.Offer
import com.fisglobal.offerservice.model.OfferRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

import java.time.LocalDateTime

@SpringBootTest
class OfferResourceSpec extends Specification {

    @Autowired
    OfferResource offerResource

    def "should save the offer"() {

        given:
        def request = new OfferRequest(name: "offer1", startDate: new Date(), endDate: (LocalDateTime.now() + 5).toDate())

        when:
        def offer = offerResource.save(request)

        then:
        offer.body.name == request.name
        offer.body.startDate == request.startDate
        offer.body.endDate == request.endDate
        offer.body.status == Offer.Status.ACTIVE
    }

    def "should save the offer with EXPIRED status when end date is passed"() {

        given:
        def request = new OfferRequest(name: "offer1", startDate: (LocalDateTime.now() - 10).toDate(), endDate: (LocalDateTime.now() - 5).toDate())

        when:
        def offer = offerResource.save(request)

        then:
        offer.body.name == request.name
        offer.body.startDate == request.startDate
        offer.body.endDate == request.endDate
        offer.body.status == Offer.Status.EXPIRED
    }

    def "should throw an error when start date is greater than end date"() {

        given:
        def request = new OfferRequest(name: "offer1", startDate: new Date(), endDate: (LocalDateTime.now() - 5).toDate())

        when:
        def offer = offerResource.save(request)

        then:
        offer.body.message == "Start date can not be greater than end date"
        offer.body.type == Error.Type.ValidationError
    }

    def "should get the offer when id is valid"() {

        given:
        def request = new OfferRequest(name: "offer1", startDate: new Date(), endDate: (LocalDateTime.now() + 5).toDate())

        when:
        def offer = offerResource.save(request)
        offer = offerResource.get(offer.body.id)

        then:
        offer.body.name == request.name
        offer.body.startDate == request.startDate
        offer.body.endDate == request.endDate
        offer.body.status == Offer.Status.ACTIVE
    }

    def "should get the offer when id is valid and update the status to EXPIRED when the end date is passed"() {

        given:
        def request = new OfferRequest(name: "offer1", startDate: new Date(), endDate: (LocalDateTime.now() + 1).toDate())

        when:
        def offer = offerResource.save(request)
        Thread.sleep(1000);
        offer = offerResource.get(offer.body.id)

        then:
        offer.body.name == request.name
        offer.body.startDate == request.startDate
        offer.body.endDate == request.endDate
        offer.body.status == Offer.Status.EXPIRED
    }

    def "should throw an error when id is not valid"() {

        given:
        def id = 12

        when:
        def offer = offerResource.get(id)

        then:
        offer.body.message == "Id is not valid: " + id
        offer.body.type == Error.Type.OfferNotFoundError
    }

    def "cancel should cancel the offer when id is valid"() {

        given:
        def request = new OfferRequest(name: "offer1", startDate: new Date(), endDate: (LocalDateTime.now() + 5).toDate())

        when:
        def offer = offerResource.save(request)
        offer = offerResource.cancel(offer.body.id)

        then:
        offer.body.name == request.name
        offer.body.startDate == request.startDate
        offer.body.endDate == request.endDate
        offer.body.status == Offer.Status.CANCELLED
    }

    def "cancel should not cancel the offer when the offer is already cancelled"() {

        given:
        def request = new OfferRequest(name: "offer1", startDate: new Date(), endDate: (LocalDateTime.now() + 5).toDate())

        when:
        def offer = offerResource.save(request)
        def id = offer.body.id
        offerResource.cancel(id)
        // try to cancel again
        offer = offerResource.cancel(id)

        then:
        offer.body.message == "The Offer is already cancelled: " + id
        offer.body.type == Error.Type.CancelledOfferError
    }

    def "cancel should not cancel the offer when id is valid and the end date is passed"() {

        given:
        def request = new OfferRequest(name: "offer1", startDate: new Date(), endDate: (LocalDateTime.now()).toDate())

        when:
        def offer = offerResource.save(request)
        def id = offer.body.id
        Thread.sleep(500);
        offer = offerResource.cancel(id)

        then:
        offer.body.message == "Expired offer can not be cancelled: " + id
        offer.body.type == Error.Type.ExpiredOfferError
    }

    def "cancel should throw an error when id is not valid"() {

        given:
        def id = 12

        when:
        def offer = offerResource.cancel(id)

        then:
        offer.body.message == "Id is not valid: " + id
        offer.body.type == Error.Type.OfferNotFoundError
    }
}
