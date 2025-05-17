package tn.esprit.models;

import tn.esprit.enumerations.Role;

public class RegularUser extends User {
    private static RegularUser instance;

    public RegularUser() {
        super();
        this.setRole(Role.REGULARUSER);
    }

    public RegularUser(String nom, String email, String password, String phone, String gender) {
        super(nom, email, password, phone, gender);
        this.setRole(Role.REGULARUSER);
    }

    public static RegularUser getInstance() {
        if (instance == null) {
            instance = new RegularUser();
        }
        return instance;
    }

    public static RegularUser getInstance(String nom, String email, String password, String phone, String gender) {
        if (instance == null) {
            instance = new RegularUser(nom, email, password, phone, gender);
        }
        return instance;
    }
}