package fr.diginamic;

import fr.diginamic.entities.Categorie;
import fr.diginamic.entities.Ingredient;
import fr.diginamic.entities.Marque;
import fr.diginamic.entities.Produit;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class IntegrationOpenFoodFacts {

    private static final String CSV_FILE_PATH = "open-food-facts2.csv";

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("open_food_facts");
        EntityManager em = emf.createEntityManager();

        try {
            processCsvFile(em);
        } finally {
            em.close();
            emf.close();
        }
    }

    public static void processCsvFile(EntityManager em) {
        try (BufferedReader br = new BufferedReader(new FileReader(CSV_FILE_PATH))) {
            String line = br.readLine();

            em.getTransaction().begin();

            while ((line = br.readLine()) != null) {
                String[] fields = line.split("\\|");

                if (fields.length < 3) {
                    continue;
                }

                // Extractions des valeurs des colonnes nécessaires
                String nomCategorie = fields[0].trim();
                String nomMarque = fields[1].trim();
                String nomProduit = fields[2].trim();
                String ingredientsField = fields[4].trim();

                // Créations ou récupérations les entités Marque et Categorie
                Marque marque = findOrCreateMarque(em, nomMarque);
                Categorie categorie = findOrCreateCategorie(em, nomCategorie);

                // Verification si contenance de caractere inutile dans le string des ingredients
                if (ingredientsField.contains("*")){
                    ingredientsField = ingredientsField.replace("*", "");
                }
                if (ingredientsField.contains("_")){
                    ingredientsField = ingredientsField.replace("_", "");
                }
                if (ingredientsField.contains(".") && ingredientsField.substring(ingredientsField.lastIndexOf(".") + 1).isEmpty()){
                    ingredientsField = ingredientsField.substring(0, ingredientsField.lastIndexOf("."));
                }

                // Créer une liste d'ingrédients en évitant les doublons
                Set<Ingredient> ingredients = processIngredients(em, ingredientsField);

                // Créations l'entité Produit et l'associations à Marque et Categorie
                Produit produit = new Produit(marque, categorie);

                // Verification si contenance d'une virgule inutile à la fin
                if (nomProduit.contains(",") && nomProduit.substring(nomProduit.lastIndexOf(",") + 1).isEmpty()){
                    nomProduit = nomProduit.substring(0, nomProduit.lastIndexOf(","));
                }
                produit.setNom(nomProduit);
                produit.setIngredients(ingredients);

                em.persist(produit);
            }

            em.getTransaction().commit();
        } catch (IOException e) {
            e.printStackTrace();
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        }
    }

    // MARQUE
    private static Marque findOrCreateMarque(EntityManager em, String nomMarque) {
        TypedQuery<Marque> query = em.createQuery("SELECT DISTINCT m FROM Marque m WHERE m.nom = :nom", Marque.class);
        query.setParameter("nom", nomMarque);
        Marque marque = query.getResultStream().findFirst().orElse(null);

        if (marque == null) {
            marque = new Marque(nomMarque);
            em.persist(marque);
        }
        return marque;
    }

    // CATEGORIE
    private static Categorie findOrCreateCategorie(EntityManager em, String nomCategorie) {
        TypedQuery<Categorie> query = em.createQuery("SELECT DISTINCT c FROM Categorie c WHERE c.nom = :nom", Categorie.class);
        query.setParameter("nom", nomCategorie);
        Categorie categorie = query.getResultStream().findFirst().orElse(null);

        if (categorie == null) {
            categorie = new Categorie(nomCategorie);
            em.persist(categorie);
        }
        return categorie;
    }

    // INGREDIENT
    private static Set<Ingredient> processIngredients(EntityManager em, String ingredientsField) {
        Set<Ingredient> ingredients = new HashSet<>();

        // Décompose la chaîne en utilisant des séparateurs possibles
        String[] ingredientNames = ingredientsField.split("[,;]");

        for (String ingredientName : ingredientNames) {
            ingredientName = ingredientName.trim();
            if (!ingredientName.isEmpty()) {
                Ingredient ingredient = findOrCreateIngredient(em, ingredientName);
                ingredients.add(ingredient);
            }
        }
        return ingredients;
    }

    private static Ingredient findOrCreateIngredient(EntityManager em, String nomIngredient) {
        TypedQuery<Ingredient> query = em.createQuery("SELECT i FROM Ingredient i WHERE i.nom = :nom", Ingredient.class);
        query.setParameter("nom", nomIngredient);
        Ingredient ingredient = query.getResultStream().findFirst().orElse(null);

        if (ingredient == null) {
            ingredient = new Ingredient(nomIngredient);
            em.persist(ingredient);
        }
        return ingredient;
    }
}
