import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class LoginSystem {
    private static final String CREDENTIALS_FILE = "credentials.txt";
    private static final String USERS_INFO_FILE = "users_info.txt";
    private static final String BLOCKED_USERS_FILE = "blocked_users.txt";
    private static final String LOGIN_HISTORY_FILE = "login_history.txt";
    private static final int MAX_ATTEMPTS = 3;
    private static Map<String, String> credentials = new HashMap<>();
    private static Map<String, Integer> loginAttempts = new HashMap<>();
    private static Map<String, Boolean> blockedUsers = new HashMap<>();
    private static Map<String, String[]> usersInfo = new HashMap<>();
    private static Map<String, String> failedLoginAttempts = new HashMap<>(); // Logs failed attempts

    public static void main(String[] args) {
        checkAndCreateFiles();
        loadCredentials();
        loadBlockedUsers();
        loadUsersInfo();

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Menu Principale");
            System.out.println("1. Login");
            System.out.println("2. Exit");
            System.out.print("Seleziona un'opzione: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consumare il newline

            if (choice == 1) {
                System.out.print("Inserisci Username: ");
                String username = scanner.nextLine();
                if (blockedUsers.getOrDefault(username, false)) {
                    System.out.println("L'utente è bloccato. Contatta l'amministratore.");
                    continue;
                }

                System.out.print("Inserisci Password: ");
                String password = scanner.nextLine();

                if (credentials.containsKey(username) && credentials.get(username).equals(password)) {
                    loginAttempts.put(username, 0);
                    LocalDateTime now = LocalDateTime.now();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                    String formattedNow = now.format(formatter);
                    String os = System.getProperty("os.name");
                    System.out.println("Login riuscito! Data e ora: " + formattedNow + " | Sistema Operativo: " + os);

                    if (username.equals("Admin") && password.equals("Admin123")) {
                        administrationPanel(scanner);
                    } else {
                        usersPanel(scanner, username);
                    }

                    logLoginHistory(username, formattedNow, os);
                } else {
                    int attempts = loginAttempts.getOrDefault(username, 0) + 1;
                    loginAttempts.put(username, attempts);
                    if (attempts >= MAX_ATTEMPTS) {
                        blockedUsers.put(username, true);
                        updateBlockedUsers();
                        System.out.println("Hai superato il numero massimo di tentativi. L'utente è stato bloccato.");
                    } else {
                        failedLoginAttempts.put(username, password);
                        System.out.println("Username o Password errati! Tentativi rimanenti: " + (MAX_ATTEMPTS - attempts));
                    }
                }
            } else if (choice == 2) {
                System.out.println("Uscita dal sistema.");
                break;
            } else {
                System.out.println("Opzione non valida. Riprova.");
            }
        }
        scanner.close();
    }

    private static void checkAndCreateFiles() {
        createFileIfNotExists(CREDENTIALS_FILE, "Admin,Admin123");
        createFileIfNotExists(USERS_INFO_FILE, "");
        createFileIfNotExists(BLOCKED_USERS_FILE, "");
        createFileIfNotExists(LOGIN_HISTORY_FILE, "");
    }

    private static void createFileIfNotExists(String filename, String defaultContent) {
        File file = new File(filename);
        if (!file.exists()) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                if (!defaultContent.isEmpty()) {
                    writer.write(defaultContent);
                    writer.newLine();
                }
                System.out.println("File " + filename + " creato.");
            } catch (IOException e) {
                System.out.println("Errore nella creazione del file " + filename + ": " + e.getMessage());
            }
        }
    }

    private static void loadCredentials() {
        loadFileToMap(CREDENTIALS_FILE, credentials);
    }

    private static void loadBlockedUsers() {
        try (BufferedReader reader = new BufferedReader(new FileReader(BLOCKED_USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                blockedUsers.put(line, true);
            }
        } catch (IOException e) {
            System.out.println("Errore nel caricamento degli utenti bloccati: " + e.getMessage());
        }
    }

    private static void loadUsersInfo() {
        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_INFO_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 5) { // Assuming format: username, nome, cognome, password, data registrazione
                    usersInfo.put(parts[0], new String[] { parts[1], parts[2], parts[3], parts[4] });
                }
            }
        } catch (IOException e) {
            System.out.println("Errore nel caricamento delle informazioni degli utenti: " + e.getMessage());
        }
    }

    private static void loadFileToMap(String filename, Map<String, String> map) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    map.put(parts[0], parts[1]);
                }
            }
        } catch (IOException e) {
            System.out.println("Errore nel caricamento del file " + filename + ": " + e.getMessage());
        }
    }

    private static void updateCredentials() {
        updateFileFromMap(CREDENTIALS_FILE, credentials);
    }

    private static void updateBlockedUsers() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(BLOCKED_USERS_FILE))) {
            for (String user : blockedUsers.keySet()) {
                writer.write(user);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Errore nell'aggiornamento degli utenti bloccati: " + e.getMessage());
        }
    }

    private static void updateUsersInfo() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_INFO_FILE))) {
            for (Map.Entry<String, String[]> entry : usersInfo.entrySet()) {
                String username = entry.getKey();
                String[] info = entry.getValue();
                writer.write(username + "," + info[0] + "," + info[1] + "," + info[2] + "," + info[3]);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Errore nell'aggiornamento delle informazioni degli utenti: " + e.getMessage());
        }
    }

    private static void updateFileFromMap(String filename, Map<String, String> map) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                writer.write(entry.getKey() + "," + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Errore nell'aggiornamento del file " + filename + ": " + e.getMessage());
        }
    }

    private static void administrationPanel(Scanner scanner) {
        while (true) {
            System.out.println("Administration Panel");
            System.out.println("1. Crea Nuovo Utente");
            System.out.println("2. Reset Password Utenti");
            System.out.println("3. Blocca/Sblocca Utenti");
            System.out.println("4. Visualizza Utenti");
            System.out.println("5. Visualizza Cronologia Accessi");
            System.out.println("6. Visualizza Tentativi di Accesso Falliti");
            System.out.println("7. Elimina Utente");
            System.out.println("8. Logout");
            System.out.print("Seleziona un'opzione: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consumare il newline

            if (choice == 1) {
                createNewUser(scanner);
            } else if (choice == 2) {
                resetUserPassword(scanner);
            } else if (choice == 3) {
                toggleUserBlock(scanner);
            } else if (choice == 4) {
                displayUsers(scanner);
            } else if (choice == 5) {
                displayLoginHistory();
            } else if (choice == 6) {
                displayFailedAttempts();
            } else if (choice == 7) {
                deleteUser(scanner);
            } else if (choice == 8) {
                System.out.println("Logout dall'Administration Panel.");
                break;
            } else {
                System.out.println("Opzione non valida. Riprova.");
            }
        }
    }

    private static void displayUsers(Scanner scanner) {
        System.out.println("Elenco utenti:");
        for (String username : credentials.keySet()) {
            String[] userInfo = usersInfo.get(username);
            if (userInfo != null) {
                System.out.println("Username: " + username);
                System.out.println("Nome: " + userInfo[0]);
                System.out.println("Cognome: " + userInfo[1]);
                System.out.println("Password: " + userInfo[2]);
                System.out.println("Data di registrazione: " + userInfo[3]);
                System.out.println("------------------------");
            }
        }
    }

    private static void createNewUser(Scanner scanner) {
        System.out.print("Inserisci il nome dell'utente: ");
        String nome = scanner.nextLine();
        System.out.print("Inserisci il cognome dell'utente: ");
        String cognome = scanner.nextLine();

        // Username generato come nome.cognome
        String username = nome.toLowerCase() + "." + cognome.toLowerCase();

        System.out.println("Username per l'utente creato automaticamente: " + username);
        System.out.print("Inserisci la password dell'utente: ");
        String password = scanner.nextLine();

        // Impostiamo i valori di email e numero di telefono come 'da aggiungere'
        String email = "da aggiungere";
        String telefono = "da aggiungere";

        // Data di registrazione
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String dataRegistrazione = now.format(formatter);

        credentials.put(username, password);
        usersInfo.put(username, new String[] { nome, cognome, email, telefono, dataRegistrazione });
        System.out.println("Utente " + username + " creato con successo!");
        updateCredentials();
        updateUsersInfo();
    }

    private static void resetUserPassword(Scanner scanner) {
        System.out.print("Inserisci l'username dell'utente di cui vuoi resettare la password: ");
        String username = scanner.nextLine();
        if (credentials.containsKey(username)) {
            System.out.print("Inserisci la nuova password: ");
            String newPassword = scanner.nextLine();
            credentials.put(username, newPassword);
            System.out.println("Password per l'utente " + username + " aggiornata con successo.");
            updateCredentials();
        } else {
            System.out.println("Username non trovato.");
        }
    }

    private static void toggleUserBlock(Scanner scanner) {
        System.out.print("Inserisci l'username dell'utente da bloccare/sbloccare: ");
        String username = scanner.nextLine();
        if (blockedUsers.containsKey(username)) {
            blockedUsers.put(username, !blockedUsers.get(username));
            updateBlockedUsers();
            System.out.println("Utente " + username + (blockedUsers.get(username) ? " bloccato." : " sbloccato."));
        } else {
            System.out.println("Utente non trovato.");
        }
    }

    private static void displayLoginHistory() {
        try (BufferedReader reader = new BufferedReader(new FileReader(LOGIN_HISTORY_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.out.println("Errore nella visualizzazione della cronologia degli accessi: " + e.getMessage());
        }
    }

    private static void displayFailedAttempts() {
        if (failedLoginAttempts.isEmpty()) {
            System.out.println("Nessun tentativo di login fallito.");
        } else {
            for (Map.Entry<String, String> entry : failedLoginAttempts.entrySet()) {
                System.out.println("Utente: " + entry.getKey() + " - Password Tentativo: " + entry.getValue());
            }
        }
    }

    private static void deleteUser(Scanner scanner) {
        System.out.print("Inserisci l'username dell'utente da eliminare: ");
        String username = scanner.nextLine();
        if (credentials.containsKey(username)) {
            credentials.remove(username);
            blockedUsers.remove(username);
            usersInfo.remove(username);
            System.out.println("Utente " + username + " eliminato.");
            updateCredentials();
            updateBlockedUsers();
            updateUsersInfo();
        } else {
            System.out.println("Utente non trovato.");
        }
    }

    private static void logLoginHistory(String username, String loginTime, String os) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOGIN_HISTORY_FILE, true))) {
            writer.write(username + "," + loginTime + "," + os);
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Errore nel log della cronologia accessi: " + e.getMessage());
        }
    }

    private static void usersPanel(Scanner scanner, String username) {
        while (true) {
            System.out.println("Pannello Utente: " + username);
            System.out.println("1. Modifica Profilo");
            System.out.println("2. Logout");
            System.out.print("Seleziona un'opzione: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consumare il newline

            if (choice == 1) {
                modifyUserProfile(scanner, username);
            } else if (choice == 2) {
                System.out.println("Logout effettuato.");
                break;
            } else {
                System.out.println("Opzione non valida. Riprova.");
            }
        }
    }

    private static void modifyUserProfile(Scanner scanner, String username) {
        String[] userInfo = usersInfo.get(username);
        if (userInfo != null) {
            System.out.println("Modifica Profilo per " + username);
            System.out.println("1. Modifica Nome: " + userInfo[0]);
            System.out.println("2. Modifica Cognome: " + userInfo[1]);
            System.out.println("3. Modifica Username (impossibile cambiare l'username)");
            System.out.println("4. Modifica Email: " + userInfo[2]);
            System.out.println("5. Modifica Numero di Telefono: " + userInfo[3]);
            System.out.println("6. Annulla");
            System.out.print("Seleziona un'opzione: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consumare il newline

            if (choice == 1) {
                System.out.print("Nuovo Nome: ");
                userInfo[0] = scanner.nextLine();
            } else if (choice == 2) {
                System.out.print("Nuovo Cognome: ");
                userInfo[1] = scanner.nextLine();
            } else if (choice == 3) {
                System.out.println("Impossibile modificare l'username.");
            } else if (choice == 4) {
                System.out.print("Nuova Email: ");
                userInfo[2] = scanner.nextLine();
            } else if (choice == 5) {
                System.out.print("Nuovo Numero di Telefono: ");
                userInfo[3] = scanner.nextLine();
            } else if (choice == 6) {
                System.out.println("Modifica annullata.");
                return;
            } else {
                System.out.println("Opzione non valida. Riprova.");
                return;
            }
            usersInfo.put(username, userInfo);
            updateUsersInfo();
            System.out.println("Profilo aggiornato con successo!");
        }
    }
}
