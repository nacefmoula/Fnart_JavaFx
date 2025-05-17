package tn.esprit.models;

import java.math.BigDecimal;

public class Dons {
    private int id;
    private double valeur;
    private String type;
    private String description;
    private Beneficiaires beneficiaire;

    public Dons() {
    }

    public Dons(double valeur, String type, String description, Beneficiaires beneficiaire) {
        this.valeur = valeur;
        this.type = type;
        this.description = description;
        this.beneficiaire = beneficiaire;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getValeur() {
        return valeur;
    }

    public void setValeur(double valeur) {
        this.valeur = valeur;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Beneficiaires getBeneficiaire() {
        return beneficiaire;
    }

    public void setBeneficiaire(Beneficiaires beneficiaire) {
        this.beneficiaire = beneficiaire;
    }

    @Override
    public String toString() {
        return "dons{" +
                "id=" + id +
                ", valeur=" + valeur +
                ", type='" + type + '\'' +
                ", description='" + description + '\'' +
                ", beneficiaire=" + beneficiaire +
                '}';
    }
}
