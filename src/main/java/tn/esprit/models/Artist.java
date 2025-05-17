package tn.esprit.models;

import tn.esprit.enumerations.Role;

public class Artist extends User {
    private static Artist instance;
    private String artStyle;
    private String portfolio;


    public Artist() {
        super();
        this.setRole(Role.ARTIST);
    }

    public Artist(String nom, String email, String password, String phone, String gender, String artStyle, String portfolio) {
        super(nom, email, password, phone, gender);
        this.setRole(Role.ARTIST);
        this.artStyle = artStyle;
        this.portfolio = portfolio;
    }

    public static Artist getInstance() {
        if (instance == null) {
            instance = new Artist();
        }
        return instance;
    }

    public static Artist getInstance(String nom, String email, String password, String phone, String gender, String artStyle, String portfolio) {
        if (instance == null) {
            instance = new Artist(nom, email, password, phone, gender, artStyle, portfolio);
        }
        return instance;
    }

    public String getArtStyle() {
        return artStyle;
    }

    public void setArtStyle(String artStyle) {
        this.artStyle = artStyle;
    }

    public String getPortfolio() {
        return portfolio;
    }

    public void setPortfolio(String portfolio) {
        this.portfolio = portfolio;
    }


}