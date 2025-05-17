package tn.esprit.models;

public class Commande {
    private int id;
    private String nom;
    private String adress;
    private String telephone;
    private String email;
    private double totale;
    private String date;
    private int artwork_id;
    private String status;

    public Commande() {
        super();
    }

    public Commande(int id, String nom, String adress, String telephone, String email, double totale, String date, int artwork_id, String status) {
        this.id = id;
        this.nom = nom;
        this.adress = adress;
        this.telephone = telephone;
        this.email = email;
        this.totale = totale;
        this.date = date;
        this.artwork_id = artwork_id;
        this.status = status;
    }

    // Getters
    public int getId() { return id; }
    public String getNom() { return nom; }
    public String getAdress() { return adress; }
    public String getTelephone() { return telephone; }
    public String getEmail() { return email; }
    public double getTotale() { return totale; }
    public String getDate() { return date; }
    public int getArtwork_id() { return artwork_id; }
    public String getStatus() { return status; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setNom(String nom) { this.nom = nom; }
    public void setAdress(String adress) { this.adress = adress; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public void setEmail(String email) { this.email = email; }
    public void setTotale(double totale) { this.totale = totale; }
    public void setDate(String date) { this.date = date; }
    public void setArtwork_id(int artwork_id) { this.artwork_id = artwork_id; }
    public void setStatus(String status) { this.status = status; }
} 