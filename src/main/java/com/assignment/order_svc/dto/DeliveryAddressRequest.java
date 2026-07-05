package com.assignment.order_svc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request DTO for the delivery address within a create-order request.
 */
public class DeliveryAddressRequest {

    @NotBlank
    private String name;

    @NotBlank
    @Pattern(regexp = "\\d{10}", message = "Phone must be 10 digits")
    private String phone;

    @NotBlank
    private String addressLine1;

    private String addressLine2;

    @NotBlank
    private String city;

    @NotBlank
    private String state;

    @NotBlank
    @Pattern(regexp = "\\d{6}", message = "Pincode must be 6 digits")
    private String pincode;

    public DeliveryAddressRequest() {}

    public String getName()                  { return name; }
    public void setName(String v)            { this.name = v; }

    public String getPhone()                 { return phone; }
    public void setPhone(String v)           { this.phone = v; }

    public String getAddressLine1()          { return addressLine1; }
    public void setAddressLine1(String v)    { this.addressLine1 = v; }

    public String getAddressLine2()          { return addressLine2; }
    public void setAddressLine2(String v)    { this.addressLine2 = v; }

    public String getCity()                  { return city; }
    public void setCity(String v)            { this.city = v; }

    public String getState()                 { return state; }
    public void setState(String v)           { this.state = v; }

    public String getPincode()               { return pincode; }
    public void setPincode(String v)         { this.pincode = v; }
}
