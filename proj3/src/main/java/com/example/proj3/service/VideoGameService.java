package com.example.proj3.service;

import org.springframework.beans.factory.annotation.Autowired;
import com.example.proj3.model.VideoGame;
import com.example.proj3.repository.VideoGameRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VideoGameService {

    private final VideoGameRepo videoGameRepo;

    public VideoGameService(VideoGameRepo videoGameRepo) {
        this.videoGameRepo = videoGameRepo;
    }

    // Finds a game by RAWG ID, or saves it if not found
    public VideoGame getOrCreateGame(VideoGame gameFromRawg) {
        Optional<VideoGame> existingGame = videoGameRepo.findByRawgId(gameFromRawg.getRawgId());

        if (existingGame.isPresent()) {
            return existingGame.get();
        } else {
            return videoGameRepo.save(gameFromRawg);
        }
    }

    // Find by ID
    public Optional<VideoGame> findById(Long id) {
        return videoGameRepo.findById(id);
    }

    // Get all games
    public List<VideoGame> getAllGames() {
        return videoGameRepo.findAll();
    }
}
