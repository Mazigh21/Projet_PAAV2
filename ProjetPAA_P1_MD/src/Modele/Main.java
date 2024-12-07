package Modele ;
/**
 * La classe principale qui initialise et démarre le programme de gestion de la colonie.
 */
public class Main {

    /**
     * Point d'entrée du programme.
     *
     * @param args arguments de ligne de commande (non utilisés).
     */
      public static void main(String[] args) {
        Colonie colonie = new Colonie();
        colonie.demarrer(args);
    }
}