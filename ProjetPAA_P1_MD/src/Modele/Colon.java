package Modele;

import java.util.*;

public class Colon {
    private char nom;
    private List<Integer> preferences = new ArrayList<>();
    private Integer objetAffecte;

    public Colon(char nom) {
        this.nom = nom;
    }

    public char getNom() {
        return nom;
    }

    public List<Integer> getPreferences() {
        return preferences;
    }

    public void setPreferences(List<Integer> preferences) {
        this.preferences = preferences;
    }

    public Integer getObjetAffecte() {
        return objetAffecte;
    }

    public void setObjetAffecte(Integer objetAffecte) {
        this.objetAffecte = objetAffecte;
    }

    public boolean estJaloux(Map<Character, Colon> colons, Set<String> conflits) {
        if (objetAffecte == null || preferences.isEmpty()) return false;

        int rangObjet = preferences.indexOf(objetAffecte);
        for (String conflit : conflits) {
            if (conflit.contains(String.valueOf(nom))) {
                char autreColonNom = conflit.charAt(0) == nom ? conflit.charAt(2) : conflit.charAt(0);
                Colon autreColon = colons.get(autreColonNom);

                if (autreColon != null && autreColon.getObjetAffecte() != null) {
                    int rangAutreObjet = preferences.indexOf(autreColon.getObjetAffecte());
                    if (rangAutreObjet < rangObjet) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
