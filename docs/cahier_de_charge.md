# Cahier de charge — Gestion des Congés (CHU Ibn Sina)

## Contexte et objectifs

- Contexte: Application desktop de gestion des congés pour le CHU Ibn Sina.
- Objectifs:
  - Gérer les employés et leurs droits annuels.
  - Enregistrer les demandes de congé avec calcul des jours ouvrés.
  - Valider/rejeter les demandes, suivre le solde annuel.
  - Visualiser les demandes en attente et approuvées.
  - Branding avec logo et lancement via icône bureau.

## Périmètre fonctionnel

- Gestion des employés:
  - Créer/afficher des employés avec `id`, `nom`, `droit annuel`.
  - Écran Employés: ajout + liste.
- Demandes de congé:
  - Saisie `ID employé`, `date début`, `date fin`, `type`.
  - Calcul des jours ouvrés (exclusion samedi/dimanche).
  - Attribution d’un numéro lisible (`display_id`) auto-incrémenté.
- Validation:
  - Liste En Attente, actions Approuver/Rejeter avec motif.
  - Liste Approuvés avec `N°`, `dates`, `type`, `jours`.
- Solde:
  - Affichage du solde annuel restant par employé et année.
  - Le solde diminue uniquement par des congés `ANNUAL` approuvés.
- Branding et lancement:
  - Logo affiché dans l’en-tête et icône de fenêtre.
  - Icône bureau Windows pour lancer l’application en mode silencieux.

## Règles métier

- Jours ouvrés:
  - Compter tous les jours de `start` à `end` inclus.
  - Exclure `samedi` et `dimanche`.
- Types de congé:
  - `ANNUAL`, `SICK`, `UNPAID`, `MATERNITY`, `PATERNITY`.
- Statuts:
  - `PENDING`, `APPROVED`, `REJECTED`.
- Solde annuel:
  - `solde = droit_annuel - somme(jours des demandes ANNUAL APPROVED sur l’année)`.
  - Troncature à 0 si négatif.
- Identifiants:
  - `id` technique = UUID (interne).
  - `display_id` = entier auto-incrémenté affiché à l’utilisateur.

## Modèle de données (SQLite)

- Table `employees`:
  - `id TEXT PRIMARY KEY`
  - `name TEXT NOT NULL`
  - `annual_entitlement INTEGER NOT NULL`
- Table `leave_requests`:
  - `id TEXT UNIQUE`
  - `employee_id TEXT NOT NULL` → FK `employees(id)`
  - `start_date TEXT NOT NULL` (ISO `yyyy-MM-dd`)
  - `end_date TEXT NOT NULL`
  - `type TEXT NOT NULL`
  - `year INTEGER NOT NULL`
  - `days INTEGER NOT NULL` (jours ouvrés)
  - `status TEXT NOT NULL`
  - `rejection_reason TEXT` (nullable)
  - `display_id INTEGER` (numéro lisible)
- Table `sequences`:
  - `name TEXT PRIMARY KEY`
  - `value INTEGER NOT NULL`

## Architecture technique

- Langage: Java SE
- UI: Swing (desktop)
- Persistance: SQLite via JDBC
- Pattern: Service + Repository (interfaces + impls)
- Fichiers clefs:
  - Entrée:
    - `src/main/java/com/gcl/conge/Main.java:13` initialisation DB et choix des repos.
    - `src/main/java/com/gcl/conge/Main.java:20` bascule `gui`/`demo`.
  - Service:
    - `requestLeave`: `src/main/java/com/gcl/conge/service/LeaveService.java:30`
    - `approveRequest`: `src/main/java/com/gcl/conge/service/LeaveService.java:37`
    - `rejectRequest`: `src/main/java/com/gcl/conge/service/LeaveService.java:52`
    - `getAnnualRemainingDays`: `src/main/java/com/gcl/conge/service/LeaveService.java:62`
    - `listPendingRequests`: `src/main/java/com/gcl/conge/service/LeaveService.java:73`
    - `listApprovedRequests`: `src/main/java/com/gcl/conge/service/LeaveService.java:76`
  - DB:
    - Schéma: `src/main/java/com/gcl/conge/db/Database.java:14`
    - Séquences: `src/main/java/com/gcl/conge/db/Database.java:63`
  - Repositories:
    - JDBC Employés: `src/main/java/com/gcl/conge/repo/JdbcEmployeeRepository.java:18`
    - JDBC Demandes: `src/main/java/com/gcl/conge/repo/JdbcLeaveRequestRepository.java:20`
  - UI:
    - Écrans/Tabs: `src/main/java/com/gcl/conge/ui/CongeApp.java:41`
    - Logo loader: `src/main/java/com/gcl/conge/ui/LogoLoader.java:10`

## Interfaces utilisateur

- Onglet Employés:
  - Formulaire d’ajout (ID, Nom, Droit annuel).
  - Table non modifiable, bouton Rafraîchir.
- Onglet Demande Congé:
  - Saisie (ID employé, Début, Fin, Type).
  - Résultat: message avec `N°`, `Jours`, `Type`.
- Onglet Validation:
  - En Attente: table avec `N°`, `Employé`, `Début`, `Fin`, `Type`, actions Approuver/Rejeter.
  - Approuvés: table avec `N°`, `Employé`, `Début`, `Fin`, `Type`, `Jours`.
- Onglet Solde Annuel:
  - Saisie (ID employé, Année) et affichage du solde.

## Exigences non fonctionnelles

- Performance:
  - Réactivité UI locale, requêtes SQLite rapides.
- Fiabilité:
  - Persistance des statuts; ID lisibles persistés.
- Sécurité:
  - Pas de secrets en clair; pas de journaux sensibles.
- Ergonomie:
  - Tables non éditables; formulaires clairs; ID lisibles.
- Maintenance:
  - Architecture modulaire, interfaces de repository.

## Déploiement et lancement

- Prérequis:
  - Java installé; SQLite JDBC `lib/sqlite-jdbc-3.51.1.0.jar`.
- Compilation/Exécution:
  - `javac -cp lib/sqlite-jdbc-3.51.1.0.jar -d out (Get-ChildItem src/main/java -Recurse -Filter *.java).FullName`
  - `java -cp "out;lib/sqlite-jdbc-3.51.1.0.jar" com.gcl.conge.Main gui`
- Icône bureau (Windows):
  - `run_gui.bat` + `run_gui.vbs` pour lancement silencieux.
  - Raccourci: `C:\Users\DELL\Desktop\CHU Ibn Sina - Gestion Congé.lnk`.
  - Icône: placer `logo.jpg` (ou `logo.png`), conversion automatique en `logo.ico`.

## Tests et acceptation

- Cas principaux:
  - Ajout employé et affichage en liste.
  - Demande congé ANNUAL sur une semaine: jours=5 (lundi–vendredi).
  - Validation et contrôle du solde annuel.
  - Affichage En Attente et Approuvés cohérents.
- Critères d’acceptation:
  - Calcul jours ouvrés correct.
  - Solde annuel diminue sur approbation ANNUAL.
  - ID lisible incrémental affiché partout (formulaire, listes).
  - Lancement via icône sans fenêtre console.

## Évolutions futures

- Jours fériés, demi-journées, règles spécifiques (samedi travaillé).
- Filtre par employé/année sur listes.
- Export PDF/CSV.
- Authentification et rôles.
- Packaging en exécutable (MSI/EXE) avec JRE embarqué.
