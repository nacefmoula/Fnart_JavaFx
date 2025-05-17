package tn.esprit.services;

import javafx.collections.ObservableList;
import tn.esprit.models.Beneficiaires;
import tn.esprit.models.Dons;
import tn.esprit.utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServicesDons {
    private Connection connection;

    public ServicesDons() {
        connection = DataSource.getInstance().getConnection();
    }

    public void add(Dons don) {
        String query = "INSERT INTO dons (valeur, type, description, beneficiaire_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setDouble(1, don.getValeur());
            statement.setString(2, don.getType());
            statement.setString(3, don.getDescription());
            statement.setInt(4, don.getBeneficiaire().getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void update(Dons don) {
        String query = "UPDATE dons SET valeur = ?, type = ?, description = ?, beneficiaire_id = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setDouble(1, don.getValeur());
            statement.setString(2, don.getType());
            statement.setString(3, don.getDescription());
            statement.setInt(4, don.getBeneficiaire().getId());
            statement.setInt(5, don.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(int id) {
        String query = "DELETE FROM dons WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ObservableList<Dons> getAll() {
        List<Dons> donsList = new ArrayList<>();
        String query = "SELECT d.*, b.nom as beneficiaire_nom, b.email as beneficiaire_email, b.telephone as beneficiaire_telephone " +
                      "FROM dons d " +
                      "JOIN beneficiaires b ON d.beneficiaire_id = b.id";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                Dons don = new Dons();
                don.setId((int) resultSet.getInt("id"));
                don.setValeur(resultSet.getDouble("valeur"));
                don.setType(resultSet.getString("type"));
                don.setDescription(resultSet.getString("description"));

                // Create and set beneficiaire
                Beneficiaires beneficiaire = new Beneficiaires();
                beneficiaire.setId((long) resultSet.getInt("beneficiaire_id"));
                beneficiaire.setNom(resultSet.getString("beneficiaire_nom"));
                beneficiaire.setEmail(resultSet.getString("beneficiaire_email"));
                beneficiaire.setTelephone(resultSet.getString("beneficiaire_telephone"));
                don.setBeneficiaire(beneficiaire);

                donsList.add(don);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return javafx.collections.FXCollections.observableArrayList(donsList);
    }
}
