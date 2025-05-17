package tn.esprit.utils;

import tn.esprit.controllers.ResetPasswordController;

public class ProtocolHandler {
    public static void handleProtocol(String url) {
        if (url.startsWith("fnart://reset-password/")) {
            String token = url.substring("fnart://reset-password/".length());
            ResetPasswordController.showResetPasswordWindow(token);
        }
    }
}