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

        // Tabela napraw ze statusem i mechanikiem
        String sqlNaprawy = "CREATE TABLE IF NOT EXISTS naprawy ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "opis_usterki TEXT NOT NULL,"
                + "roboczogodziny REAL NOT NULL,"
                + "koszt_czesci REAL NOT NULL,"
                + "pojazd_vin TEXT NOT NULL,"
                + "status TEXT DEFAULT 'Przyjęte',"
                + "mechanik TEXT DEFAULT '',"
                + "data_przyjecia TEXT DEFAULT (date('now')),"
                + "FOREIGN KEY(pojazd_vin) REFERENCES pojazdy(vin));";

        // Tabela bazy części zamiennych
        String sqlCzesci = "CREATE TABLE IF NOT EXISTS czesci ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "nazwa TEXT NOT NULL,"
                + "cena REAL NOT NULL,"
                + "ilosc INTEGER NOT NULL DEFAULT 0,"
                + "min_stan INTEGER NOT NULL DEFAULT 5);";

        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(sqlKlienci);
            stmt.execute(sqlPojazdy);
            stmt.execute(sqlNaprawy);
            stmt.execute(sqlCzesci);

            // Dodanie kolumn status, mechanik, data_przyjecia jeśli tabela naprawy już istnieje bez nich
            try { stmt.execute("ALTER TABLE naprawy ADD COLUMN status TEXT DEFAULT 'Przyjęte';"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE naprawy ADD COLUMN mechanik TEXT DEFAULT '';"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE naprawy ADD COLUMN data_przyjecia TEXT DEFAULT '';"); } catch (SQLException ignored) {}

            System.out.println("Baza danych AUTO-EXPERT zainicjalizowana poprawnie.");
        } catch (SQLException e) {
            System.out.println("Błąd tworzenia tabel: " + e.getMessage());
        }
    }

    // ==================== MODUŁ KLIENTÓW ====================

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

    // Wyświetlanie wszystkich klientów
    public static void wyswietlKlientow() {
        String sql = "SELECT id, imie, nazwisko, telefon FROM klienci";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n================ LISTA KLIENTÓW ================");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.println("ID: " + rs.getInt("id") +
                                   " | " + rs.getString("imie") +
                                   " " + rs.getString("nazwisko") +
                                   " | Tel: " + rs.getString("telefon"));
            }
            if (!found) {
                System.out.println("Brak klientów w bazie danych.");
            }
            System.out.println("================================================");
        } catch (SQLException e) {
            System.out.println("Błąd przy pobieraniu klientów: " + e.getMessage());
        }
    }

    // ==================== MODUŁ POJAZDÓW ====================

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

    // Wyświetlanie pojazdów wraz z przypisanymi właścicielami
    public static void wyswietlPojazdy() {
        String sql = "SELECT p.vin, p.marka, p.model, k.imie, k.nazwisko " +
                     "FROM pojazdy p " +
                     "INNER JOIN klienci k ON p.klient_id = k.id";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n================ LISTA POJAZDÓW ================");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.println("VIN: " + rs.getString("vin") +
                                   " | " + rs.getString("marka") +
                                   " " + rs.getString("model") +
                                   " | Właściciel: " + rs.getString("imie") +
                                   " " + rs.getString("nazwisko"));
            }
            if (!found) {
                System.out.println("Brak pojazdów w bazie danych.");
            }
            System.out.println("================================================");
        } catch (SQLException e) {
            System.out.println("Błąd przy pobieraniu pojazdów: " + e.getMessage());
        }
    }

    // ==================== MODUŁ ZLECEŃ / NAPRAW ====================

    // Dodawanie nowej naprawy do bazy
    public static void dodajNaprawe(String opis, double godziny, double czesci, String vin, String mechanik) {
        String sql = "INSERT INTO naprawy(opis_usterki, roboczogodziny, koszt_czesci, pojazd_vin, status, mechanik, data_przyjecia) VALUES(?,?,?,?,?,?,date('now'))";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, opis);
            pstmt.setDouble(2, godziny);
            pstmt.setDouble(3, czesci);
            pstmt.setString(4, vin);
            pstmt.setString(5, "Przyjęte");
            pstmt.setString(6, mechanik);
            pstmt.executeUpdate();
            System.out.println("Dodano kartę naprawy dla pojazdu o VIN: " + vin);
        } catch (SQLException e) {
            System.out.println("Błąd przy dodawaniu naprawy: " + e.getMessage());
        }
    }

    // Wyświetlanie listy zleceń z ich statusami
    public static void wyswietlZlecenia() {
        String sql = "SELECT n.id, n.opis_usterki, n.status, n.mechanik, n.data_przyjecia, p.marka, p.model, p.vin " +
                     "FROM naprawy n " +
                     "INNER JOIN pojazdy p ON n.pojazd_vin = p.vin";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n================ LISTA ZLECEŃ ================");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.println("Zlecenie #" + rs.getInt("id") +
                                   " | Auto: " + rs.getString("marka") + " " + rs.getString("model") +
                                   " (VIN: " + rs.getString("vin") + ")" +
                                   "\n   Usterka: " + rs.getString("opis_usterki") +
                                   "\n   Status: " + rs.getString("status") +
                                   " | Mechanik: " + (rs.getString("mechanik").isEmpty() ? "nieprzypisany" : rs.getString("mechanik")) +
                                   " | Data: " + rs.getString("data_przyjecia"));
                System.out.println("------------------------------------------------");
            }
            if (!found) {
                System.out.println("Brak zleceń w bazie danych.");
            }
            System.out.println("===============================================");
        } catch (SQLException e) {
            System.out.println("Błąd przy pobieraniu zleceń: " + e.getMessage());
        }
    }

    // Zmiana statusu zlecenia (Przyjęte -> W trakcie -> Zakończone)
    public static void zmienStatus(int id, String nowyStatus) {
        String sql = "UPDATE naprawy SET status = ? WHERE id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nowyStatus);
            pstmt.setInt(2, id);
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Zmieniono status zlecenia #" + id + " na: " + nowyStatus);
            } else {
                System.out.println("Nie znaleziono zlecenia o ID: " + id);
            }
        } catch (SQLException e) {
            System.out.println("Błąd zmiany statusu: " + e.getMessage());
        }
    }

    // ==================== MODUŁ BAZY CZĘŚCI ====================

    // Dodawanie części do magazynu
    public static void dodajCzesc(String nazwa, double cena, int ilosc, int minStan) {
        String sql = "INSERT INTO czesci(nazwa, cena, ilosc, min_stan) VALUES(?,?,?,?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nazwa);
            pstmt.setDouble(2, cena);
            pstmt.setInt(3, ilosc);
            pstmt.setInt(4, minStan);
            pstmt.executeUpdate();
            System.out.println("Dodano część: " + nazwa + " (ilość: " + ilosc + " szt.)");
        } catch (SQLException e) {
            System.out.println("Błąd przy dodawaniu części: " + e.getMessage());
        }
    }

    // Wyświetlanie magazynu części z alertem o niskim stanie
    public static void wyswietlCzesci() {
        String sql = "SELECT id, nazwa, cena, ilosc, min_stan FROM czesci ORDER BY nazwa";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n================ MAGAZYN CZĘŚCI ================");
            boolean found = false;
            while (rs.next()) {
                found = true;
                int ilosc = rs.getInt("ilosc");
                int minStan = rs.getInt("min_stan");
                String alert = (ilosc <= minStan) ? " ⚠ NISKI STAN!" : "";
                System.out.println("ID: " + rs.getInt("id") +
                                   " | " + rs.getString("nazwa") +
                                   " | Cena: " + rs.getDouble("cena") + " PLN" +
                                   " | Ilość: " + ilosc + " szt." +
                                   " | Min: " + minStan + " szt." + alert);
            }
            if (!found) {
                System.out.println("Magazyn jest pusty.");
            }
            System.out.println("================================================");
        } catch (SQLException e) {
            System.out.println("Błąd przy pobieraniu części: " + e.getMessage());
        }
    }

    // Aktualizacja stanu magazynowego części
    public static void aktualizujStanCzesci(int id, int nowaIlosc) {
        String sql = "UPDATE czesci SET ilosc = ? WHERE id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, nowaIlosc);
            pstmt.setInt(2, id);
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Zaktualizowano stan części ID: " + id + " -> " + nowaIlosc + " szt.");
                // Sprawdzenie czy stan spadł poniżej minimum
                sprawdzMinStan(id);
            } else {
                System.out.println("Nie znaleziono części o ID: " + id);
            }
        } catch (SQLException e) {
            System.out.println("Błąd aktualizacji stanu: " + e.getMessage());
        }
    }

    // Zgłoszenie niskiego stanu magazynowego (automatyczne)
    private static void sprawdzMinStan(int id) {
        String sql = "SELECT nazwa, ilosc, min_stan FROM czesci WHERE id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int ilosc = rs.getInt("ilosc");
                int minStan = rs.getInt("min_stan");
                if (ilosc <= minStan) {
                    System.out.println("⚠ UWAGA: Część \"" + rs.getString("nazwa") +
                                       "\" ma niski stan magazynowy! (" + ilosc + "/" + minStan + " szt.) " +
                                       "- Należy złożyć zamówienie!");
                }
            }
        } catch (SQLException e) {
            System.out.println("Błąd sprawdzania stanu: " + e.getMessage());
        }
    }

    // ==================== ARCHIWUM HISTORII SERWISOWEJ ====================

    // Wyszukiwanie historii serwisowej po numerze VIN
    public static void historiaPoVin(String vin) {
        String sql = "SELECT n.id, n.opis_usterki, n.roboczogodziny, n.koszt_czesci, n.status, n.mechanik, n.data_przyjecia, " +
                     "p.marka, p.model, k.imie, k.nazwisko " +
                     "FROM naprawy n " +
                     "INNER JOIN pojazdy p ON n.pojazd_vin = p.vin " +
                     "INNER JOIN klienci k ON p.klient_id = k.id " +
                     "WHERE p.vin = ? " +
                     "ORDER BY n.id DESC";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, vin);
            ResultSet rs = pstmt.executeQuery();

            System.out.println("\n========== HISTORIA SERWISOWA (VIN: " + vin + ") ==========");
            boolean found = false;
            while (rs.next()) {
                found = true;
                double kosztRobocizny = rs.getDouble("roboczogodziny") * 150.0;
                double suma = kosztRobocizny + rs.getDouble("koszt_czesci");
                System.out.println("Naprawa #" + rs.getInt("id") + " | Data: " + rs.getString("data_przyjecia") +
                                   "\n   Auto: " + rs.getString("marka") + " " + rs.getString("model") +
                                   " | Właściciel: " + rs.getString("imie") + " " + rs.getString("nazwisko") +
                                   "\n   Usterka: " + rs.getString("opis_usterki") +
                                   "\n   Status: " + rs.getString("status") +
                                   " | Mechanik: " + (rs.getString("mechanik").isEmpty() ? "nieprzypisany" : rs.getString("mechanik")) +
                                   "\n   Koszt: " + suma + " PLN (robocizna: " + kosztRobocizny + " + części: " + rs.getDouble("koszt_czesci") + ")");
                System.out.println("----------------------------------------------------");
            }
            if (!found) {
                System.out.println("Brak historii napraw dla podanego numeru VIN.");
            }
            System.out.println("=========================================================");
        } catch (SQLException e) {
            System.out.println("Błąd wyszukiwania historii: " + e.getMessage());
        }
    }

    // Wyszukiwanie historii serwisowej po nazwisku klienta
    public static void historiaPoNazwisku(String nazwisko) {
        String sql = "SELECT n.id, n.opis_usterki, n.roboczogodziny, n.koszt_czesci, n.status, n.mechanik, n.data_przyjecia, " +
                     "p.marka, p.model, p.vin, k.imie, k.nazwisko " +
                     "FROM naprawy n " +
                     "INNER JOIN pojazdy p ON n.pojazd_vin = p.vin " +
                     "INNER JOIN klienci k ON p.klient_id = k.id " +
                     "WHERE k.nazwisko LIKE ? " +
                     "ORDER BY n.id DESC";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + nazwisko + "%");
            ResultSet rs = pstmt.executeQuery();

            System.out.println("\n========== HISTORIA SERWISOWA (Klient: " + nazwisko + ") ==========");
            boolean found = false;
            while (rs.next()) {
                found = true;
                double kosztRobocizny = rs.getDouble("roboczogodziny") * 150.0;
                double suma = kosztRobocizny + rs.getDouble("koszt_czesci");
                System.out.println("Naprawa #" + rs.getInt("id") + " | Data: " + rs.getString("data_przyjecia") +
                                   "\n   Klient: " + rs.getString("imie") + " " + rs.getString("nazwisko") +
                                   "\n   Auto: " + rs.getString("marka") + " " + rs.getString("model") + " (VIN: " + rs.getString("vin") + ")" +
                                   "\n   Usterka: " + rs.getString("opis_usterki") +
                                   "\n   Status: " + rs.getString("status") +
                                   " | Mechanik: " + (rs.getString("mechanik").isEmpty() ? "nieprzypisany" : rs.getString("mechanik")) +
                                   "\n   Koszt: " + suma + " PLN");
                System.out.println("----------------------------------------------------");
            }
            if (!found) {
                System.out.println("Brak historii napraw dla podanego klienta.");
            }
            System.out.println("=========================================================");
        } catch (SQLException e) {
            System.out.println("Błąd wyszukiwania historii: " + e.getMessage());
        }
    }

    // ==================== MODUŁ ROZLICZENIOWY ====================

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
