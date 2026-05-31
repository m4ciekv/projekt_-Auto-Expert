# Auto-Expert — Moduł aplikacyjny

## Struktura katalogów
- `src/` — pliki źródłowe Java (App.java, DatabaseManager.java)
- `lib/` — zależności (sterownik SQLite JDBC)
- `bin/` — skompilowane pliki .class (generowane automatycznie)

## Kompilacja i uruchomienie
```bash
javac -cp "lib/sqlite-jdbc-3.53.1.0.jar" -d bin src/*.java
java -cp "bin:lib/sqlite-jdbc-3.53.1.0.jar" App
```

## Baza danych
Plik `auto_expert.db` (SQLite) tworzony jest automatycznie przy pierwszym uruchomieniu aplikacji.
