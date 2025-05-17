package tn.esprit.models;

import tn.esprit.enumerations.Role;

import java.util.Date;
import java.sql.Timestamp;

public class User {

    private int id;
    private String nom, email, password, phone, gender, status;
    private String imagePath; // New field for profile image
    private Role role;
    private Date DateOfBirth;
    private String profilePicture;
    private String resetToken;
    private Timestamp resetTokenExpiry;

    public User() {
        // Default constructor
    }

    public User(int id) {
        this.id = id;
        this.status = "PENDING";
    }

    public User(String nom, String email, String password, String phone, String gender) {
        this.nom = nom;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.gender = gender;
        this.status = "PENDING";
    }

    public User(String nom, String email, String password, String phone, String gender, Date DateOfBirth) {
        this.nom = nom;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.gender = gender;
        this.status = "PENDING";
        this.DateOfBirth = DateOfBirth;
    }

    public User(String nom, String email, String password, String phone, String gender, Role role) {
        this.nom = nom;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.gender = gender;
        this.role = role;
        this.status = "PENDING";
    }

    public User(String nom, String email, String password, String phone, String gender, Role role, Date DateOfBirth) {
        this.nom = nom;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.gender = gender;
        this.role = role;
        this.status = "PENDING";
        this.DateOfBirth = DateOfBirth;
    }

    public User(String nom, String email, String password, String phone, String gender, Role role, String imagePath) {
        this.nom = nom;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.gender = gender;
        this.role = role;
        this.imagePath = imagePath;
        this.status = "PENDING";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Date getDateOfBirth() {
        return DateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        DateOfBirth = dateOfBirth;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }

    public Timestamp getResetTokenExpiry() {
        return resetTokenExpiry;
    }

    public void setResetTokenExpiry(Timestamp resetTokenExpiry) {
        this.resetTokenExpiry = resetTokenExpiry;
    }

    public void setImage(String image) {
        this.imagePath = image;
    }
    public String getImage(){
        return imagePath;
    }
    public void setIsActive(boolean isActive) {
        this.status = isActive ? "ACTIVE" : "INACTIVE";
    }

    public boolean isActive() {
        return "ACTIVE".equals(this.status);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", phone='" + phone + '\'' +
                ", gender='" + gender + '\'' +
                ", role=" + role +
                ", status='" + status + '\'' +
                ", imagePath='" + imagePath + '\'' +
                ", profilePicture='" + profilePicture + '\'' +
                ", resetToken='" + resetToken + '\'' +
                ", resetTokenExpiry=" + resetTokenExpiry +
                '}';
    }
}