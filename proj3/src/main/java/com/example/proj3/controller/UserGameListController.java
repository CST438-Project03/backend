package com.example.proj3.controller;

import com.example.proj3.model.User;
import com.example.proj3.model.UserGameList;
import com.example.proj3.model.VideoGame;
import com.example.proj3.service.UserGameListService;
import com.example.proj3.service.VideoGameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/lists")
public class UserGameListController {

    private final UserGameListService listService;
    private final VideoGameService videoGameService;

    @Autowired
    public UserGameListController(UserGameListService listService, VideoGameService videoGameService) {
        this.listService = listService;
        this.videoGameService = videoGameService;
    }
    //create lists
    @PostMapping("createList")
    public ResponseEntity<?> createList(
            @RequestParam String name,
            @AuthenticationPrincipal User user) {

        if (name == null || name.trim().isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "List name cannot be empty");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try {
            UserGameList newList = listService.createList(name, user);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "List created successfully");
            response.put("list", newList);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to create list: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    //gets all lists from user
    @GetMapping("/getUserLists")
    public ResponseEntity<?> getUserLists(@AuthenticationPrincipal User user) {
        try {
            List<UserGameList> lists = listService.getUserLists(user);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Lists retrieved successfully");
            response.put("lists", lists);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to retrieve lists: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    //adds game to list
    @PostMapping("addGame/{listId}/games/{gameId}")
    public ResponseEntity<?> addGameToList(
            @PathVariable Long listId,
            @PathVariable Long gameId) {

        try {

            listService.addGameToList(listId, gameId);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Game added to list successfully");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (RuntimeException e) {
            Map<String, String> response = new HashMap<>();

            // Check message to determine the appropriate status code
            if (e.getMessage().contains("List not found")) {
                response.put("message", "List not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            } else if (e.getMessage().contains("Game not found")) {
                response.put("message", "Game not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            } else {
                response.put("message", "Failed to add game to list: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        }
    }

    //removes game from list
    @DeleteMapping("removeGame/{listId}/games/{gameId}")
    public ResponseEntity<?> removeGameFromList(
            @PathVariable Long listId,
            @PathVariable Long gameId) {

        try {

            listService.removeGameFromList(listId, gameId);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Game removed from list successfully");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (RuntimeException e) {
            Map<String, String> response = new HashMap<>();

            // Check message to determine the appropriate status code
            if (e.getMessage().contains("List not found")) {
                response.put("message", "List not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            } else {
                response.put("message", "Failed to remove game from list: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        }
    }

    //deletes list
    @DeleteMapping("deleteList/{listId}")
    public ResponseEntity<?> deleteList(
            @PathVariable Long listId,
            @AuthenticationPrincipal User user) {

        try {

            listService.deleteList(listId, user);

            Map<String, String> response = new HashMap<>();
            response.put("message", "List deleted successfully");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (RuntimeException e) {
            Map<String, String> response = new HashMap<>();

            // Check message to determine the appropriate status
            if (e.getMessage().contains("List not found")) {
                response.put("message", "List not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            } else if (e.getMessage().contains("You can only delete")) {
                response.put("message", "You can only delete your own lists");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            } else {
                response.put("message", "Failed to delete list: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        }
    }
}