package com.example.lab4;

import java.util.UUID;

public class RssFeedModel {

    public String title;
    public String link;
    public String description;
    public String image;
    public long id;

    public RssFeedModel(String title, String link, String description, String image) {
        this.title = title;
        this.link = link;
        this.description = description;
        this.image = image;
        id = -1;
    }

    public RssFeedModel(long id, String title, String link, String description, String image) {
        this.title = title;
        this.link = link;
        this.description = description;
        this.id = id;
        this.image = image;
    }

    public RssFeedModel(long id, String title, String link, String description) {
        this.title = title;
        this.link = link;
        this.description = description;
        this.id = id;
    }
}

