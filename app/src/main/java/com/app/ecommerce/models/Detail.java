package com.app.ecommerce.models;

public class Detail {
    private String Id;
    private String Name;
    private String PRID;
    private String Quantity;
    private String Code;

    public Detail(String id, String name, String PRID, String quantity, String code) {
        Id = id;
        Name = name;
        this.PRID = PRID;
        Quantity = quantity;
        Code = code;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getPRID() {
        return PRID;
    }

    public void setPRID(String PRID) {
        this.PRID = PRID;
    }

    public String getQuantity() {
        return Quantity;
    }

    public void setQuantity(String quantity) {
        Quantity = quantity;
    }

    public String getCode() {
        return Code;
    }

    public void setCode(String code) {
        Code = code;
    }
}
