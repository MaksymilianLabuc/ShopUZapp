package com.example.shopuzapp.ui.home;

import com.example.shopuzapp.models.Listing;

/**
 * Interfejs {@code OnListingItemClickListener} służy do obsługi zdarzenia kliknięcia
 * na element listy ogłoszeń.
 *
 * Klasy implementujące ten interfejs muszą zdefiniować metodę {@link #onItemClick(Listing, int)},
 * która umożliwia wykonanie odpowiedniej akcji po kliknięciu na dany element.
 *
 * @see com.example.shopuzapp.models.Listing
 */
public interface OnListingItemClickListener {
    /**
     * Metoda wywoływana w momencie kliknięcia na element listy.
     *
     * @param item     Obiekt {@link Listing}, który reprezentuje ogłoszenie, na którym nastąpiło kliknięcie.
     * @param position Pozycja elementu w liście.
     */
    void onItemClick(Listing item, int position);
}
