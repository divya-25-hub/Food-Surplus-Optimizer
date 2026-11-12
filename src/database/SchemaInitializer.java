package database;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import linkedlist.FoodDonation;
import queue.FoodRequest;
import service.FoodShareService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Runs once when the web application starts.
 *
 * 1. Makes sure the tables used by FoodShare exist (existing tables and
 *    their data are never changed or dropped).
 * 2. Adds the donation "status" column if an older database lacks it.
 * 3. Reloads donations and requests that are still pending in MySQL into the
 *    linked list / stack / queue so nothing is lost on a Tomcat restart.
 */
@WebListener
public class SchemaInitializer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent event) {
        try (Connection con = DBConnection.getConnection()) {
            createTables(con);
            addDonationStatusColumn(con);
            restorePendingData(con);
            System.out.println("FOODSHARE: database ready, pending data restored.");
        } catch (SQLException e) {
            System.out.println("FOODSHARE: database initialisation failed: "
                    + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createTables(Connection con) throws SQLException {
        try (Statement st = con.createStatement()) {

            st.execute("CREATE TABLE IF NOT EXISTS Donations ("
                + "donation_id INT PRIMARY KEY AUTO_INCREMENT,"
                + "food_name VARCHAR(100) NOT NULL,"
                + "quantity DECIMAL(10,2) NOT NULL,"
                + "donor_username VARCHAR(50) NOT NULL,"
                + "status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',"
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            st.execute("CREATE TABLE IF NOT EXISTS FoodShareAppRequests ("
                + "request_id VARCHAR(20) PRIMARY KEY,"
                + "recipient_name VARCHAR(100) NOT NULL,"
                + "food_required VARCHAR(100) NOT NULL,"
                + "quantity_required INT NOT NULL,"
                + "status VARCHAR(20) NOT NULL DEFAULT 'PENDING',"
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            st.execute("CREATE TABLE IF NOT EXISTS FoodShareContacts ("
                + "contact_id INT PRIMARY KEY AUTO_INCREMENT,"
                + "full_name VARCHAR(100) NOT NULL,"
                + "contact_type VARCHAR(20) NOT NULL,"
                + "address VARCHAR(255),"
                + "phone VARCHAR(30),"
                + "email VARCHAR(100))");

            st.execute("CREATE TABLE IF NOT EXISTS FoodShareInventory ("
                + "inventory_id INT PRIMARY KEY AUTO_INCREMENT,"
                + "donor_id INT NOT NULL,"
                + "food_name VARCHAR(100) NOT NULL,"
                + "quantity_available INT NOT NULL)");

            st.execute("CREATE TABLE IF NOT EXISTS FoodShareNeeds ("
                + "need_id INT PRIMARY KEY AUTO_INCREMENT,"
                + "recipient_id INT NOT NULL,"
                + "food_name VARCHAR(100) NOT NULL,"
                + "quantity_required INT NOT NULL,"
                + "status VARCHAR(20) NOT NULL DEFAULT 'PENDING')");

            st.execute("CREATE TABLE IF NOT EXISTS FoodShareProcessedHistory ("
                + "history_id INT PRIMARY KEY AUTO_INCREMENT,"
                + "item_type VARCHAR(20),"
                + "item_id VARCHAR(30),"
                + "food_name VARCHAR(100),"
                + "quantity INT,"
                + "matched_name VARCHAR(100),"
                + "processed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "status VARCHAR(20))");
        }
    }

    private void addDonationStatusColumn(Connection con) throws SQLException {
        try (Statement st = con.createStatement()) {
            st.execute("ALTER TABLE Donations ADD COLUMN status "
                    + "VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE'");
        } catch (SQLException e) {
            // 1060 = duplicate column name: the column already exists
            if (e.getErrorCode() != 1060) {
                throw e;
            }
        }
    }

    private void restorePendingData(Connection con) throws SQLException {

        // Newest 5 available donations (stack capacity is 5), oldest first
        List<FoodDonation> donations = new ArrayList<>();
        String donationSql = "SELECT donation_id, food_name, quantity, donor_username "
                + "FROM Donations WHERE status = 'AVAILABLE' "
                + "ORDER BY donation_id DESC LIMIT 5";
        try (PreparedStatement ps = con.prepareStatement(donationSql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                donations.add(new FoodDonation(
                        "D" + rs.getInt("donation_id"),
                        rs.getString("food_name"),
                        (int) Math.round(rs.getDouble("quantity")),
                        rs.getString("donor_username")));
            }
        }
        Collections.reverse(donations);
        for (FoodDonation d : donations) {
            FoodShareService.addSavedDonation(
                    Integer.parseInt(d.getDonationId().substring(1)),
                    d.getFoodName(), d.getQuantity(), d.getDonorName());
        }

        // Oldest 5 pending requests (queue capacity is 5)
        String requestSql = "SELECT request_id, recipient_name, food_required, "
                + "quantity_required FROM FoodShareAppRequests "
                + "WHERE status = 'PENDING' ORDER BY created_at, request_id LIMIT 5";
        try (PreparedStatement ps = con.prepareStatement(requestSql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                FoodShareService.enqueueSavedRequest(new FoodRequest(
                        rs.getString("request_id"),
                        rs.getString("recipient_name"),
                        rs.getString("food_required"),
                        rs.getInt("quantity_required")));
            }
        }

        // Continue request numbering after the highest saved ID
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT request_id FROM FoodShareAppRequests");
             ResultSet rs = ps.executeQuery()) {
            int max = 1000;
            while (rs.next()) {
                String id = rs.getString(1);
                if (id != null && id.matches("R\\d+")) {
                    max = Math.max(max, Integer.parseInt(id.substring(1)));
                }
            }
            FoodShareService.setRequestCounter(max);
        }
    }
}
