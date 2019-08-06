package com.example.appchat.objectclass;

import java.io.Serializable;

public class Avatar implements Serializable {
    private String valueBase64;
    private String name;

    public Avatar(String valueBase64, String name) {
        this.valueBase64 = valueBase64;
        this.name = name;
    }

    public String getValueBase64() {
        return valueBase64;
    }

    public void setValueBase64(String valueBase64) {
        this.valueBase64 = valueBase64;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
