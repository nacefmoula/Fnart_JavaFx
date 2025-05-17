package tn.esprit.models;

import java.util.Date;
import java.util.Objects;
import java.time.LocalDate;

public class Atelier {

    private int id; // Auto-increment
    private String titre;
    private String lieu;
    private Date date;
    private int participantMax;
    private String description;
    private String image;
    private int nbPlaces; // Field for the number of places
    private double longitude; // Coordonnée longitude pour Mapbox
    private double latitude; // Coordonnée latitude pour Mapbox

    private int inscriptionCount; // New field for number of inscriptions

    public Atelier() {}

    public Atelier(String titre, String lieu, Date date, int participantMax, String description, String image) {
        this.titre = titre;
        this.lieu = lieu;
        this.date = date;
        this.participantMax = participantMax;
        this.description = description;
        this.image = image;
    }

    public Atelier(int id, String titre, String lieu, Date date, int participantMax, String description, String image) {
        this.id = id;
        this.titre = titre;
        this.lieu = lieu;
        this.date = date;
        this.participantMax = participantMax;
        this.description = description;
        this.image = image;
    }

    public Atelier(int id, String titre, String lieu, Date date, int participantMax, String description, String image, double longitude, double latitude) {
        this.id = id;
        this.titre = titre;
        this.lieu = lieu;
        this.date = date;
        this.participantMax = participantMax;
        this.description = description;
        this.image = image;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getLieu() {
        return lieu;
    }

    public void setLieu(String lieu) {
        this.lieu = lieu;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getParticipantMax() {
        return participantMax;
    }

    public void setParticipantMax(int participantMax) {
        this.participantMax = participantMax;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getNbPlaces() {
        return nbPlaces;
    }

    public void setNbPlaces(int nbPlaces) {
        this.nbPlaces = nbPlaces;
    }

    public int getInscriptionCount() {
        return inscriptionCount;
    }

    public void setInscriptionCount(int inscriptionCount) {
        this.inscriptionCount = inscriptionCount;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Atelier atelier)) return false;
        return id == atelier.id &&
                participantMax == atelier.participantMax &&
                Objects.equals(titre, atelier.titre) &&
                Objects.equals(lieu, atelier.lieu) &&
                Objects.equals(date, atelier.date) &&
                Objects.equals(description, atelier.description) &&
                Objects.equals(image, atelier.image) &&
                Double.compare(atelier.longitude, longitude) == 0 &&
                Double.compare(atelier.latitude, latitude) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, titre, lieu, date, participantMax, description, image, longitude, latitude);
    }

    @Override
    public String toString() {
        return "Atelier{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", lieu='" + lieu + '\'' +
                ", date=" + date +
                ", participantMax=" + participantMax +
                ", description='" + description + '\'' +
                ", image='" + image + '\'' +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                '}';
    }

    public int getMaxParticipants() {
        return 0;
    }
}
