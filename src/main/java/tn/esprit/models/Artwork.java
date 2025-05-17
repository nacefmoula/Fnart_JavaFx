package tn.esprit.models;

public class Artwork {
    private int id;
    private String titre;
    private String description;
    private int prix;
    private String image;
    private String artistenom;
    private String status;
    private boolean approved;
    // Nouveaux champs pour les détails
    private String technique;
    private String dimensions;
    private String condition;
    private String location;
    private String inventoryNumber;
    private String year;

    public Artwork() {
        super();
    }

    public Artwork(int id, String titre, String description, int prix, String image, String artistenom, boolean approved) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.prix = prix;
        this.image = image;
        this.artistenom = artistenom;
        this.status = "disponible";
        this.approved = approved;
    }

    public Artwork(String titre, String description, int prix, String image, String artistenom) {
        this.titre = titre;
        this.description = description;
        this.prix = prix;
        this.image = image;
        this.artistenom = artistenom;
        this.status = "disponible";
        this.approved = false;
    }

    public Artwork(int id, String titre, String description, int prix, String image, String artistenom, String status, boolean approved) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.prix = prix;
        this.image = image;
        this.artistenom = artistenom;
        this.status = status;
        this.approved = approved;
    }

    // Getters existants
    public int getId() { return id; }
    public String getTitre() { return titre; }
    public String getDescription() { return description; }
    public int getPrix() { return prix; }
    public String getImage() { return image; }
    public String getArtistenom() { return artistenom; }
    public String getStatus() { return status; }
    public boolean isApproved() { return approved; }

    // Setters existants
    public void setId(int id) { this.id = id; }
    public void setTitre(String titre) { this.titre = titre; }
    public void setDescription(String description) { this.description = description; }
    public void setPrix(int prix) { this.prix = prix; }
    public void setImage(String image) { this.image = image; }
    public void setArtistenom(String artistenom) { this.artistenom = artistenom; }
    public void setStatus(String status) { this.status = status; }
    public void setApproved(boolean approved) { this.approved = approved; }

    // Nouveaux getters pour les détails
    public String getTitle() { return titre; }
    public String getArtist() { return artistenom; }
    public String getYear() { return year; }
    public String getTechnique() { return technique; }
    public String getDimensions() { return dimensions; }
    public String getCondition() { return condition; }
    public String getLocation() { return location; }
    public String getInventoryNumber() { return inventoryNumber; }
    public String getImageUrl() { return image; }

    // Nouveaux setters pour les détails
    public void setYear(String year) { this.year = year; }
    public void setTechnique(String technique) { this.technique = technique; }
    public void setDimensions(String dimensions) { this.dimensions = dimensions; }
    public void setCondition(String condition) { this.condition = condition; }
    public void setLocation(String location) { this.location = location; }
    public void setInventoryNumber(String inventoryNumber) { this.inventoryNumber = inventoryNumber; }

    @Override
    public String toString() {
        return "Artwork{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", description='" + description + '\'' +
                ", prix=" + prix +
                ", image='" + image + '\'' +
                ", artistenom='" + artistenom + '\'' +
                ", status='" + status + '\'' +
                ", approved=" + approved +
                ", technique='" + technique + '\'' +
                ", dimensions='" + dimensions + '\'' +
                ", condition='" + condition + '\'' +
                ", location='" + location + '\'' +
                ", inventoryNumber='" + inventoryNumber + '\'' +
                ", year='" + year + '\'' +
                '}';
    }
} 