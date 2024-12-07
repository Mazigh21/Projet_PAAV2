package Modele;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Colonie {
    /**
     * Liste des colons dans la colonie.
     */
    private List<String> colons = new ArrayList<>();

    /**
     * Liste des ressources disponibles.
     */
    private List<String> ressources = new ArrayList<>();

    /**
     * Map des préférences de chaque colon.
     */
    private Map<String, List<String>> preferences = new HashMap<>();

    /**
     * Map des affectations des ressources à chaque colon.
     */
    private Map<String, String> affectations = new HashMap<>();

    /**
     * Ensemble des conflits entre les colons.
     */
    private Set<String> conflits = new HashSet<>();

    /**
     * Scanner pour lire les entrées de l'utilisateur.
     */
    private Scanner sc = new Scanner(System.in);

    /**
     * Enumération pour suivre la section actuelle lors de la lecture du fichier.
     */
    private enum Section {
        NONE, COLON, RESSOURCE, DETESTE, PREFERENCES
    }

    /**
     * Méthode principale pour démarrer le programme.
     * Si args.length > 0, on tente de lire la configuration depuis le fichier,
     * sinon on passe en mode manuel.
     */
    public void demarrer(String[] args) {
        if (args.length > 0) {
            if (!lireFichier(args[0])) {
                System.out.println("Erreur lors de la lecture du fichier.");
                return;
            }
            // Si la lecture s'est bien passée, les colons, ressources, conflits et préférences sont déjà configurés
            proposerSolutionNaive();
            gererAffectations();
        } else {
            // Mode manuel
            initialiserColons();
            // On génère les ressources de la même taille que colons (ex: si 3 colons => ressources "1","2","3")
            for (int i = 1; i <= colons.size(); i++) {
                ressources.add(String.valueOf(i));
            }

            configurerColonie();
            proposerSolutionNaive();
            gererAffectations();
        }
    }

    /**
     * Initialise la liste des colons en demandant à l'utilisateur combien de colons (entre 1 et 26).
     */
    private void initialiserColons() {
        boolean saisieValide = false;
        int nb = 0;
        while (!saisieValide) {
            System.out.println("Veuillez entrer le nombre de colons (Max 26):");
            String input = sc.nextLine().trim();
            try {
                nb = parseNombreColons(input);
                saisieValide = true;
            } catch (NombreNonEntierException e) {
                System.out.println("Vous n'avez pas saisi un nombre, veuillez saisir un nombre entre 1 et 26.");
            } catch (NombreDeColonsInvalideException e) {
                System.out.println("Veuillez saisir un nombre entre 1 et 26.");
            }
        }

        for (int i = 0; i < nb; i++) {
            colons.add(String.valueOf((char) ('A' + i)));
        }
    }

    private int parseNombreColons(String input) throws NombreNonEntierException, NombreDeColonsInvalideException {
        int nb;
        try {
            nb = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            throw new NombreNonEntierException("La valeur saisie n'est pas un entier.");
        }

        if (nb < 1 || nb > 26) {
            throw new NombreDeColonsInvalideException("Le nombre de colons doit être entre 1 et 26.");
        }
        return nb;
    }

    /**
     * Configure la colonie en mode manuel (ajout de relations et de préférences).
     */
    private void configurerColonie() {
        boolean fin = false;
        while (!fin) {
            afficherLeMenuDeConfiguration();
            String choixStr = sc.nextLine();
            int choix = 0;
            try {
                choix = Integer.parseInt(choixStr);
            } catch (NumberFormatException e) {
                System.out.println("Choix incorrect. Réessayez.");
                continue;
            }

            switch (choix) {
                case 1:
                    try {
                        ajouterRelation();
                    } catch (RelationMemeColonException e) {
                        System.out.println("Vous ne pouvez pas ajouter une relation entre un colon et lui-même.");
                    } catch (RelationDejaExistanteException e) {
                        System.out.println("Relation déjà ajoutée.");
                    } catch (FormatIncorrectException e) {
                        System.out.println("Format incorrect ! respectez ce format : A B");
                    }
                    break;
                case 2:
                    try {
                        ajouterPreferencesManuelles();
                    } catch (PreferencesDejaAjouteesException e) {
                        System.out.println("Préférences déjà ajoutées pour ce colon.");
                    } catch (FormatIncorrectException e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                case 3:
                    fin = verification();
                    break;
                default:
                    System.out.println("Choix incorrect. Réessayez.");
            }
        }
    }

    private void afficherLeMenuDeConfiguration() {
        System.out.println("\nMenu:");
        System.out.println("1 - Ajouter une relation entre deux colons");
        System.out.println("2 - Ajouter les préférences d’un colon");
        System.out.println("3 - Fin");
        System.out.print("Choix: ");
    }

    private void ajouterRelation() throws RelationMemeColonException, RelationDejaExistanteException, FormatIncorrectException {
        System.out.print("Entrez les deux colons qui ne s'aiment pas, par exemple: A B: ");
        String input = sc.nextLine().trim();
        String[] parts = input.split("\\s+");
        if (parts.length != 2) {
            throw new FormatIncorrectException("Format incorrect");
        }

        String colon1 = parts[0];
        String colon2 = parts[1];

        if (colon1.equals(colon2)) {
            throw new RelationMemeColonException("Relation avec le même colon non autorisée.");
        }

        if (!colons.contains(colon1) || !colons.contains(colon2)) {
            System.out.println("Les colons spécifiés n'existent pas.");
            return;
        }

        // Vérifier si la relation existe déjà
        if (estEnConflit(colon1, colon2)) {
            throw new RelationDejaExistanteException("Relation déjà existante.");
        }

        String relation = colon1 + "-" + colon2;
        conflits.add(relation);
        System.out.println("Relation ajoutée entre " + colon1 + " et " + colon2);
    }

    /**
     * Vérifie si deux colons sont en conflit (déjà enregistrés dans le set 'conflits')
     */
    private boolean estEnConflit(String c1, String c2) {
        String rel1 = c1 + "-" + c2;
        String rel2 = c2 + "-" + c1;
        return conflits.contains(rel1) || conflits.contains(rel2);
    }

    private void ajouterPreferencesManuelles() throws PreferencesDejaAjouteesException, FormatIncorrectException {
        System.out.print("Entrez le nom du colon et ses préférences, exemple: A 1 2 3: ");
        String ligne = sc.nextLine().trim();
        int nombreDeColons = colons.size();

        String[] parties = ligne.split("\\s+");
        if (parties.length != nombreDeColons + 1) {
            throw new FormatIncorrectException("Format incorrect ! respectez ce format : A 1 3 2...");
        }

        String nom = parties[0];
        if (!colons.contains(nom)) {
            System.out.println("Le colon spécifié n'existe pas.");
            return;
        }

        if (preferences.containsKey(nom)) {
            throw new PreferencesDejaAjouteesException("Préférences déjà ajoutées");
        }

        List<String> prefs = new ArrayList<>();
        for (int i = 1; i < parties.length; i++) {
            String res = parties[i];
            if (!ressources.contains(res)) {
                // Si la ressource n'existe pas, vérifier si c'est un nombre valide puis la créer (dans le mode manuel c'est déjà créé)
                System.out.println("La ressource " + res + " n'existe pas.");
                return;
            }
            prefs.add(res);
        }

        if (prefs.size() != ressources.size()) {
            System.out.println("Veuillez ajouter des ressources qui existent (le nombre de ressources = le nombre de colons).");
            return;
        }

        preferences.put(nom, prefs);
        System.out.println("Préférences ajoutées pour le colon " + nom);
    }

    private boolean verification() {
        for (String colon : colons) {
            if (!preferences.containsKey(colon) || preferences.get(colon).size() != ressources.size()) {
                System.out.println("Le colon " + colon + " a des préférences incomplètes.");
                return false;
            }
        }
        return true;
    }

    private void proposerSolutionNaive() {
        affectations.clear();
        Set<String> ressourcesAttribuees = new HashSet<>();
        for (String colon : colons) {
            List<String> prefList = preferences.get(colon);
            for (String pref : prefList) {
                if (!ressourcesAttribuees.contains(pref)) {
                    affectations.put(colon, pref);
                    ressourcesAttribuees.add(pref);
                    break;
                }
            }
        }
        afficherAffectations();
    }

    private void gererAffectations() {
        boolean fin = false;
        while (!fin) {
            afficherMenuAffectation();
            String choixStr = sc.nextLine();
            int choix = 0;
            try {
                choix = Integer.parseInt(choixStr);
            } catch (NumberFormatException e) {
                System.out.println("Choix incorrect. Réessayez.");
                continue;
            }

            switch (choix) {
                case 1:
                    echangerRessources();
                    break;
                case 2:
                    afficherColonsJaloux();
                    break;
                case 3:
                    fin = true;
                    System.out.println("Programme terminé.");
                    break;
                default:
                    System.out.println("Choix incorrect. Réessayez.");
            }
            afficherAffectations();
        }
    }

    private void afficherMenuAffectation() {
        System.out.println("\nMenu:");
        System.out.println("1 - Échanger les ressources de deux colons");
        System.out.println("2 - Afficher le nombre de colons jaloux");
        System.out.println("3 - Fin");
        System.out.print("Choix: ");
    }

    private void afficherAffectations() {
        System.out.println("\nAffectations actuelles:");
        for (String colon : affectations.keySet()) {
            System.out.println(colon + ": " + affectations.get(colon));
        }
    }

    private void afficherColonsJaloux() {
        int jaloux = 0;
        List<String> colonsJaloux = new ArrayList<>();
        for (String colon : colons) {
            if (estJaloux(colon)) {
                jaloux++;
                colonsJaloux.add(colon);
            }
        }
        System.out.println("Nombre de colons jaloux: " + jaloux + " (" + String.join(", ", colonsJaloux) + ")");
    }

    /**
     * Détermine si un colon est jaloux.
     * Un colon est jaloux s'il a une ressource qui lui est affectée, mais un colon avec lequel il est en conflit
     * a une ressource mieux classée dans ses préférences.
     */
    private boolean estJaloux(String colon) {
        if (!affectations.containsKey(colon) || !preferences.containsKey(colon)) return false;

        String ressourceAffectee = affectations.get(colon);
        List<String> pref = preferences.get(colon);
        int indexAffectee = pref.indexOf(ressourceAffectee);
        if (indexAffectee == -1) return false;

        // Parcourir tous les conflits impliquant ce colon
        for (String relation : conflits) {
            if (relation.contains(colon)) {
                String[] parts = relation.split("-");
                String autreColon = parts[0].equals(colon) ? parts[1] : parts[0];

                if (affectations.containsKey(autreColon) && preferences.containsKey(colon)) {
                    String ressourceAutre = affectations.get(autreColon);
                    int indexAutre = pref.indexOf(ressourceAutre);
                    if (indexAutre != -1 && indexAutre < indexAffectee) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void echangerRessources() {
        System.out.print("Entrez les deux colons dont vous voulez échanger les ressources, exemple: A B: ");
        String ligne = sc.nextLine().trim();
        String[] parties = ligne.split("\\s+");

        if (parties.length == 2) {
            String colon1 = parties[0];
            String colon2 = parties[1];

            if (colons.contains(colon1) && colons.contains(colon2)) {
                if (affectations.containsKey(colon1) && affectations.containsKey(colon2)) {
                    String temp = affectations.get(colon1);
                    affectations.put(colon1, affectations.get(colon2));
                    affectations.put(colon2, temp);
                    System.out.println("Échange effectué entre " + colon1 + " et " + colon2);
                } else {
                    System.out.println("Un des colons n'a pas de ressource affectée, échange impossible.");
                }
            } else {
                System.out.println("Un ou plusieurs colons spécifiés n'existent pas.");
            }
        } else {
            System.out.println("Format incorrect.");
        }
    }

    /**
     * Lit le fichier de configuration et initialise la colonie.
     *
     * @param nomFichier Chemin vers le fichier de configuration.
     * @return {@code true} si la lecture et l'initialisation se sont bien passée, sinon {@code false}.
     */
    private boolean lireFichier(String nomFichier) {
        BufferedReader lecteur = null;
        try {
            lecteur = new BufferedReader(new FileReader(nomFichier));
            String ligne;
            Section sectionCourante = Section.NONE;
            int numeroLigne = 0;



            while ((ligne = lecteur.readLine()) != null) {
                numeroLigne++;
                ligne = ligne.trim();

                if (ligne.isEmpty()) {
                    continue; // ignorer les lignes vides
                }

                // Vérifier que la ligne se termine par un '.'
                if (!ligne.endsWith(".")) {
                    System.out.println("Erreur de syntaxe à la ligne " + numeroLigne + " : il manque le point à la fin.");
                    return false;
                }

                // Supprimer le point
                ligne = ligne.substring(0, ligne.length() - 1);

                // Analyser la ligne
                if (ligne.startsWith("colon(")) {
                    if (sectionCourante == Section.NONE || sectionCourante == Section.COLON) {
                        sectionCourante = Section.COLON;
                    } else {
                        String sectionIncorrecte = getNomSection(sectionCourante);
                        System.out.println("Erreur : Vous n'avez pas respecté l'ordre du fichier (colon, ressource, deteste, preferences), 'colon' après '" + sectionIncorrecte + "' à la ligne " + numeroLigne);
                        return false;
                    }

                    String nomColon = obtenirArgument(ligne, "colon", numeroLigne);
                    if (nomColon == null) return false;

                    if (!nomColon.matches("[A-Za-z0-9]+")) {
                        System.out.println("Erreur : nom de colon invalide à la ligne " + numeroLigne);
                        return false;
                    }

                    if (colons.contains(nomColon)) {
                        System.out.println("Erreur : le colon '" + nomColon + "' a déjà été défini à la ligne " + numeroLigne);
                        return false;
                    }
                    if (ressources.contains(nomColon)) {
                        System.out.println("Erreur : le nom '" + nomColon + "' est déjà utilisé comme ressource");
                        return false;
                    }

                    colons.add(nomColon);

                } else if (ligne.startsWith("ressource(")) {
                    if (sectionCourante == Section.COLON || sectionCourante == Section.RESSOURCE) {
                        if (colons.isEmpty()) {
                            System.out.println("Erreur : Aucun colon défini avant ressource.");
                            return false;
                        }
                        sectionCourante = Section.RESSOURCE;
                    } else if (sectionCourante == Section.NONE) {
                        System.out.println("Erreur : 'ressource' en premier (ça doit être après colons).");
                        return false;
                    } else {
                        String sectionIncorrecte = getNomSection(sectionCourante);
                        System.out.println("Erreur : 'ressource' après '" + sectionIncorrecte + "'");
                        return false;
                    }

                    String nomRessource = obtenirArgument(ligne, "ressource", numeroLigne);
                    if (nomRessource == null) return false;
                    if (!nomRessource.matches("[A-Za-z0-9]+")) {
                        System.out.println("Erreur : nom de ressource invalide à la ligne " + numeroLigne);
                        return false;
                    }

                    if (ressources.contains(nomRessource)) {
                        System.out.println("Erreur : ressource '" + nomRessource + "' déjà définie.");
                        return false;
                    }
                    if (colons.contains(nomRessource)) {
                        System.out.println("Erreur : nom '" + nomRessource + "' déjà utilisé comme colon.");
                        return false;
                    }

                    ressources.add(nomRessource);

                } else if (ligne.startsWith("deteste(")) {
                    if (sectionCourante == Section.RESSOURCE || sectionCourante == Section.DETESTE) {
                        if (sectionCourante == Section.RESSOURCE) {
                            if (colons.size() != ressources.size()) {
                                System.out.println("Erreur : nb colons != nb ressources.");
                                return false;
                            }
                        }
                        sectionCourante = Section.DETESTE;
                    } else {
                        String sectionIncorrecte = getNomSection(sectionCourante);
                        System.out.println("Erreur : 'deteste' pas dans l'ordre correct.");
                        return false;
                    }

                    String[] args = obtenirArguments(ligne, "deteste", 2, numeroLigne);
                    if (args == null) return false;

                    String c1 = args[0];
                    String c2 = args[1];

                    if (!colons.contains(c1) || !colons.contains(c2)) {
                        System.out.println("Erreur : colon(s) non défini(s) dans 'deteste'.");
                        return false;
                    }

                    conflits.add(c1 + "-" + c2);

                } else if (ligne.startsWith("preferences(")) {
                    if (sectionCourante == Section.DETESTE || sectionCourante == Section.PREFERENCES) {
                        if (sectionCourante == Section.DETESTE) {
                            if (colons.size() != ressources.size()) {
                                System.out.println("Erreur : nb colons != nb ressources.");
                                return false;
                            }
                        }
                        sectionCourante = Section.PREFERENCES;
                    } else {
                        System.out.println("Erreur : 'preferences' pas dans l'ordre correct.");
                        return false;
                    }

                    String[] args = obtenirArguments(ligne, "preferences", -1, numeroLigne);
                    if (args == null) return false;

                    if (args.length != ressources.size() + 1) {
                        System.out.println("Erreur : nombre de préférences incorrect.");
                        return false;
                    }

                    String c = args[0];
                    if (!colons.contains(c)) {
                        System.out.println("Erreur : colon '" + c + "' non défini.");
                        return false;
                    }

                    if (preferences.containsKey(c)) {
                        System.out.println("Erreur : préférences déjà définies pour '" + c + "'.");
                        return false;
                    }

                    List<String> prefs = new ArrayList<>();
                    for (int i = 1; i < args.length; i++) {
                        String r = args[i];
                        if (!ressources.contains(r)) {
                            System.out.println("Erreur : ressource '" + r + "' non définie.");
                            return false;
                        }

                        if (prefs.contains(r)) {
                            System.out.println("Erreur : ressource '" + r + "' dupliquée dans préférences de '" + c + "'.");
                            return false;
                        }

                        prefs.add(r);
                    }

                    preferences.put(c, prefs);

                } else {
                    System.out.println("Erreur : élément inconnu à la ligne " + numeroLigne);
                    return false;
                }
            }

            // Vérifier préférences pour tous les colons
            for (String c : colons) {
                if (!preferences.containsKey(c)) {
                    System.out.println("Erreur : les préférences du colon '" + c + "' non définies.");
                    return false;
                }
            }

            return true; // Succès

        } catch (IOException e) {
            System.out.println("Erreur lors de la lecture du fichier : " + e.getMessage());
            return false;
        } finally {
            if (lecteur != null) {
                try {
                    lecteur.close();
                } catch (IOException e) {
                    // Ignorer
                }
            }
        }
    }

    private String getNomSection(Section section) {
        switch (section) {
            case NONE: return "aucune";
            case COLON: return "colon";
            case RESSOURCE: return "ressource";
            case DETESTE: return "deteste";
            case PREFERENCES: return "preferences";
            default: return "";
        }
    }

    private String obtenirArgument(String ligne, String motCle, int numeroLigne) {
        int debut = motCle.length() + 1;
        int fin = ligne.indexOf(')', debut);
        if (fin == -1) {
            System.out.println("Erreur de syntaxe à la ligne " + numeroLigne + " : parenthèse fermante manquante.");
            return null;
        }


        String arg = ligne.substring(debut, fin).trim();
        if (arg.isEmpty()) {
            System.out.println("Erreur : argument manquant pour '" + motCle + "' à la ligne " + numeroLigne);
            return null;
        }
        return arg;
    }

    private String[] obtenirArguments(String ligne, String motCle, int nombreArgsAttendus, int numeroLigne) {
        int debut = motCle.length() + 1;
        int fin = ligne.indexOf(')', debut);
        if (fin == -1) {
            System.out.println("Erreur de syntaxe à la ligne " + numeroLigne + " : parenthèse fermante manquante.");
            return null;
        }


        String argsString = ligne.substring(debut, fin).trim();
        String[] args = argsString.split(",");
        if (nombreArgsAttendus != -1 && args.length != nombreArgsAttendus) {
            System.out.println("Erreur : nombre d'arguments incorrect pour '" + motCle + "' à la ligne " + numeroLigne);
            return null;
        }
        for (int i = 0; i < args.length; i++) {
            args[i] = args[i].trim();
            if (!args[i].matches("[A-Za-z0-9]+")) {
                System.out.println("Erreur : argument invalide '" + args[i] + "' pour '" + motCle + "' à la ligne " + numeroLigne + ". Les noms doivent être alphanumériques.");
                return null;
            }
        }
        return args;
    }
}
