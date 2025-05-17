package tn.esprit.utils;

import tn.esprit.models.Artwork;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.IntegerProperty;
import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static final List<Artwork> cartItems = new ArrayList<>();
    private static final IntegerProperty cartCount = new SimpleIntegerProperty(0);

    public static void addToCart(Artwork artwork) {
        cartItems.add(artwork);
        cartCount.set(cartItems.size());
    }

    public static List<Artwork> getCartItems() {
        return cartItems;
    }

    public static void clearCart() {
        cartItems.clear();
        cartCount.set(0);
    }

    public static int getCartCount() {
        return cartCount.get();
    }

    public static IntegerProperty cartCountProperty() {
        return cartCount;
    }

    public static void removeFromCart(Artwork artwork) {
        cartItems.remove(artwork);
        cartCount.set(cartItems.size());
    }
} 