package fr.diginamic.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "erreur")
public class Erreur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String message;

    // Constructors, Getters, Setters, equals, and hashCode methods
}
