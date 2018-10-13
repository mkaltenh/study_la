package de.hawlandshut.studyla.events;

/**
* Objektklasse Event
* Wird zur Übergabe von einzelnen Events an den EventAdapter verwendet
* @Fragment: EventFragment
 */

import android.support.annotation.Nullable;

import java.util.Date;

public class Event {

    private String name;
    private Date date;
    private Date endDate;
    private String url;
    private String picUrl;

    public Event() {

    }

    public Event(String name, Date date, @Nullable Date endDate, String url, @Nullable String picUrl){
        this.name = name;
        this.date = date;
        this.endDate = endDate;
        this.url = url;
        this.picUrl = picUrl;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public Date getDate(){
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
