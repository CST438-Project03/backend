package com.example.proj3.controller;

import com.example.proj3.model.VideoGame;
import com.example.proj3.service.VideoGameService;
import com.example.proj3.service.RawgApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/games")
public class VideoGameController {

    private final VideoGameService videoGameService;
    private final RawgApiService rawgApiService;

    @Autowired
    public VideoGameController(VideoGameService videoGameService, RawgApiService rawgApiService) {
        this.videoGameService = videoGameService;
        this.rawgApiService = rawgApiService;
    }

    //gets all games
    @GetMapping("/all")
    public ResponseEntity<?> getAllGames() {
        Map<String, Object> response = new HashMap<>();

        try {
            List<VideoGame> games = videoGameService.getAllGames();

            response.put("message", "Games retrieved successfully");
            response.put("games", games);
            response.put("count", games.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Failed to retrieve games: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    //get a game by its id
    @GetMapping("/getGameById/{id}")
    public ResponseEntity<?> getGameById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            System.out.println("Fetching game with ID: " + id); // Debugging log
            Optional<VideoGame> gameOpt = videoGameService.findById(id);

            if (gameOpt.isPresent()) {
                response.put("message", "Game retrieved successfully");
                response.put("game", gameOpt.get());
                return ResponseEntity.ok(response);
            } else {
                System.out.println("Game not found for ID: " + id); // Debugging log
                response.put("message", "Game not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            System.out.println("Error fetching game: " + e.getMessage()); // Debugging log
            response.put("message", "Failed to retrieve game: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    //creates or update game in db
    @PostMapping("/createOrUpdateGame")
    public ResponseEntity<?> createOrUpdateGame(@RequestBody VideoGame game) {
        Map<String, Object> response = new HashMap<>();

        if (game == null) {
            response.put("message", "Game data cannot be empty");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (game.getRawgId() == null) {
            response.put("message", "RAWG ID is required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try {
            VideoGame savedGame = videoGameService.getOrCreateGame(game);

            boolean isNewGame = game.getId() == null;
            String message = isNewGame ? "Game created successfully" : "Game updated successfully";

            response.put("message", message);
            response.put("game", savedGame);
            return ResponseEntity.status(isNewGame ? HttpStatus.CREATED : HttpStatus.OK).body(response);
        } catch (Exception e) {
            response.put("message", "Failed to save game: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/fetchFromRawg")
    public ResponseEntity<?> fetchGamesFromRawg(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "18") int pageSize
    ) {
        try {
            // Fetch games from RAWG API
            List<VideoGame> games = rawgApiService.fetchGamesFromRawg(page, pageSize);
            int totalGames = rawgApiService.getTotalGames(); // Total number of games
            int totalPages = (int) Math.ceil((double) totalGames / pageSize);

            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("games", games);
            response.put("totalPages", totalPages);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching games: " + e.getMessage());
        }
    }
}