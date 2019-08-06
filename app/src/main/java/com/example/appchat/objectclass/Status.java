package com.example.appchat.objectclass;

import java.util.ArrayList;
import java.util.List;

public class Status {
    private int id;
    private Contact userid;
    private long createdate;
    private String body;
    private List<Avatar> list;
    private List<String> urlImages;

    public Status() {
        list = new ArrayList<>();
        urlImages = new ArrayList<>();
    }

    public Status(int id, Contact userid, int createdate, String body, List<Avatar> list) {
        this.id = id;
        this.userid = userid;
        this.createdate = createdate;
        this.body = body;
        this.list = list;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Contact getUserid() {
        return userid;
    }

    public void setUserid(Contact userid) {
        this.userid = userid;
    }

    public long getCreatedate() {
        return createdate;
    }

    public void setCreatedate(long createdate) {
        this.createdate = createdate;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public List<Avatar> getList() {
        return list;
    }

    public void setList(List<Avatar> list) {
        this.list = list;
    }

    public List<String> getUrlImages() {
        return urlImages;
    }

    public void setUrlImages(List<String> urlImages) {
        this.urlImages = urlImages;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) return false;

        if(!(obj instanceof Status)) return false;

        Status status = (Status) obj;

        if(this.getId() == status.getId()) return true;

        return false;
    }
}
