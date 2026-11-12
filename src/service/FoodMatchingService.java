package service;

import database.DBConnection;
import linkedlist.FoodDonation;
import queue.FoodRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FoodMatchingService {

    private FoodMatchingService() {}

    public static List<Map<String, Object>> findRecipients(
            FoodDonation donation) throws SQLException {

        List<Map<String, Object>> matches = new ArrayList<>();

        String sql =
            "SELECT c.full_name, c.address, c.phone, c.email, " +
            "n.quantity_required, n.need_id " +
            "FROM FoodShareNeeds n " +
            "JOIN FoodShareContacts c ON c.contact_id = n.recipient_id " +
            "WHERE c.contact_type = 'RECIPIENT' " +
            "AND LOWER(TRIM(n.food_name)) = LOWER(TRIM(?)) " +
            "AND n.quantity_required <= ? " +
            "AND n.status = 'PENDING' " +
            "ORDER BY n.need_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, donation.getFoodName());
            ps.setInt(2, donation.getQuantity());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> match = new LinkedHashMap<>();
                    match.put("name", rs.getString("full_name"));
                    match.put("address", rs.getString("address"));
                    match.put("phone", rs.getString("phone"));
                    match.put("email", rs.getString("email"));
                    match.put("quantity", rs.getInt("quantity_required"));
                    match.put("needId", rs.getInt("need_id"));
                    matches.add(match);
                }
            }
        }

        return matches;
    }

    public static List<Map<String, Object>> findDonors(
            FoodRequest request) throws SQLException {

        List<Map<String, Object>> matches = new ArrayList<>();

        String sql =
            "SELECT c.full_name, c.address, c.phone, c.email, " +
            "i.food_name, i.quantity_available, i.inventory_id " +
            "FROM FoodShareInventory i " +
            "JOIN FoodShareContacts c ON c.contact_id = i.donor_id " +
            "WHERE c.contact_type = 'DONOR' " +
            "AND LOWER(TRIM(i.food_name)) = LOWER(TRIM(?)) " +
            "AND i.quantity_available >= ? " +
            "ORDER BY i.inventory_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, request.getFoodRequired());
            ps.setInt(2, request.getQuantityRequired());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> match = new LinkedHashMap<>();
                    match.put("name", rs.getString("full_name"));
                    match.put("address", rs.getString("address"));
                    match.put("phone", rs.getString("phone"));
                    match.put("email", rs.getString("email"));
                    match.put("foodName", rs.getString("food_name"));
                    match.put("quantityAvailable",
                              rs.getInt("quantity_available"));
                    match.put("inventoryId", rs.getInt("inventory_id"));
                    matches.add(match);
                }
            }
        }

        return matches;
    }

    public static void recordHistory(
            String itemType,
            String itemId,
            String foodName,
            int quantity,
            String matchedName,
            String status) throws SQLException {

        String sql =
            "INSERT INTO FoodShareProcessedHistory " +
            "(item_type, item_id, food_name, quantity, matched_name, status) " +
            "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, itemType);
            ps.setString(2, itemId);
            ps.setString(3, foodName);
            ps.setInt(4, quantity);
            ps.setString(5, matchedName);
            ps.setString(6, status);
            ps.executeUpdate();
        }
    }

    public static List<Map<String, Object>> getProcessedHistory()
            throws SQLException {

        List<Map<String, Object>> history = new ArrayList<>();

        String sql =
            "SELECT history_id, item_type, item_id, food_name, quantity, " +
            "matched_name, processed_at, status " +
            "FROM FoodShareProcessedHistory ORDER BY history_id DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("historyId", rs.getInt("history_id"));
                item.put("itemType", rs.getString("item_type"));
                item.put("itemId", rs.getString("item_id"));
                item.put("foodName", rs.getString("food_name"));
                item.put("quantity", rs.getInt("quantity"));
                item.put("matchedName", rs.getString("matched_name"));
                item.put("processedAt",
                         rs.getTimestamp("processed_at").toString());
                item.put("status", rs.getString("status"));
                history.add(item);
            }
        }

        return history;
    }
}
