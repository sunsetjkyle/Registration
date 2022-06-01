package com.ubunifu.appclone.models;

public class Posts {
    String caption, post_id, post_img_url, current_date, current_time, publisher, full_names, profile_img_url;

    public Posts() {
    }

    public Posts(String caption, String post_id, String post_img_url, String current_date, String current_time, String publisher, String full_names, String profile_img_url) {
        this.caption = caption;
        this.post_id = post_id;
        this.post_img_url = post_img_url;
        this.current_date = current_date;
        this.current_time = current_time;
        this.publisher = publisher;
        this.full_names = full_names;
        this.profile_img_url = profile_img_url;
    }

    public String getFull_names() {
        return full_names;
    }

    public void setFull_names(String full_names) {
        this.full_names = full_names;
    }

    public String getProfile_img_url() {
        return profile_img_url;
    }

    public void setProfile_img_url(String profile_img_url) {
        this.profile_img_url = profile_img_url;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getPost_id() {
        return post_id;
    }

    public void setPost_id(String post_id) {
        this.post_id = post_id;
    }

    public String getPost_img_url() {
        return post_img_url;
    }

    public void setPost_img_url(String post_img_url) {
        this.post_img_url = post_img_url;
    }

    public String getCurrent_date() {
        return current_date;
    }

    public void setCurrent_date(String current_date) {
        this.current_date = current_date;
    }

    public String getCurrent_time() {
        return current_time;
    }

    public void setCurrent_time(String current_time) {
        this.current_time = current_time;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }
}
