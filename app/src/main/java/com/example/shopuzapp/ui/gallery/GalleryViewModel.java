package com.example.shopuzapp.ui.gallery;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * Klasa {@code GalleryViewModel} rozszerza {@link ViewModel} i służy do przechowywania oraz zarządzania danymi
 * dla fragmentu galerii. Dzięki temu dane przetrwają zmiany konfiguracji, takie jak obrót ekranu.
 */
public class GalleryViewModel extends ViewModel {

    /**
     * MutableLiveData przechowująca tekst wyświetlany w galerii.
     */
    private final MutableLiveData<String> mText;

    /**
     * Konstruktor klasy {@code GalleryViewModel}.
     * Inicjalizuje {@code mText} oraz ustawia domyślną wartość "This is gallery fragment".
     */
    public GalleryViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is gallery fragment");
    }

    /**
     * Zwraca dane tekstowe jako obiekt {@link LiveData}.
     * Pozwala to na obserwowanie zmian oraz automatyczną aktualizację interfejsu użytkownika,
     * gdy wartość tekstu ulegnie zmianie.
     *
     * @return {@link LiveData<String>} zawierające tekst dla fragmentu galerii.
     */
    public LiveData<String> getText() {
        return mText;
    }
}
