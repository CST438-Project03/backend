package com.example.proj3.service;

import com.example.proj3.model.VideoGame;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class RawgApiService {

    private static final String RAWG_API_KEY = "c3101d469d92487fa6cc0d34454b74d7";
    private static final String RAWG_API_URL = "https://api.rawg.io/api/games";

    public List<VideoGame> fetchGamesFromRawg(int page, int pageSize) {
        RestTemplate restTemplate = new RestTemplate();
        String url = RAWG_API_URL + "?key=" + RAWG_API_KEY + "&page=" + page + "&page_size=" + pageSize;

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");

        List<VideoGame> games = new ArrayList<>();
        for (Map<String, Object> result : results) {
            VideoGame game = new VideoGame();
            game.setRawgId(result.get("id").toString());
            game.setTitle(result.get("name").toString());
            game.setImageUrl(result.get("background_image").toString());
            games.add(game);
        }

        return games;
    }

    public List<VideoGame> searchGames(String query) {
        RestTemplate restTemplate = new RestTemplate();
        String url = RAWG_API_URL + "?key=" + RAWG_API_KEY + "&search=" + query;

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");

        List<VideoGame> games = new ArrayList<>();
        for (Map<String, Object> result : results) {
            VideoGame game = new VideoGame();
            game.setRawgId(result.get("id").toString());
            game.setTitle(result.get("name").toString());
            game.setImageUrl(result.get("background_image") != null ? result.get("background_image").toString() : null);
            games.add(game);
        }

        return games;
    }

    public int getTotalGames() {
        RestTemplate restTemplate = new RestTemplate();
        String url = RAWG_API_URL + "?key=" + RAWG_API_KEY;

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        return (int) response.get("count");
    }
}
