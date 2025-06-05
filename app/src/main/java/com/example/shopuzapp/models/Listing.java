package com.example.shopuzapp.models;

import com.google.firebase.firestore.Exclude;
import java.util.Map;

/**
 * Klasa reprezentująca pojedynczą ofertę w aplikacji ShopUzApp.
 */
public class Listing {
    /**
     * Unikalny identyfikator oferty. Oznaczony jako @Exclude, aby nie był zapisywany w Firestore.
     */
    @Exclude
    public String id;

    /**
     * Tytuł oferty.
     */
    public String title;

    /**
     * Opis oferty.
     */
    public String description;

    /**
     * Obraz oferty przechowywany jako blob.
     */
    public String imageBlob;

    /**
     * Cena oferty.
     */
    public double price;

    /**
     * Identyfikator właściciela oferty.
     */
    public String ownerId;

    /**
     * Lokalizacja oferty (szerokość i długość geograficzna).
     */
    public Map<String, String> location;

    /**
     * Konstruktor domyślny wymagany przez Firestore.
     */
    public Listing() {}

    /**
     * Konstruktor tworzący obiekt Listing na podstawie tytułu, opisu i URI obrazu.
     *
     * @param title Tytuł oferty.
     * @param description Opis oferty.
     * @param imageURI Adres URI obrazu oferty.
     */
    public Listing(String title, String description, String imageURI) {
        this.title = title;
        this.description = description;
        this.imageBlob = imageURI;
    }

    /**
     * Pobiera identyfikator oferty.
     *
     * @return Identyfikator oferty.
     */
    @Exclude
    public String getId() {
        return id;
    }

    /**
     * Ustawia identyfikator oferty.
     *
     * @param id Identyfikator oferty.
     */
    @Exclude
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Pobiera tytuł oferty.
     *
     * @return Tytuł oferty.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Ustawia tytuł oferty.
     *
     * @param title Tytuł oferty.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Pobiera opis oferty.
     *
     * @return Opis oferty.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Ustawia opis oferty.
     *
     * @param description Opis oferty.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Pobiera obraz oferty.
     *
     * @return Obraz oferty jako blob.
     */
    public String getImageBlob() {
        return imageBlob;
    }

    /**
     * Ustawia obraz oferty.
     *
     * @param imageBlob Obraz oferty jako blob.
     */
    public void setImageBlob(String imageBlob) {
        this.imageBlob = imageBlob;
    }

    /**
     * Pobiera cenę oferty.
     *
     * @return Cena oferty.
     */
    public double getPrice() {
        return price;
    }

    /**
     * Ustawia cenę oferty.
     *
     * @param price Cena oferty.
     */
    public void setPrice(double price) {
        this.price = price;
    }

    /**
     * Pobiera identyfikator właściciela oferty.
     *
     * @return Identyfikator właściciela.
     */
    public String getOwnerId() {
        return ownerId;
    }

    /**
     * Ustawia identyfikator właściciela oferty.
     *
     * @param ownerId Identyfikator właściciela.
     */
    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    /**
     * Pobiera lokalizację oferty.
     *
     * @return Mapa zawierająca współrzędne geograficzne.
     */
    public Map<String, String> getLocation() {
        return location;
    }

    /**
     * Ustawia lokalizację oferty.
     *
     * @param location Mapa zawierająca szerokość i długość geograficzną.
     */
    public void setLocation(Map<String, String> location) {
        this.location = location;
    }
}
