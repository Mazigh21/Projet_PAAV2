package Modele;

import java.util.*;

public class GestionnaireConflits {
    private Set<String> conflits = new HashSet<>();

    public void ajouterConflit(char colon1, char colon2) {
        String relation = colon1 + "-" + colon2;
        conflits.add(relation);
    }

    public boolean estEnConflit(char colon1, char colon2) {
        String relation1 = colon1 + "-" + colon2;
        String relation2 = colon2 + "-" + colon1;
        return conflits.contains(relation1) || conflits.contains(relation2);
    }

    public Set<String> getConflits() {
        return conflits;
    }
}
