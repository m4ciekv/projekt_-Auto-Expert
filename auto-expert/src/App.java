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
            System.out.println("5. Dodaj nową naprawę dla pojazdu");
            System.out.println("6. Pokaż MODUŁ ROZLICZENIOWY (Faktury)");
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
                    String vin = scanner.nextLine().trim();
                    
                    //walidacja numeru VIN - musi mieć dokładnie 17 znaków
                    if (vin.length() != 17) {
                        System.out.println("Błąd: Numer VIN musi mieć 17 znaków!" + " (Wprowadzono: " + vin.length() + ")");
                        break;
                    }
                    //Gdy vin poprawny:
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

                case 5:
                System.out.println("\n--- DODAWANIE NOWEJ NAPRAWY ---");
                    System.out.print("Podaj opis usterki: ");
                    String opis = scanner.nextLine();
                    
                    System.out.print("Podaj liczbę roboczogodzin (np. 2,5): ");
                    while (!scanner.hasNextDouble()) {
                        System.out.println("Błąd: Wprowadź poprawną liczbę godzin!");
                        scanner.next();
                    }
                    double godziny = scanner.nextDouble();
                    
                    System.out.print("Podaj koszt części w PLN (np. 450): ");
                    while (!scanner.hasNextDouble()) {
                        System.out.println("Błąd: Wprowadź poprawną kwotę za części!");
                        scanner.next();
                    }
                    double czesci = scanner.nextDouble();
                    scanner.nextLine(); // Czyszczenie bufora po czytaniu liczb
                    
                    System.out.print("Podaj 17-znakowy numer VIN pojazdu: ");
                    String naprawaVin = scanner.nextLine().trim();
                    
                    // Walidacja VIN dla bezpieczeństwa relacji bazy
                    if (naprawaVin.length() != 17) {
                        System.out.println("Błąd: Podany VIN jest nieprawidłowy (musi mieć 17 znaków)!");
                        break;
                    }
                    
                    DatabaseManager.dodajNaprawe(opis, godziny, czesci, naprawaVin);
                    break;

                case 6:
                //wywołanie modułu rozliczeniowego
                    DatabaseManager.wyswietlRozliczenia();
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