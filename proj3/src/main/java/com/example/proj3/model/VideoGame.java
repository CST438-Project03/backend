package com.example.proj3.model;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.JoinColumn;

@Entity
public class VideoGame {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // or RAWG id can be switched

    private String title;
    private String genre;
    private String imageUrl;
    private String rawgId;

    public VideoGame() {
        // Default constructor required by JPA
    }


    public VideoGame(Long id, String title, String genre, String imageUrl, String rawgId) {
        this.id = id;
        this.title = title;
        this.genre = genre;
        this.imageUrl = imageUrl;
        this.rawgId = rawgId;
    }

    //setters and getters, subject to change depending on rawg api format
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getRawgId() {
        return rawgId;
    }

    public void setRawgId(String rawgId) {
        this.rawgId = rawgId;
    }
}
