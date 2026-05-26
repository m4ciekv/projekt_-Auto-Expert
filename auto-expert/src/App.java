public class App {
    public static void main(String[] args) {
        // Wywołujemy Twoją metodę tworzącą obie tabele
        DatabaseManager.createTables();
        
        // Dodajemy testowego klienta
        DatabaseManager.dodajKlienta("Maciej", "Jagodzinski", "123456789");
    }
}