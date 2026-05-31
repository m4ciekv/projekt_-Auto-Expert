# Auto-Expert — System zarządzania warsztatem samochodowym

System dla obsługi warsztatu samochodowego „Auto-Expert" realizowany w ramach projektu z Inżynierii Oprogramowania.

## Skład grupy
- **Maciej Jagodziński** (63732)
- **Mikhail Rodia** (61385)

## Opis projektu
Aplikacja konsolowa napisana w języku Java, umożliwiająca kompleksową obsługę warsztatu samochodowego. System korzysta z lokalnej bazy danych SQLite i zapewnia bezpieczeństwo danych poprzez mechanizm PreparedStatement (ochrona przed SQL Injection) oraz walidację danych wejściowych.

## Funkcjonalności
1. **Ewidencja klientów i pojazdów** — dodawanie i przeglądanie klientów (z walidacją telefonu) oraz pojazdów (z walidacją VIN)
2. **Zarządzanie zleceniami (naprawami)** — rejestracja napraw, zmiana statusów (Przyjęte → W trakcie naprawy → Zakończone), przypisywanie mechaników
3. **Zarządzanie bazą części i materiałów** — magazyn części z automatycznym alertem niskiego stanu magazynowego
4. **Moduł rozliczeniowy** — automatyczne generowanie kosztorysów (roboczogodziny × 150 PLN/rbh + części) z symulacją płatności bezgotówkowej
5. **Archiwum historii serwisowej** — przeglądanie historii napraw z filtrowaniem po numerze VIN lub nazwisku klienta

## Technologie
- **Język programowania:** Java (JDK 17)
- **Baza danych:** SQLite (sterownik JDBC: sqlite-jdbc-3.53.1.0)
- **IDE:** Visual Studio Code z rozszerzeniami Java
- **Kontrola wersji:** Git / GitHub

## Struktura projektu
```
projekt_-Auto-Expert/
├── README.md
├── Dokumentacja_Auto-Expert.docx
└── auto-expert/
    ├── src/
    │   ├── App.java              # Menu główne i interfejs użytkownika
    │   └── DatabaseManager.java  # Warstwa dostępu do bazy danych
    ├── lib/
    │   └── sqlite-jdbc-3.53.1.0.jar
    └── bin/                      # Skompilowane pliki .class
```

## Uruchomienie

### Wymagania
- Java JDK 17 lub nowsza

### Kompilacja
```bash
cd auto-expert
javac -cp "lib/sqlite-jdbc-3.53.1.0.jar" -d bin src/*.java
```

### Uruchomienie
```bash
java -cp "bin:lib/sqlite-jdbc-3.53.1.0.jar" App
```
> Na systemie Windows należy zamienić `:` na `;` w classpath.

## Dokumentacja
Pełna dokumentacja projektowa znajduje się w pliku `Dokumentacja_Auto-Expert.docx` i zawiera:
- Opis projektu i moduły systemu
- Czasochłonność, zasoby i harmonogram (Ćw. 8)
- Zarządzanie ryzykiem i kosztorys (Ćw. 9)
- Specyfikacja przypadków użycia UML (Ćw. 10)
- Strategia bezpieczeństwa (Ćw. 11)
- Harmonogram testów (Ćw. 12)
- Instrukcja obsługi
- Zrzuty ekranowe
- Diagramy UML (klas, sekwencji, przypadków użycia)
