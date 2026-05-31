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
            System.out.println("1.  Dodaj nowego klienta");
            System.out.println("2.  Wyświetl wszystkich klientów");
            System.out.println("3.  Dodaj nowy pojazd (przypisz do klienta)");
            System.out.println("4.  Wyświetl wszystkie pojazdy");
            System.out.println("5.  Dodaj nową naprawę dla pojazdu");
            System.out.println("6.  Pokaż MODUŁ ROZLICZENIOWY (Faktury)");
            System.out.println("7.  Zarządzanie zleceniami (statusy)");
            System.out.println("8.  Zarządzanie bazą części");
            System.out.println("9.  Archiwum historii serwisowej");
            System.out.println("0.  Wyjście z programu");
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
                    String imie = scanner.nextLine().trim();
                    System.out.print("Podaj nazwisko: ");
                    String nazwisko = scanner.nextLine().trim();

                    // WALIDACJA 1: Sprawdzenie czy imię i nazwisko nie są puste
                    if (imie.isEmpty() || nazwisko.isEmpty()) {
                        System.out.println("Błąd: Imię i nazwisko nie mogą być puste!");
                        break;
                    }

                    System.out.print("Podaj numer telefonu (9 cyfr): ");
                    String telefon = scanner.nextLine().trim();

                    // WALIDACJA 2: Sprawdzenie czy telefon to dokładnie 9 cyfr
                    if (!telefon.matches("\\d{9}")) {
                        System.out.println("Błąd: Numer telefonu musi składać się z dokładnie 9 cyfr! (Wprowadzono: " + telefon.length() + ")");
                        break;
                    }

                    // Jeśli dane przeszły walidację, zapisujemy do bazy
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

                    System.out.print("Podaj liczbę roboczogodzin (np. 2.5): ");
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

                    System.out.print("Podaj imię i nazwisko mechanika (lub ENTER aby pominąć): ");
                    String mechanik = scanner.nextLine().trim();

                    DatabaseManager.dodajNaprawe(opis, godziny, czesci, naprawaVin, mechanik);
                    break;

                case 6:
                    //wywołanie modułu rozliczeniowego
                    DatabaseManager.wyswietlRozliczenia();
                    break;

                case 7:
                    // MODUŁ ZARZĄDZANIA ZLECENIAMI - zmiana statusów
                    menuZlecen(scanner);
                    break;

                case 8:
                    // MODUŁ ZARZĄDZANIA BAZĄ CZĘŚCI
                    menuCzesci(scanner);
                    break;

                case 9:
                    // ARCHIWUM HISTORII SERWISOWEJ
                    menuHistoria(scanner);
                    break;

                case 0:
                    running = false;
                    System.out.println("\nZamykanie systemu AUTO-EXPERT. Do widzenia!");
                    break;

                default:
                    System.out.println("Niepoprawna opcja! Wybierz liczbę od 0 do 9.");
                    break;
            }
        }

        scanner.close();
    }

    // ==================== PODMENU: ZARZĄDZANIE ZLECENIAMI ====================
    private static void menuZlecen(Scanner scanner) {
        boolean submenu = true;
        while (submenu) {
            System.out.println("\n-------- ZARZĄDZANIE ZLECENIAMI --------");
            System.out.println("1. Wyświetl wszystkie zlecenia");
            System.out.println("2. Zmień status zlecenia");
            System.out.println("0. Powrót do menu głównego");
            System.out.println("----------------------------------------");
            System.out.print("Wybierz opcję: ");

            int w = -1;
            if (scanner.hasNextInt()) {
                w = scanner.nextInt();
                scanner.nextLine();
            } else {
                System.out.println("Błąd: Wprowadź poprawną cyfrę!");
                scanner.nextLine();
                continue;
            }

            switch (w) {
                case 1:
                    DatabaseManager.wyswietlZlecenia();
                    break;
                case 2:
                    DatabaseManager.wyswietlZlecenia();
                    System.out.print("Podaj ID zlecenia: ");
                    if (scanner.hasNextInt()) {
                        int id = scanner.nextInt();
                        scanner.nextLine();
                        System.out.println("Dostępne statusy:");
                        System.out.println("  1 - Przyjęte");
                        System.out.println("  2 - W trakcie naprawy");
                        System.out.println("  3 - Zakończone");
                        System.out.print("Wybierz nowy status: ");
                        if (scanner.hasNextInt()) {
                            int statusNr = scanner.nextInt();
                            scanner.nextLine();
                            String nowyStatus;
                            switch (statusNr) {
                                case 1: nowyStatus = "Przyjęte"; break;
                                case 2: nowyStatus = "W trakcie naprawy"; break;
                                case 3: nowyStatus = "Zakończone"; break;
                                default:
                                    System.out.println("Niepoprawny numer statusu!");
                                    continue;
                            }
                            DatabaseManager.zmienStatus(id, nowyStatus);
                        } else {
                            System.out.println("Błąd: Wprowadź cyfrę!");
                            scanner.nextLine();
                        }
                    } else {
                        System.out.println("Błąd: Wprowadź cyfrę!");
                        scanner.nextLine();
                    }
                    break;
                case 0:
                    submenu = false;
                    break;
                default:
                    System.out.println("Niepoprawna opcja!");
                    break;
            }
        }
    }

    // ==================== PODMENU: ZARZĄDZANIE BAZĄ CZĘŚCI ====================
    private static void menuCzesci(Scanner scanner) {
        boolean submenu = true;
        while (submenu) {
            System.out.println("\n-------- ZARZĄDZANIE BAZĄ CZĘŚCI --------");
            System.out.println("1. Wyświetl magazyn części");
            System.out.println("2. Dodaj nową część");
            System.out.println("3. Aktualizuj stan magazynowy");
            System.out.println("0. Powrót do menu głównego");
            System.out.println("-----------------------------------------");
            System.out.print("Wybierz opcję: ");

            int w = -1;
            if (scanner.hasNextInt()) {
                w = scanner.nextInt();
                scanner.nextLine();
            } else {
                System.out.println("Błąd: Wprowadź poprawną cyfrę!");
                scanner.nextLine();
                continue;
            }

            switch (w) {
                case 1:
                    DatabaseManager.wyswietlCzesci();
                    break;
                case 2:
                    System.out.print("Podaj nazwę części: ");
                    String nazwa = scanner.nextLine().trim();
                    if (nazwa.isEmpty()) {
                        System.out.println("Błąd: Nazwa nie może być pusta!");
                        break;
                    }
                    System.out.print("Podaj cenę (PLN): ");
                    while (!scanner.hasNextDouble()) {
                        System.out.println("Błąd: Wprowadź poprawną cenę!");
                        scanner.next();
                    }
                    double cena = scanner.nextDouble();

                    System.out.print("Podaj ilość na magazynie: ");
                    while (!scanner.hasNextInt()) {
                        System.out.println("Błąd: Wprowadź poprawną ilość!");
                        scanner.next();
                    }
                    int ilosc = scanner.nextInt();

                    System.out.print("Podaj minimalny stan magazynowy: ");
                    while (!scanner.hasNextInt()) {
                        System.out.println("Błąd: Wprowadź poprawną liczbę!");
                        scanner.next();
                    }
                    int minStan = scanner.nextInt();
                    scanner.nextLine();

                    DatabaseManager.dodajCzesc(nazwa, cena, ilosc, minStan);
                    break;
                case 3:
                    DatabaseManager.wyswietlCzesci();
                    System.out.print("Podaj ID części do aktualizacji: ");
                    if (scanner.hasNextInt()) {
                        int czId = scanner.nextInt();
                        System.out.print("Podaj nową ilość: ");
                        while (!scanner.hasNextInt()) {
                            System.out.println("Błąd: Wprowadź poprawną ilość!");
                            scanner.next();
                        }
                        int nowaIlosc = scanner.nextInt();
                        scanner.nextLine();
                        DatabaseManager.aktualizujStanCzesci(czId, nowaIlosc);
                    } else {
                        System.out.println("Błąd: Wprowadź cyfrę!");
                        scanner.nextLine();
                    }
                    break;
                case 0:
                    submenu = false;
                    break;
                default:
                    System.out.println("Niepoprawna opcja!");
                    break;
            }
        }
    }

    // ==================== PODMENU: ARCHIWUM HISTORII SERWISOWEJ ====================
    private static void menuHistoria(Scanner scanner) {
        boolean submenu = true;
        while (submenu) {
            System.out.println("\n-------- ARCHIWUM HISTORII SERWISOWEJ --------");
            System.out.println("1. Szukaj historii po numerze VIN");
            System.out.println("2. Szukaj historii po nazwisku klienta");
            System.out.println("0. Powrót do menu głównego");
            System.out.println("----------------------------------------------");
            System.out.print("Wybierz opcję: ");

            int w = -1;
            if (scanner.hasNextInt()) {
                w = scanner.nextInt();
                scanner.nextLine();
            } else {
                System.out.println("Błąd: Wprowadź poprawną cyfrę!");
                scanner.nextLine();
                continue;
            }

            switch (w) {
                case 1:
                    System.out.print("Podaj numer VIN: ");
                    String vinSearch = scanner.nextLine().trim();
                    if (vinSearch.isEmpty()) {
                        System.out.println("Błąd: Numer VIN nie może być pusty!");
                        break;
                    }
                    DatabaseManager.historiaPoVin(vinSearch);
                    break;
                case 2:
                    System.out.print("Podaj nazwisko klienta: ");
                    String nazwiskoSearch = scanner.nextLine().trim();
                    if (nazwiskoSearch.isEmpty()) {
                        System.out.println("Błąd: Nazwisko nie może być puste!");
                        break;
                    }
                    DatabaseManager.historiaPoNazwisku(nazwiskoSearch);
                    break;
                case 0:
                    submenu = false;
                    break;
                default:
                    System.out.println("Niepoprawna opcja!");
                    break;
            }
        }
    }
}
