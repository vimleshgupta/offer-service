package com.fisglobal.offerservice.model;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Getter
@Setter
public class OfferRequest {

    @NotBlank(message = "Name is mandatory")
    private String name;

    @NotNull(message = "StartDate is mandatory")
    private Date startDate;

    @NotNull(message = "EndDate is mandatory")
    private Date endDate;
}
