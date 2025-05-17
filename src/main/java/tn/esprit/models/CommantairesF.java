package tn.esprit.models;

import java.time.LocalDate;

public class CommantairesF {

    private int id;
    private User user; // Association with the User class
    private Forum forum; // Association with the Forum class
    private LocalDate date_c;
    private String texte_c;

    // Constructor with all fields
    public CommantairesF(int id, User user, Forum forum, LocalDate date_c, String texte_c) {
        this.id = id;
        this.user = user;
        this.forum = forum;
        this.date_c = date_c;
        this.texte_c = texte_c;
    }

    // Constructor with userId
    public CommantairesF(int userId, Forum forum, LocalDate date_c, String texte_c) {
        this.user = new User(userId);
        this.forum = forum;
        this.date_c = date_c;
        this.texte_c = texte_c;
    }

    // Default constructor
    public CommantairesF() {
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Forum getForum() {
        return forum;
    }

    public void setForum(Forum forum) {
        this.forum = forum;
    }

    public LocalDate getDate_c() {
        return date_c;
    }

    public void setDate_c(LocalDate date_c) {
        this.date_c = date_c;
    }

    public String getTexte_c() {
        return texte_c;
    }

    public void setTexte_c(String texte_c) {
        this.texte_c = texte_c;
    }

    @Override
    public String toString() {
        return "Commentaire_f{" +
                "id=" + id +
                ", user=" + (user != null ? user.getId() : "null") +
                ", forum=" + (forum != null ? forum.getTitre_f() : "null") +
                ", date_c=" + date_c +
                ", texte_c='" + texte_c + '\'' +
                '}';
    }
}