package tn.esprit.models;

import tn.esprit.enumerations.Role;

import java.util.Date;

public class Admin extends User {
    private static Admin instance;

    public Admin() {
        super();
        this.setRole(Role.ADMIN);
    }

    public Admin(String nom, String email, String password, String phone, String gender) {
        super(nom, email, password, phone, gender,Role.ADMIN);
    }

    public static Admin getInstance() {
        if (instance == null) {
            instance = new Admin();
        }
        return instance;
    }

    public static Admin getInstance(String nom, String email, String password, String phone, String gender) {
        if (instance == null) {
            instance = new Admin(nom, email, password, phone, gender);
        }
        return instance;
    }
}
