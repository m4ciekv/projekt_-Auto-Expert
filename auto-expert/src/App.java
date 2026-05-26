import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        // Inicjalizacja bazy danych i tabel na starcie
        DatabaseManager.createTables();
        
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n================================================");
            System.out.println("          SYSTEM ZARZĄDZANIA AUTO-EXPERT        ");
            System.out.println("================================================");
            System.out.println("1. Dodaj nowego klienta");
            System.out.println("2. Wyświetl wszystkich klientów");
            System.out.println("3. Dodaj nowy pojazd (przypisz do klienta)");
            System.out.println("4. Wyświetl wszystkie pojazdy");
            System.out.println("0. Wyjście z programu");
            System.out.println("================================================");
            System.out.print("Wybierz opcję: ");

            int wybor = -1;
            if (scanner.hasNextInt()) {
                wybor = scanner.nextInt();
                scanner.nextLine(); // Czyszczenie bufora nowej linii
            } else {
                System.out.println("Błąd: Wprowadź poprawną cyfrę!");
                scanner.nextLine(); // Czyszczenie błędnego wpisu po użytkowniku
                continue;
            }

            switch (wybor) {
                case 1:
                    System.out.println("\n--- DODAWANIE NOWEGO KLIENTA ---");
                    System.out.print("Podaj imię: ");
                    String imie = scanner.nextLine();
                    System.out.print("Podaj nazwisko: ");
                    String nazwisko = scanner.nextLine();
                    System.out.print("Podaj numer telefonu: ");
                    String telefon = scanner.nextLine();
                    
                    DatabaseManager.dodajKlienta(imie, nazwisko, telefon);
                    break;

                case 2:
                    DatabaseManager.wyswietlKlientow();
                    break;

                case 3:
                    System.out.println("\n--- DODAWANIE NOWEGO POJAZDU ---");
                    System.out.print("Podaj numer VIN: ");
                    String vin = scanner.nextLine();
                    System.out.print("Podaj markę: ");
                    String marka = scanner.nextLine();
                    System.out.print("Podaj model: ");
                    String model = scanner.nextLine();
                    System.out.print("Podaj ID właściciela (klienta): ");
                    
                    int klientId = -1;
                    if (scanner.hasNextInt()) {
                        klientId = scanner.nextInt();
                        scanner.nextLine(); // Czyszczenie bufora
                        DatabaseManager.dodajPojazd(vin, marka, model, klientId);
                    } else {
                        System.out.println("Błąd: ID klienta musi być liczbą!");
                        scanner.nextLine(); // Czyszczenie bufora
                    }
                    break;

                case 4:
                    DatabaseManager.wyswietlPojazdy();
                    break;

                case 0:
                    running = false;
                    System.out.println("\nZamykanie systemu AUTO-EXPERT. Do widzenia!");
                    break;

                default:
                    System.out.println("Niepoprawna opcja! Wybierz liczbę od 0 do 4.");
                    break;
            }
        }
        
        scanner.close();
    }
}