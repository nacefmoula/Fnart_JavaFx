package tn.esprit.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class Forum {
    private int id;
    private Date date_f;
    private String titre_f;
    private User user;
    private String categorie_f;
    private String description_f;
    private String image_f;
    private float rating; // New field for rating

    // Default constructor
    public Forum() {
        this.user = new User();
        this.rating = 0.0f;
    }

    // Constructor with ID
    public Forum(int id) {
        this.id = id;
        this.user = new User();
        this.rating = 0.0f;
    }

    // Constructor for creation without ID
    public Forum(Date date_f, String titre_f, User user, String categorie_f,
                 String description_f, String image_f, float rating) {
        this.date_f = date_f;
        this.titre_f = titre_f;
        this.user = user;
        this.categorie_f = categorie_f;
        this.description_f = description_f;
        this.image_f = image_f;
        this.rating = rating;
    }

    // Complete constructor with ID
    public Forum(int id, Date date_f, String titre_f, User user,
                 String categorie_f, String description_f, String image_f, float rating) {
        this.id = id;
        this.date_f = date_f;
        this.titre_f = titre_f;
        this.user = user;
        this.categorie_f = categorie_f;
        this.description_f = description_f;
        this.image_f = image_f;
        this.rating = rating;
    }

    // Constructor for adding forum
    public Forum(Date date_f, String titre_f, String categorie_f, String description_f, String image_f) {
        this.date_f = date_f;
        this.titre_f = titre_f;
        this.categorie_f = categorie_f;
        this.description_f = description_f;
        this.image_f = image_f;
        this.rating = 0.0f;
    }

    // Constructor for tests
    public Forum(String titre_f, String categorie_f, String description_f) {
        this.titre_f = titre_f;
        this.categorie_f = categorie_f;
        this.description_f = description_f;
        this.user = new User();
        this.date_f = new Date();
        this.rating = 0.0f;
    }

    public Forum(int id, String titreF, String descriptionF) {
        this.id = id;
        this.titre_f = titreF;
        this.description_f = descriptionF;
        this.rating = 0.0f;
    }

    // Getters & Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getDate_f() {
        return date_f;
    }

    public void setDate_f(Date date_f) {
        this.date_f = date_f;
    }

    public String getTitre_f() {
        return titre_f;
    }

    public void setTitre_f(String titre_f) {
        this.titre_f = titre_f;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        this.user = user;
    }

    public String getCategorie_f() {
        return categorie_f;
    }

    public void setCategorie_f(String categorie_f) {
        this.categorie_f = categorie_f;
    }

    public String getDescription_f() {
        return description_f;
    }

    public void setDescription_f(String description_f) {
        this.description_f = description_f;
    }

    public String getImage_f() {
        return image_f;
    }

    public void setImage_f(String image_f) {
        this.image_f = image_f;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    // Utility method for formatted date
    public String getFormattedDate() {
        if (date_f != null) {
            return new SimpleDateFormat("dd/MM/yyyy").format(date_f);
        }
        return "";
    }

    // Methods for TableView display
    public String getTitle() {
        return titre_f;
    }

    public String getDescription() {
        return description_f;
    }

    public String getCategory() {
        return categorie_f;
    }

    public String getAuthorName() {
        return user != null ? user.getNom() : "Unknown";
    }

    // Equals & HashCode based on ID
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Forum forum = (Forum) o;
        return id == forum.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Forum{" +
                "id=" + id +
                ", date_f=" + getFormattedDate() +
                ", titre_f='" + titre_f + '\'' +
                ", user=" + (user != null ? user.getNom() + " (ID:" + user.getId() + ")" : "null") +
                ", categorie_f='" + categorie_f + '\'' +
                ", description_f='" + (description_f != null ? description_f.substring(0, Math.min(description_f.length(), 30)) + "..." : "null") +
                ", image_f='" + (image_f != null ? "[" + image_f.length() + " chars]" : "null") +
                ", rating=" + rating +
                '}';
    }
}