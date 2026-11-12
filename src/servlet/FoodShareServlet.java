package servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import database.DBConnection;
import linkedlist.FoodDonation;
import queue.FoodRequest;
import service.FoodMatchingService;
import service.FoodShareService;

@WebServlet("/foodshare")
public class FoodShareServlet extends HttpServlet {

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        PrintWriter out = response.getWriter();

        if ("addDonation".equals(action)) {
            addDonation(request, out);
        } else if ("addRequest".equals(action)) {
            addRequest(request, out);
        } else if ("viewDonations".equals(action)) {
            printDonations(out, FoodShareService.getDonations());
        } else if ("viewStack".equals(action)) {
            printDonations(out, FoodShareService.getStack());
        } else if ("viewRequests".equals(action)) {
            viewRequests(response, out);
        } else if ("processDonation".equals(action)) {
            processDonation(response, out);
        } else if ("confirmDonation".equals(action)) {
            confirmDonation(request, response, out);
        } else if ("processRequest".equals(action)) {
            processRequest(response, out);
        } else if ("confirmRequest".equals(action)) {
            confirmRequest(request, response, out);
        } else if ("processedHistory".equals(action)) {
            processedHistory(response, out);
        } else {
            fail(out, "Invalid action");
        }
    }

    // ==========================================
    // ADD DONATION  (saved in MySQL, then added to
    // the linked list and the stack)
    // ==========================================

    private void addDonation(HttpServletRequest request, PrintWriter out) {

        String foodName = request.getParameter("foodName");
        String quantityText = request.getParameter("quantity");
        String donorName = request.getParameter("donorName");

        if (isBlank(foodName) || isBlank(quantityText) || isBlank(donorName)) {
            fail(out, "All fields are required");
            return;
        }

        try {
            int quantity = Integer.parseInt(quantityText.trim());

            if (quantity <= 0) {
                fail(out, "Quantity must be greater than zero");
                return;
            }

            if (FoodShareService.isDonationStackFull()) {
                fail(out, "The donation stack is full. Process a donation before adding a new one.");
                return;
            }

            String sql = "INSERT INTO Donations "
                       + "(food_name, quantity, donor_username, status) "
                       + "VALUES (?, ?, ?, 'AVAILABLE')";

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(
                         sql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setString(1, foodName.trim());
                ps.setInt(2, quantity);
                ps.setString(3, donorName.trim());

                if (ps.executeUpdate() < 1) {
                    fail(out, "Donation was not saved");
                    return;
                }

                int dbId;
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) {
                        fail(out, "Donation was saved but no ID was returned");
                        return;
                    }
                    dbId = keys.getInt(1);
                }

                String donationId = FoodShareService.addSavedDonation(
                        dbId, foodName.trim(), quantity, donorName.trim());

                out.print("{\"success\":true,"
                        + "\"message\":\"Donation saved successfully.\","
                        + "\"donationId\":" + jsonQuote(donationId) + "}");
            }

        } catch (NumberFormatException e) {
            fail(out, "Quantity must be a valid whole number");
        } catch (SQLException e) {
            e.printStackTrace();
            fail(out, "Database error while saving donation");
        }
    }

    // ==========================================
    // ADD REQUEST  (saved in MySQL, then queued)
    // ==========================================

    private void addRequest(HttpServletRequest request, PrintWriter out) {

        String recipientName = request.getParameter("recipientName");
        String foodRequired = request.getParameter("foodRequired");
        String quantityText = request.getParameter("quantityRequired");

        try {
            if (isBlank(recipientName) || isBlank(foodRequired)
                    || isBlank(quantityText)) {
                fail(out, "Enter valid recipient, food and positive quantity details.");
                return;
            }

            int quantityRequired = Integer.parseInt(quantityText.trim());

            if (quantityRequired <= 0) {
                fail(out, "Enter valid recipient, food and positive quantity details.");
                return;
            }

            if (FoodShareService.isRequestQueueFull()) {
                fail(out, "Request queue is full");
                return;
            }

            String requestId = FoodShareService.nextRequestId();

            String sql = "INSERT INTO FoodShareAppRequests "
                       + "(request_id, recipient_name, food_required, "
                       + "quantity_required, status) "
                       + "VALUES (?, ?, ?, ?, 'PENDING')";

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, requestId);
                ps.setString(2, recipientName.trim());
                ps.setString(3, foodRequired.trim());
                ps.setInt(4, quantityRequired);
                ps.executeUpdate();
            }

            FoodShareService.enqueueSavedRequest(new FoodRequest(
                    requestId, recipientName.trim(),
                    foodRequired.trim(), quantityRequired));

            out.print("{\"success\":true,"
                    + "\"message\":\"Food request added successfully\","
                    + "\"requestId\":" + jsonQuote(requestId) + "}");

        } catch (NumberFormatException e) {
            fail(out, "Quantity must be a positive whole number");
        } catch (SQLException e) {
            e.printStackTrace();
            fail(out, "Database error while saving food request");
        }
    }

    // ==========================================
    // VIEW DONATIONS / VIEW STACK
    // ==========================================

    private void printDonations(PrintWriter out, List<FoodDonation> donations) {
        out.print("[");
        for (int i = 0; i < donations.size(); i++) {
            FoodDonation d = donations.get(i);
            if (i > 0) out.print(",");
            out.print("{\"donationId\":" + jsonQuote(d.getDonationId())
                    + ",\"foodName\":" + jsonQuote(d.getFoodName())
                    + ",\"quantity\":" + d.getQuantity()
                    + ",\"donorName\":" + jsonQuote(d.getDonorName()) + "}");
        }
        out.print("]");
    }

    // ==========================================
    // VIEW REQUESTS  (pending requests from MySQL)
    // ==========================================

    private void viewRequests(HttpServletResponse response, PrintWriter out) {
        String sql = "SELECT request_id, recipient_name, food_required, quantity_required "
                + "FROM FoodShareAppRequests WHERE status='PENDING' "
                + "ORDER BY created_at, request_id";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            out.print("[");
            boolean first = true;
            while (rs.next()) {
                if (!first) out.print(",");
                first = false;
                out.print("{\"requestId\":" + jsonQuote(rs.getString("request_id"))
                        + ",\"recipientName\":" + jsonQuote(rs.getString("recipient_name"))
                        + ",\"foodRequired\":" + jsonQuote(rs.getString("food_required"))
                        + ",\"quantityRequired\":" + rs.getInt("quantity_required") + "}");
            }
            out.print("]");
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            fail(out, "Could not load saved food requests.");
        }
    }

    // ==========================================
    // PROCESS DONATION - step 1: find matching recipients.
    // Nothing is changed; the donation stays available.
    // ==========================================

    private void processDonation(HttpServletResponse response, PrintWriter out) {
        try {
            FoodDonation donation = FoodShareService.getNextDonation();

            if (donation == null) {
                fail(out, "No pending donations are available.");
                return;
            }

            List<Map<String, Object>> matches =
                    FoodMatchingService.findRecipients(donation);

            out.print("{\"success\":true,");
            out.print("\"message\":" + jsonQuote(matches.isEmpty()
                    ? "No matching recipient found. The donation is still available."
                    : "Matching recipients found. Select one to continue.") + ",");
            out.print("\"donationId\":" + jsonQuote(donation.getDonationId()) + ",");
            out.print("\"foodName\":" + jsonQuote(donation.getFoodName()) + ",");
            out.print("\"quantity\":" + donation.getQuantity() + ",");
            out.print("\"donorName\":" + jsonQuote(donation.getDonorName()) + ",");
            out.print("\"matches\":[");

            for (int i = 0; i < matches.size(); i++) {
                Map<String, Object> m = matches.get(i);
                if (i > 0) out.print(",");
                out.print("{\"needId\":" + m.get("needId")
                        + ",\"name\":" + jsonQuote(String.valueOf(m.get("name")))
                        + ",\"address\":" + jsonQuote(String.valueOf(m.get("address")))
                        + ",\"phone\":" + jsonQuote(String.valueOf(m.get("phone")))
                        + ",\"email\":" + jsonQuote(String.valueOf(m.get("email")))
                        + ",\"quantity\":" + m.get("quantity") + "}");
            }
            out.print("]}");

        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            fail(out, "Database error while finding matching recipients.");
        }
    }

    // ==========================================
    // PROCESS DONATION - step 2: confirmed (user clicked Yes).
    // Updates the recipient need and the donation in ONE transaction.
    // ==========================================

    private void confirmDonation(HttpServletRequest request,
            HttpServletResponse response, PrintWriter out) {

        String donationId = request.getParameter("donationId");
        String needIdText = request.getParameter("needId");

        if (isBlank(donationId) || isBlank(needIdText)) {
            fail(out, "Donation and recipient details are required.");
            return;
        }

        try {
            int needId = Integer.parseInt(needIdText.trim());
            int donationDbId = Integer.parseInt(donationId.trim().substring(1));

            FoodDonation donation = FoodShareService.getNextDonation();

            if (donation == null || !donation.getDonationId().equals(donationId)) {
                fail(out, "This donation is no longer available. Refresh and try again.");
                return;
            }

            Map<String, Object> selected = null;
            for (Map<String, Object> match : FoodMatchingService.findRecipients(donation)) {
                if (Integer.parseInt(String.valueOf(match.get("needId"))) == needId) {
                    selected = match;
                    break;
                }
            }

            if (selected == null) {
                fail(out, "The selected recipient is no longer eligible for this donation. Refresh the list.");
                return;
            }

            try (Connection con = DBConnection.getConnection()) {
                con.setAutoCommit(false);
                try {
                    int needUpdated;
                    try (PreparedStatement ps = con.prepareStatement(
                            "UPDATE FoodShareNeeds SET status = 'FULFILLED' "
                          + "WHERE need_id = ? AND status = 'PENDING'")) {
                        ps.setInt(1, needId);
                        needUpdated = ps.executeUpdate();
                    }

                    int donationUpdated;
                    try (PreparedStatement ps = con.prepareStatement(
                            "UPDATE Donations SET status = 'DISTRIBUTED' "
                          + "WHERE donation_id = ? AND status = 'AVAILABLE'")) {
                        ps.setInt(1, donationDbId);
                        donationUpdated = ps.executeUpdate();
                    }

                    if (needUpdated != 1 || donationUpdated != 1) {
                        con.rollback();
                        fail(out, "This donation or recipient request has already been processed.");
                        return;
                    }
                    con.commit();
                } catch (SQLException e) {
                    con.rollback();
                    throw e;
                }
            }

            FoodShareService.completeDonation(donationId);

            recordHistorySafely("DONATION", donationId, donation.getFoodName(),
                    donation.getQuantity(), String.valueOf(selected.get("name")));

            out.print("{\"success\":true,"
                    + "\"message\":\"Distribution confirmed successfully.\","
                    + "\"recipientName\":" + jsonQuote(String.valueOf(selected.get("name")))
                    + "}");

        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            fail(out, "Invalid donation or recipient selection.");
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            fail(out, "Database error while confirming distribution.");
        }
    }

    // ==========================================
    // PROCESS REQUEST - step 1: find suitable donors.
    // Nothing is changed; the request stays pending.
    // ==========================================

    private void processRequest(HttpServletResponse response, PrintWriter out) {
        try {
            FoodRequest pending = loadNextPendingRequest();

            if (pending == null) {
                fail(out, "No pending food requests are available.");
                return;
            }

            List<Map<String, Object>> matches = FoodMatchingService.findDonors(pending);

            out.print("{\"success\":true,");
            out.print("\"requestId\":" + jsonQuote(pending.getRequestId()) + ",");
            out.print("\"recipientName\":" + jsonQuote(pending.getRecipientName()) + ",");
            out.print("\"foodRequired\":" + jsonQuote(pending.getFoodRequired()) + ",");
            out.print("\"quantityRequired\":" + pending.getQuantityRequired() + ",");
            out.print("\"message\":" + jsonQuote(matches.isEmpty()
                    ? "No matching donor found. This request remains pending."
                    : "Matching donors found. Select one to continue.") + ",");
            out.print("\"matches\":[");

            for (int i = 0; i < matches.size(); i++) {
                Map<String, Object> m = matches.get(i);
                if (i > 0) out.print(",");
                out.print("{\"name\":" + jsonQuote(String.valueOf(m.get("name")))
                        + ",\"address\":" + jsonQuote(String.valueOf(m.get("address")))
                        + ",\"phone\":" + jsonQuote(String.valueOf(m.get("phone")))
                        + ",\"email\":" + jsonQuote(String.valueOf(m.get("email")))
                        + ",\"foodName\":" + jsonQuote(String.valueOf(m.get("foodName")))
                        + ",\"quantityAvailable\":" + m.get("quantityAvailable")
                        + ",\"inventoryId\":" + m.get("inventoryId") + "}");
            }
            out.print("]}");

        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            fail(out, "Database error while finding matching donors.");
        }
    }

    // ==========================================
    // PROCESS REQUEST - step 2: confirmed (user clicked Yes).
    // ==========================================

    private void confirmRequest(HttpServletRequest request,
            HttpServletResponse response, PrintWriter out) {

        String requestId = request.getParameter("requestId");
        String inventoryText = request.getParameter("inventoryId");

        if (isBlank(requestId) || isBlank(inventoryText)) {
            fail(out, "Request and donor details are required.");
            return;
        }

        try {
            int inventoryId = Integer.parseInt(inventoryText.trim());

            FoodRequest pending = loadNextPendingRequest();
            if (pending == null || !pending.getRequestId().equals(requestId)) {
                fail(out, "This request is no longer next in the queue. Refresh and try again.");
                return;
            }

            Map<String, Object> selected = null;
            for (Map<String, Object> match : FoodMatchingService.findDonors(pending)) {
                if (Integer.parseInt(String.valueOf(match.get("inventoryId"))) == inventoryId) {
                    selected = match;
                    break;
                }
            }

            if (selected == null) {
                fail(out, "The selected donor is no longer suitable for this request. Refresh the list.");
                return;
            }

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(
                         "UPDATE FoodShareAppRequests SET status='FULFILLED' "
                       + "WHERE request_id=? AND status='PENDING'")) {
                ps.setString(1, requestId);
                if (ps.executeUpdate() != 1) {
                    fail(out, "This request has already been processed.");
                    return;
                }
            }

            FoodRequest queued = FoodShareService.getNextRequest();
            if (queued != null && queued.getRequestId().equals(requestId)) {
                FoodShareService.processNextRequest();
            }

            recordHistorySafely("REQUEST", requestId, pending.getFoodRequired(),
                    pending.getQuantityRequired(), String.valueOf(selected.get("name")));

            out.print("{\"success\":true,"
                    + "\"message\":\"Request confirmed successfully.\","
                    + "\"donorName\":" + jsonQuote(String.valueOf(selected.get("name")))
                    + "}");

        } catch (NumberFormatException e) {
            fail(out, "Invalid donor selection.");
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            fail(out, "Database error while confirming request.");
        }
    }

    // ==========================================
    // PROCESSED HISTORY
    // ==========================================

    private void processedHistory(HttpServletResponse response, PrintWriter out) {
        try {
            List<Map<String, Object>> history = FoodMatchingService.getProcessedHistory();

            out.print("[");
            for (int i = 0; i < history.size(); i++) {
                Map<String, Object> h = history.get(i);
                if (i > 0) out.print(",");
                out.print("{\"historyId\":" + jsonQuote(String.valueOf(h.get("historyId")))
                        + ",\"itemType\":" + jsonQuote(String.valueOf(h.get("itemType")))
                        + ",\"itemId\":" + jsonQuote(String.valueOf(h.get("itemId")))
                        + ",\"foodName\":" + jsonQuote(String.valueOf(h.get("foodName")))
                        + ",\"quantity\":" + h.get("quantity")
                        + ",\"matchedName\":" + jsonQuote(String.valueOf(h.get("matchedName")))
                        + ",\"processedAt\":" + jsonQuote(String.valueOf(h.get("processedAt")))
                        + ",\"status\":" + jsonQuote(String.valueOf(h.get("status"))) + "}");
            }
            out.print("]");
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            fail(out, "Could not load processed history");
        }
    }

    // ==========================================
    // HELPERS
    // ==========================================

    private FoodRequest loadNextPendingRequest() throws SQLException {
        String sql = "SELECT request_id, recipient_name, food_required, quantity_required "
                + "FROM FoodShareAppRequests WHERE status='PENDING' "
                + "ORDER BY created_at, request_id LIMIT 1";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return new FoodRequest(rs.getString("request_id"),
                        rs.getString("recipient_name"),
                        rs.getString("food_required"),
                        rs.getInt("quantity_required"));
            }
        }
        return null;
    }

    // A history-log problem must never undo a confirmed distribution.
    private void recordHistorySafely(String type, String id, String food,
            int quantity, String matchedName) {
        try {
            FoodMatchingService.recordHistory(
                    type, id, food, quantity, matchedName, "COMPLETED");
        } catch (SQLException e) {
            System.out.println("FOODSHARE: could not write history: " + e.getMessage());
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static void fail(PrintWriter out, String message) {
        out.print("{\"success\":false,\"message\":" + jsonQuote(message) + "}");
    }

    private static String jsonQuote(String value) {
        if (value == null) value = "";
        StringBuilder escaped = new StringBuilder("\"");
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '\\': escaped.append("\\\\"); break;
                case '"':  escaped.append("\\\""); break;
                case '\b': escaped.append("\\b"); break;
                case '\f': escaped.append("\\f"); break;
                case '\n': escaped.append("\\n"); break;
                case '\r': escaped.append("\\r"); break;
                case '\t': escaped.append("\\t"); break;
                default:
                    if (ch < 0x20) escaped.append(String.format("\\u%04x", (int) ch));
                    else escaped.append(ch);
            }
        }
        return escaped.append('"').toString();
    }
}
