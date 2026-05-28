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
        
                //moduł rozliczeniowy
        String sqlNaprawy = "CREATE TABLE IF NOT EXISTS naprawy ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "opis_usterki TEXT NOT NULL,"
                + "roboczogodziny REAL NOT NULL,"
                + "koszt_czesci REAL NOT NULL,"
                + "pojazd_vin TEXT NOT NULL,"
                + "FOREIGN KEY(pojazd_vin) REFERENCES pojazdy(vin));";

        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(sqlKlienci);
            stmt.execute(sqlPojazdy);
            stmt.execute(sqlNaprawy);
            System.out.println("Tabele (klienci, pojazdy i naprawy) zostały utworzone poprawnie.");
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

    // Dodawanie pojazdu przypisanego do ID klienta
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
    // Wyświetlanie wszystkich klientów
    public static void wyswietlKlientow() {
        String sql = "SELECT id, imie, nazwisko, telefon FROM klienci";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            System.out.println("\n================ LISTA KLIENTÓW ================");
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id") + 
                                   " | " + rs.getString("imie") + 
                                   " " + rs.getString("nazwisko") + 
                                   " | Tel: " + rs.getString("telefon"));
            }
            System.out.println("================================================");
        } catch (SQLException e) {
            System.out.println("Błąd przy pobieraniu klientów: " + e.getMessage());
        }
    }
    // Wyświetlanie pojazdów wraz z przypisanymi właścicielami
    public static void wyswietlPojazdy() {
        String sql = "SELECT p.vin, p.marka, p.model, k.imie, k.nazwisko " +
                     "FROM pojazdy p " +
                     "INNER JOIN klienci k ON p.klient_id = k.id";
                     
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            System.out.println("\n================ LISTA POJAZDÓW ================");
            while (rs.next()) {
                System.out.println("VIN: " + rs.getString("vin") + 
                                   " | " + rs.getString("marka") + 
                                   " " + rs.getString("model") + 
                                   " | Właściciel: " + rs.getString("imie") + 
                                   " " + rs.getString("nazwisko"));
            }
            System.out.println("================================================");
        } catch (SQLException e) {
            System.out.println("Błąd przy pobieraniu pojazdów: " + e.getMessage());
        }
    }
    // Dodawanie nowej naprawy do bazy
    public static void dodajNaprawe(String opis, double godziny, double czesci, String vin) {
        String sql = "INSERT INTO naprawy(opis_usterki, roboczogodziny, koszt_czesci, pojazd_vin) VALUES(?,?,?,?)";
        try (Connection conn = connect(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, opis);
            pstmt.setDouble(2, godziny);
            pstmt.setDouble(3, czesci);
            pstmt.setString(4, vin);
            pstmt.executeUpdate();
            System.out.println("Dodano kartę naprawy dla pojazdu o VIN: " + vin);
        } catch (SQLException e) {
            System.out.println("Błąd przy dodawaniu naprawy: " + e.getMessage());
        }
    }

    // MODUŁ ROZLICZENIOWY: Automatyczne wyliczanie kosztów 
    public static void wyswietlRozliczenia() {
    // Pobieramy dane naprawy oraz markę/model auta przez INNER JOIN
    String sql = "SELECT n.id, n.opis_usterki, n.roboczogodziny, n.koszt_czesci, p.marka, p.model " +
                 "FROM naprawy n " +
                 "INNER JOIN pojazdy p ON n.pojazd_vin = p.vin";
                 
    final double STAWKA_GODZINOWA = 150.0; // Stawka za roboczogodzinę w warsztacie (150 PLN/rbh)

    try (Connection conn = connect();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {
        
        System.out.println("\n================ MODUŁ ROZLICZENIOWY ================");
        boolean mamyNaprawy = false;
        
        // Tworzymy Scanner do obsługi decyzji o płatności bezgotówkowej
        java.util.Scanner bankScanner = new java.util.Scanner(System.in);
        
        while (rs.next()) {
            mamyNaprawy = true;
            double godziny = rs.getDouble("roboczogodziny");
            double czesci = rs.getDouble("koszt_czesci");
            
            // Algorytm wyliczenia sumy końcowej: (godziny * 150) + części
            double kosztRobocizny = godziny * STAWKA_GODZINOWA;
            double sumaKoncowa = kosztRobocizny + czesci;

            System.out.println("Faktura nr: " + rs.getInt("id") + 
                               " | Auto: " + rs.getString("marka") + " " + rs.getString("model") +
                               "\n -> Usługa: " + rs.getString("opis_usterki") +
                               "\n -> Robocizna: " + godziny + " rbh x " + STAWKA_GODZINOWA + " PLN = " + kosztRobocizny + " PLN" +
                               "\n -> Koszt części: " + czesci + " PLN" +
                               "\n -> ŁĄCZNIE DO ZAPŁATY: " + sumaKoncowa + " PLN");
            System.out.println("----------------------------------------------------");
            
            // płatność bezgotówkowa (symulacja terminala)
            System.out.print("Czy płatność ma zostać zrealizowana bezgotówkowo kartą/telefonem? (T/N): ");
            String decyzja = bankScanner.nextLine().trim().toUpperCase();
            
            if (decyzja.equals("T")) {
                System.out.println("\n[SYSTEM BANKOWY] Inicjalizacja połączenia z terminalem płatniczym...");
                try {
                    Thread.sleep(1500); // 1.5 sekundy sztucznego opóźnienia dla realizmu!
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.println("[SYSTEM BANKOWY] Proszę przyłożyć kartę...");
                try { Thread.sleep(1500); } catch (InterruptedException e) {}
                System.out.println("✅ [SYSTEM BANKOWY] Płatność autoryzowana pomyślnie! Pobrano kwotę: " + sumaKoncowa + " PLN.");
                System.out.println("----------------------------------------------------");
            } else {
                System.out.println("[INFO] Wybrano płatność gotówkową. Oczekiwanie na gotówkę w kasie.");
                System.out.println("----------------------------------------------------");
            }
        }
        
        if (!mamyNaprawy) {
            System.out.println("Brak zarejestrowanych napraw w bazie danych.");
        }
        System.out.println("=====================================================");
    } catch (SQLException e) {
        System.out.println("Błąd modułu rozliczeniowego: " + e.getMessage());
    }
}
}