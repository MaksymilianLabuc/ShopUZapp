package com.example.shopuzapp.ui.slideshow;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * Klasa {@code SlideshowViewModel} rozszerza {@link ViewModel} i służy do przechowywania oraz zarządzania danymi
 * dla fragmentu Slideshow. Dzięki zastosowaniu obiektu {@link MutableLiveData} możemy dynamicznie reagować
 * na zmiany danych, co umożliwia automatyczną aktualizację interfejsu użytkownika.
 */
public class SlideshowViewModel extends ViewModel {

    /**
     * Prywatne pole {@code mText} przechowujące dane typu {@link String} w obiekcie {@link MutableLiveData}.
     */
    private final MutableLiveData<String> mText;

    /**
     * Konstruktor klasy {@code SlideshowViewModel}.
     * Inicjalizuje pole {@code mText} i ustawia domyślną wartość na "This is slideshow fragment".
     */
    public SlideshowViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is slideshow fragment");
    }

    /**
     * Zwraca obiekt {@link LiveData}, który umożliwia obserwację zmian wartości przechowywanej w {@code mText}.
     *
     * @return {@link LiveData<String>} zawierające tekst do wyświetlenia.
     */
    public LiveData<String> getText() {
        return mText;
    }
}
