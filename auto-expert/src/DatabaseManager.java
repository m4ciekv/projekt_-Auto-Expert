import java.sql.*;

public class DatabaseManager {
    private static final String URL = "jdbc:sqlite:auto_expert.db";

    // Metoda do połączenia z bazą
    public static Connection connect() {
        try {
            Connection conn = DriverManager.getConnection(URL);
            // Włączenie obsługi kluczy obcych w SQLite (wymagane przy każdym połączeniu!)
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON;");
            }
            return conn;
        } catch (SQLException e) {
            System.out.println("Błąd połączenia: " + e.getMessage());
            return null;
        }
    }

    // Tworzenie tabel na start aplikacji - wszystko w jednym, bezpiecznym bloku
    public static void createTables() {
        String sqlKlienci = "CREATE TABLE IF NOT EXISTS klienci ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "imie TEXT NOT NULL,"
                + "nazwisko TEXT NOT NULL,"
                + "telefon TEXT);";

        String sqlPojazdy = "CREATE TABLE IF NOT EXISTS pojazdy ("
                + "vin TEXT PRIMARY KEY,"
                + "marka TEXT NOT NULL,"
                + "model TEXT NOT NULL,"
                + "klient_id INTEGER,"
                + "FOREIGN KEY(klient_id) REFERENCES klienci(id));";

        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(sqlKlienci);
            stmt.execute(sqlPojazdy);
            System.out.println("Tabele (klienci i pojazdy) zostały utworzone poprawnie.");
        } catch (SQLException e) {
            System.out.println("Błąd tworzenia tabel: " + e.getMessage());
        }
    }

    // Dodawanie klienta do bazy
    public static void dodajKlienta(String imie, String nazwisko, String telefon) {
        String sql = "INSERT INTO klienci(imie, nazwisko, telefon) VALUES(?,?,?)";
        try (Connection conn = connect(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, imie);
            pstmt.setString(2, nazwisko);
            pstmt.setString(3, telefon);
            pstmt.executeUpdate();
            System.out.println("Dodano klienta: " + imie + " " + nazwisko);
        } catch (SQLException e) {
            System.out.println("Błąd przy dodawaniu klienta: " + e.getMessage());
        }
    }

    // NOWOŚĆ: Dodawanie pojazdu przypisanego do ID klienta
    public static void dodajPojazd(String vin, String marka, String model, int klientId) {
        String sql = "INSERT INTO pojazdy(vin, marka, model, klient_id) VALUES(?,?,?,?)";
        try (Connection conn = connect(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, vin);
            pstmt.setString(2, marka);
            pstmt.setString(3, model);
            pstmt.setInt(4, klientId);
            pstmt.executeUpdate();
            System.out.println("Dodano pojazd o numerze VIN: " + vin);
        } catch (SQLException e) {
            System.out.println("Błąd przy dodawaniu pojazdu: " + e.getMessage());
        }
    }
}