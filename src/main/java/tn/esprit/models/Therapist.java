package tn.esprit.models;

import tn.esprit.enumerations.Role;

import java.util.Date;

public class Therapist extends User {
    private static Therapist instance;
    private String specialization;
    private int yearsOfExperience;

    public Therapist() {
        super();
        this.setRole(Role.THERAPIST);
    }

    public Therapist(String nom, String email, String password, String phone, String gender,
                     String specialization, int yearsOfExperience) {
        super(nom, email, password, phone, gender, Role.THERAPIST);
        this.specialization = specialization;
        this.yearsOfExperience = yearsOfExperience;
    }

    public static Therapist getInstance() {
        if (instance == null) {
            instance = new Therapist();
        }
        return instance;
    }

    public static Therapist getInstance(String nom, String email, String password, String phone, String gender,
                                        String specialization, int yearsOfExperience) {
        if (instance == null) {
            instance = new Therapist(nom, email, password, phone, gender, specialization, yearsOfExperience);
        }
        return instance;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public int getYearsOfExperience() {
        return yearsOfExperience;
    }

    public void setYearsOfExperience(int yearsOfExperience) {
        this.yearsOfExperience = yearsOfExperience;
    }
}
