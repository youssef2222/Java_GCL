- Application desktop pour la gestion des congés du CHU Ibn Sina, avec interface Swing.
- Onglets principaux:
  - Employés: ajouter et lister les employés (ID, Nom, Droit annuel).
  - Demande Congé: saisir une demande (ID employé, dates, type) avec calcul des jours ouvrés.
  - Validation: deux listes — En Attente et Approuvés — avec actions Approuver/Rejeter.
  - Solde Annuel: visualiser le solde restant par employé et année.
- Identifiants:
  - N° de demande lisible auto‑incrémenté (affiché dans l’UI).
  - UUID interne uniquement pour la base.
Calcul des jours

- Compte les jours ouvrés entre la date de début et la date de fin, inclusivement.
- Exclut samedi et dimanche (pas de jours fériés par défaut).
Types de congé

- ANNUAL (annuel):
  - Réduit le solde annuel après approbation.
  - Apparaît dans les listes En Attente/Approuvés avec les jours utilisés.
- SICK (maladie):
  - Ne réduit pas le solde annuel dans l’application actuelle.
  - Traité comme une demande avec statut, jours calculés à titre informatif.
- UNPAID (sans solde):
  - Ne réduit pas le solde annuel; congé non rémunéré.
- MATERNITY (maternité):
  - Ne réduit pas le solde annuel dans l’application actuelle.
- PATERNITY (paternité):
  - Ne réduit pas le solde annuel dans l’application actuelle.
Flux d’utilisation

- Créer l’employé avec son droit annuel.
- Saisir une demande:
  - Le système calcule les jours ouvrés et attribue un N° lisible.
- Valider:
  - Approuver/Rejeter depuis “En Attente”; motif de rejet possible.
  - Après approbation d’un congé ANNUAL, le solde annuel diminue.
- Suivre:
  - “Approuvés” affiche N°, employé, dates, type, jours utilisés.
  - “Solde Annuel” montre le nombre de jours restants.
