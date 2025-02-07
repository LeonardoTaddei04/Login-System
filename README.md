# Login-System
Access Management System in Java. L'applicazione consente la gestione degli accessi attraverso un'interfaccia da terminale, con supporto per amministratori e utenti.


# Access Management System

## Descrizione
Questo progetto implementa un **Access Management System** in Java. L'applicazione consente la gestione degli accessi attraverso un'interfaccia da terminale, con supporto per amministratori e utenti.

## Funzionalità
### Per gli utenti:
- **Login e Logout**
- **Modifica Profilo** (Nome, Cognome, Email, Numero di Telefono)

### Per gli amministratori:
- **Gestione Utenti** (Creazione, Visualizzazione, Rimozione)
- **Generazione Automatica Username** (formato: `nome.cognome`)
- **Visualizzazione Dettagli Utenti** (Nome, Cognome, Username, Password, Data di Registrazione)

## Installazione e Avvio
### Prerequisiti:
- Java Development Kit (JDK) 8 o superiore

### Compilazione ed Esecuzione:
```sh
javac LoginSystem.java
java LoginSystem
```

## Struttura del Codice
- **LoginSystem.java** → File principale contenente la logica del sistema
- **usersInfo.txt** → File di archiviazione dati utenti
- **access_log.txt** → File di log degli accessi

## Note
- Gli username sono generati automaticamente durante la registrazione.
- Gli utenti possono modificare i propri dati, eccetto l'username.
- Gli amministratori hanno accesso ai dati di tutti gli utenti e possono gestire gli account.
