package com.example.proj3.service;

import org.springframework.beans.factory.annotation.Autowired;
import com.example.proj3.model.User;
import com.example.proj3.model.UserGameList;
import com.example.proj3.model.VideoGame;
import com.example.proj3.repository.UserGameListRepo;
import com.example.proj3.repository.VideoGameRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service

public class UserGameListService {

    private final UserGameListRepo listRepo;
    private final VideoGameRepo gameRepo;

    public UserGameListService(UserGameListRepo listRepo, VideoGameRepo gameRepo) {
        this.listRepo = listRepo;
        this.gameRepo = gameRepo;
    }

    // Create a list
    public UserGameList createList(String name, User user) {
        UserGameList list = new UserGameList(name,user);
        return listRepo.save(list);
    }

    // Add a game to a list
    public void addGameToList(Long listId, Long gameId) {
        Optional<UserGameList> listOpt = listRepo.findById(listId);
        if (listOpt.isEmpty()) {
            throw new RuntimeException("List not found");
        }

        Optional<VideoGame> gameOpt = gameRepo.findById(gameId);
        if (gameOpt.isEmpty()) {
            throw new RuntimeException("Game not found");
        }

        UserGameList list = listOpt.get();
        list.getVideoGames().add(gameOpt.get());
        listRepo.save(list);
    }

    // Get all lists for a user
    public List<UserGameList> getUserLists(User user) {

        return listRepo.findByUser(user);

    }

    // delete a game from a list
    public void removeGameFromList(Long listId, Long gameId) {
        Optional<UserGameList> listOpt = listRepo.findById(listId);
        if (listOpt.isEmpty()) {
            throw new RuntimeException("List not found");
        }

        UserGameList list = listOpt.get();
        list.getVideoGames().removeIf(game -> game.getId().equals(gameId));
        listRepo.save(list);
    }

    // Delete a list if owned by the user, not currently working will fix
    public void deleteList(Long listId, User user) {
        Optional<UserGameList> listOpt = listRepo.findById(listId);
        if (listOpt.isEmpty()) {
            throw new RuntimeException("List not found");
        }

        UserGameList list = listOpt.get();

        if (!list.getUser().equals(user)) {
            throw new RuntimeException("You can only delete your own lists");
        }

        listRepo.delete(list);
    }

}

