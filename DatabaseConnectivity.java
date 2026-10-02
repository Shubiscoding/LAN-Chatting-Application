import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DatabaseConnectivity {
    private Connection connection;

    public void StartSql() {
        try {
            String url = "jdbc:mysql://localhost:3306/LANChat?allowPublicKeyRetrieval=true&useSSL=false";
            String username = "root";
            String password = "12345";
            connection = DriverManager.getConnection(url, username, password);
            System.out.println("Database Connected");
        } catch (SQLException e) {
            System.out.println("Database Connection failed");
            e.printStackTrace();
        }
    }

    public boolean UserExists(int userId) {

        try {
            String query = "SELECT id FROM users WHERE id = ?";

            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean RegisterUser(int userId, String username, String password) {

        try {
            String query = "INSERT INTO users (id, username, password) VALUES (?, ?, ?)";

            PreparedStatement ps = connection.prepareStatement(query);

            ps.setInt(1, userId);
            ps.setString(2, username);
            ps.setString(3, password);

            ps.executeUpdate();

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isLogin(int userId, String password) {

        try {
            String query = "SELECT id FROM users WHERE id = ? AND password = ?";

            PreparedStatement ps = connection.prepareStatement(query);

            ps.setInt(1, userId);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String GetUsername(int userId) {

        try {
            String query = "SELECT username FROM users WHERE id = ?";

            PreparedStatement ps = connection.prepareStatement(query);

            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("username");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public int GetUserId(String username) {

        try {
            String query = "SELECT id FROM users WHERE username = ?";

            PreparedStatement ps = connection.prepareStatement(query);

            ps.setString(1, username);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public boolean UpdatePassword(int userId, String newPassword) {

        try {
            String query = "UPDATE users SET password = ? WHERE id = ?";

            PreparedStatement ps = connection.prepareStatement(query);

            ps.setString(1, newPassword);
            ps.setInt(2, userId);

            int rows = ps.executeUpdate();

            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean DeleteUser(int userId) {

        try {
            String query = "DELETE FROM users WHERE id = ?";

            PreparedStatement ps = connection.prepareStatement(query);

            ps.setInt(1, userId);

            int rows = ps.executeUpdate();

            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void CloseSql() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Integer> LoadIds() {

        ArrayList<Integer> ids = new ArrayList<>();

        try {
            String query = "SELECT id FROM users";

            PreparedStatement ps = connection.prepareStatement(query);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                ids.add(rs.getInt("id"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ids;
    }
}