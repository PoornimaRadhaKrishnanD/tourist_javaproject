import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class TouristManagementApp extends JFrame {
    // Components of the form
    private JTextField locationField;
    private JTextArea resultArea;
    private JButton searchButton;
    private DatabaseConnector dbConnector;

    public TouristManagementApp() {
        // Frame properties
        setTitle("Tourist Management System");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Initialize DatabaseConnector
        dbConnector = new DatabaseConnector();

        // Panel for user input
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(2, 2));

        // Location input
        JLabel locationLabel = new JLabel("Enter Current Location:");
        locationField = new JTextField();

        // Search button
        searchButton = new JButton("Search");

        // Adding components to input panel
        inputPanel.add(locationLabel);
        inputPanel.add(locationField);
        inputPanel.add(new JLabel()); // For alignment
        inputPanel.add(searchButton);

        // Panel for displaying the results
        resultArea = new JTextArea(5, 20);
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);

        // Add the panels to the frame
        add(inputPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Add action listener for the search button
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performSearch();
            }
        });
    }

    // Method to handle search action
    private void performSearch() {
        String location = locationField.getText();

        if (location.isEmpty()) {
            resultArea.setText("Please enter your current location.");
        } else {
            try {
                // Get coordinates for the given location
                double[] coordinates = GeocodingService.getCoordinates(location);
                double lat = coordinates[0];
                double lon = coordinates[1];

                // Get nearby cities and restaurants
                getCities(lat, lon);
                getRestaurants(lat, lon);

            } catch (Exception e) {
                e.printStackTrace();
                resultArea.setText("Error retrieving data for the location.");
            }
        }
    }

    // Method to get and display cities
    private void getCities(double lat, double lon) {
        String query = "SELECT * FROM Cities WHERE ABS(latitude - ?) <= 0.1 AND ABS(longitude - ?) <= 0.1"; // Adjust as needed
        try (Connection conn = dbConnector.connect(); 
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setDouble(1, lat);
            pstmt.setDouble(2, lon);
            ResultSet rs = pstmt.executeQuery();

            StringBuilder cities = new StringBuilder();
            while (rs.next()) {
                cities.append("City ID: ").append(rs.getInt("id"))
                      .append(", Name: ").append(rs.getString("name")).append("\n");
            }
            resultArea.append(cities.toString());
        } catch (SQLException e) {
            e.printStackTrace();
            resultArea.setText("Error retrieving cities.");
        }
    }

    // Method to get and display restaurants
    private void getRestaurants(double lat, double lon) {
        String query = "SELECT * FROM Restaurants WHERE ABS(latitude - ?) <= 0.1 AND ABS(longitude - ?) <= 0.1"; // Adjust as needed
        try (Connection conn = dbConnector.connect(); 
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setDouble(1, lat);
            pstmt.setDouble(2, lon);
            ResultSet rs = pstmt.executeQuery();

            StringBuilder restaurants = new StringBuilder();
            while (rs.next()) {
                restaurants.append("Restaurant ID: ").append(rs.getInt("id"))
                           .append(", Name: ").append(rs.getString("name"))
                           .append(", City ID: ").append(rs.getInt("city_id")).append("\n");
            }
            resultArea.append(restaurants.toString());
        } catch (SQLException e) {
            e.printStackTrace();
            resultArea.setText("Error retrieving restaurants.");
        }
    }

    // Main method to run the application
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new TouristManagementApp().setVisible(true);
            }
        });
    }
}
