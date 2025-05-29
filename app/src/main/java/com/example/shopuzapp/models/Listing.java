package com.example.shopuzapp.models;

public class Listing {
    public String title;
    public String description;
    public String imageBlob;


    public Listing(){}
    public Listing(String title, String description, String imageURI) {
        this.title = title;
        this.description = description;
        this.imageBlob = imageURI;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageBlob() {
        return imageBlob;
    }

    public void setImageBlob(String imageBlob) {
        this.imageBlob = imageBlob;
    }
}
