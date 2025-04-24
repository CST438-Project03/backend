package com.example.proj3.service;

import com.example.proj3.model.User;
import com.example.proj3.model.UserGameList;
import com.example.proj3.model.VideoGame;
import com.example.proj3.repository.UserGameListRepo;
import com.example.proj3.repository.VideoGameRepo;
import com.example.proj3.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserGameListService {

    private final UserGameListRepo listRepo;
    private final VideoGameRepo gameRepo;
    private final UserRepository userRepository;
    private final VideoGameService videoGameService;

    @Autowired
    public UserGameListService(UserGameListRepo listRepo, VideoGameRepo gameRepo, UserRepository userRepository, VideoGameService videoGameService) {
        this.listRepo = listRepo;
        this.gameRepo = gameRepo;
        this.userRepository = userRepository;
        this.videoGameService = videoGameService;
    }

    // Creates a new game list for a given username
    public UserGameList createList(String name, User user) {
       // User user = userRepository.findByUsername(username)
         //       .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        UserGameList list = new UserGameList(name, user);
        return listRepo.save(list);
    }

    // Adds a game to a list; fetches from RAWG if not already in the DB
    public void addGameToList(Long listId, Long gameId) {
        UserGameList list = listRepo.findById(listId)
                .orElseThrow(() -> new RuntimeException("List not found"));

        VideoGame game = gameRepo.findById(gameId).orElse(null);

        // If not found, fetch from RAWG and save
        if (game == null) {
            game = videoGameService.fetchAndSaveFromRawg(gameId);
        }

        if (!list.getVideoGames().contains(game)) {
            list.getVideoGames().add(game);
            listRepo.save(list);
        }
    }

    // Retrieves all game lists for a user
    public List<UserGameList> getUserLists(User user) {
        return listRepo.findByUser(user);
    }

    // Removes game from a list
    public void removeGameFromList(Long listId, Long gameId) {
        Optional<UserGameList> listOpt = listRepo.findById(listId);
        if (listOpt.isEmpty()) {
            throw new RuntimeException("List not found");
        }

        UserGameList list = listOpt.get();
        list.getVideoGames().removeIf(game -> game.getId().equals(gameId));
        listRepo.save(list);
    }

    // Deletes a user's list
    public void deleteList(Long listId, User user) {
        Optional<UserGameList> listOpt = listRepo.findById(listId);
        if (listOpt.isEmpty()) {
            throw new RuntimeException("List not found");
        }

        UserGameList list = listOpt.get();
        System.out.println("Authenticated user ID: " + user.getId());
        System.out.println("List owner ID: " + list.getUser().getId());


        if (!list.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You can only delete your own lists");
        }

        listRepo.delete(list);
    }
}

