package com.example.shopuzapp.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * Klasa {@code HomeViewModel} rozszerza {@link ViewModel} i służy do przechowywania
 * oraz zarządzania danymi dla fragmentu głównego. Dzięki mechanizmowi LiveData fragment
 * może obserwować zmiany danych i automatycznie aktualizować interfejs użytkownika.
 */
public class HomeViewModel extends ViewModel {

    /**
     * Prywatny obiekt klasy {@link MutableLiveData} przechowujący tekst, który ma być wyświetlony.
     */
    private final MutableLiveData<String> mText;

    /**
     * Konstruktor klasy {@code HomeViewModel}.
     * Inicjalizuje {@code mText} i ustawia jego domyślną wartość na "This is home fragment".
     */
    public HomeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");
    }

    /**
     * Metoda zwraca obiekt {@link LiveData}, zawierający tekst do wyświetlenia w interfejsie użytkownika.
     * Pozwala to na obserwowanie zmian wartości tekstu przez inne komponenty, takie jak fragmenty.
     *
     * @return {@link LiveData} przechowująca tekst.
     */
    public LiveData<String> getText() {
        return mText;
    }
}
