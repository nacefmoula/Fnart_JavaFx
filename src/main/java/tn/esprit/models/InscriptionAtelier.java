package tn.esprit.models;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Date;

public class InscriptionAtelier {
    private IntegerProperty id;
    private IntegerProperty idAtelier;
    private StringProperty nomTemporaire;
    private StringProperty emailTemporaire;
    private StringProperty statut; // "en attente", "confirmée", "annulée"

    public InscriptionAtelier() {
        this.id = new SimpleIntegerProperty();
        this.idAtelier = new SimpleIntegerProperty();
        this.nomTemporaire = new SimpleStringProperty();
        this.emailTemporaire = new SimpleStringProperty();
        this.statut = new SimpleStringProperty();
        this.statut.set("en attente");
    }

    public InscriptionAtelier(int idAtelier, String nomTemporaire, String emailTemporaire) {
        this();
        this.idAtelier.set(idAtelier);
        this.nomTemporaire.set(nomTemporaire);
        this.emailTemporaire.set(emailTemporaire);
    }

    // Getters et Setters
    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public int getIdAtelier() {
        return idAtelier.get();
    }

    public void setIdAtelier(int idAtelier) {
        this.idAtelier.set(idAtelier);
    }

    public IntegerProperty idAtelierProperty() {
        return idAtelier;
    }

    public String getNomTemporaire() {
        return nomTemporaire.get();
    }

    public void setNomTemporaire(String nomTemporaire) {
        this.nomTemporaire.set(nomTemporaire);
    }

    public StringProperty nomTemporaireProperty() {
        return nomTemporaire;
    }

    public String getEmailTemporaire() {
        return emailTemporaire.get();
    }

    public void setEmailTemporaire(String emailTemporaire) {
        this.emailTemporaire.set(emailTemporaire);
    }

    public StringProperty emailTemporaireProperty() {
        return emailTemporaire;
    }

    public String getStatut() {
        return statut.get();
    }

    public void setStatut(String statut) {
        this.statut.set(statut);
    }

    public StringProperty statutProperty() {
        return statut;
    }

    public InscriptionAtelier getAtelier() {
        return null;
    }
}