package com.fisglobal.offerservice.model

import spock.lang.Shared
import spock.lang.Specification

import javax.validation.ConstraintViolation
import javax.validation.Validation
import javax.validation.ValidatorFactory
import javax.validation.Validator

class OfferRequestSpec extends Specification {

    @Shared
    Validator validator;

    def setupSpec() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    def "should validate valid data"() {
        given:
        def customer = new OfferRequest(name: "hello", startDate: new Date(), endDate: new Date());

        when:
        Set<ConstraintViolation> violations = validator.validate(customer);

        then:
        violations.size() == 0
    }

    def "should give an error when field is invalid"() {
        given:
        def customer = new OfferRequest(name: name, startDate: startDate, endDate: endDate);

        when:
        Set<ConstraintViolation> violations = validator.validate(customer);

        then:
        violations.size() == 1
        violations[0].getProperties().get("propertyPath").toString() == path
        violations[0].getMessage() == message

        where:
        name   | startDate  | endDate    | path        | message
        ""     | new Date() | new Date() | "name"      | "Name is mandatory"
        null   | new Date() | new Date() | "name"      | "Name is mandatory"
        "name" | null       | new Date() | "startDate" | "StartDate is mandatory"
        "name" | new Date() | null       | "endDate"   | "EndDate is mandatory"
    }

    def "should give an error when all fields are invalid"() {
        given:
        def customer = new OfferRequest();

        when:
        Set<ConstraintViolation> violations = validator.validate(customer);

        then:
        violations.size() == 3
    }
}
