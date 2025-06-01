package com.example.shopuzapp.models;

import com.google.firebase.firestore.Exclude;

public class Listing {
    @Exclude
    public String id;
    public String title;
    public String description;
    public String imageBlob;
    public double price;
    public String ownerId;


    public Listing(){}
    public Listing(String title, String description, String imageURI) {
        this.title = title;
        this.description = description;
        this.imageBlob = imageURI;
    }

    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
    public void setId(String id) {
        this.id = id;
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

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }
}
