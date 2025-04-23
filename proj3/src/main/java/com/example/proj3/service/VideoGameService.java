package com.example.proj3.service;

import org.springframework.beans.factory.annotation.Autowired;
import com.example.proj3.model.VideoGame;
import com.example.proj3.repository.VideoGameRepo;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import java.util.stream.Collectors;
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

    public VideoGame fetchAndSaveFromRawg(Long rawgId) {
        String apiKey = "c3101d469d92487fa6cc0d34454b74d7"; // <- Replace with your actual RAWG key
        String url = "https://api.rawg.io/api/games/" + rawgId + "?key=" + apiKey;

        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> data = response.getBody();

            if (data == null || data.get("name") == null) {
                throw new RuntimeException("Invalid game data from RAWG API.");
            }

            VideoGame game = new VideoGame();
            game.setRawgId(rawgId.toString());
            game.setTitle((String) data.get("name"));
            game.setImageUrl((String) data.get("background_image"));

            // Convert genres to comma-separated string
            var genres = (List<Map<String, Object>>) data.get("genres");
            String genreStr = genres.stream()
                    .map(g -> (String) g.get("name"))
                    .collect(Collectors.joining(", "));
            game.setGenre(genreStr);

            return videoGameRepo.save(game);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch game from RAWG: " + e.getMessage());
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
