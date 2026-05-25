import java.sql.*;

public class DatabaseManager {
    private static final String URL = "jdbc:sqlite:auto_expert.db";

    // Metoda do połączenia z bazą
    public static Connection connect() {
        try {
            return DriverManager.getConnection(URL);
        } catch (SQLException e) {
            System.out.println("Błąd połączenia: " + e.getMessage());
            return null;
        }
    }

    // Tworzenie tabeli klientów zgodnie z wymaganiami
    public static void createTables() {
        String sql = "CREATE TABLE IF NOT EXISTS klienci ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "imie TEXT NOT NULL,"
                + "nazwisko TEXT NOT NULL,"
                + "telefon TEXT);";

        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println("Błąd tworzenia tabeli: " + e.getMessage());
        }
    }
}