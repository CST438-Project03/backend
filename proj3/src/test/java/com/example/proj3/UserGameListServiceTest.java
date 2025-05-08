package com.example.proj3.service;

import com.example.proj3.model.User;
import com.example.proj3.model.UserGameList;
import com.example.proj3.model.VideoGame;
import com.example.proj3.repository.UserGameListRepo;
import com.example.proj3.repository.UserRepository;
import com.example.proj3.repository.VideoGameRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserGameListServiceTest {

    @Mock
    private UserGameListRepo listRepo;

    @Mock
    private VideoGameRepo gameRepo;

    @Mock
    private UserRepository userRepository;

    @Mock
    private VideoGameService videoGameService;

    @InjectMocks
    private UserGameListService userGameListService;

    private User testUser;
    private VideoGame testGame;
    private UserGameList testList;

    @BeforeEach
    void setUp() {
        // Initialize test objects
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");

        testGame = new VideoGame();
        testGame.setId(100L);
        testGame.setTitle("Test Game");

        testList = new UserGameList("Favorites", testUser);
        testList.setId(1L);
        testList.setVideoGames(new ArrayList<>());
    }

    @Test
    void createList_Success() {
        // Arrange
        when(listRepo.save(any(UserGameList.class))).thenReturn(testList);

        // Act
        UserGameList created = userGameListService.createList("Favorites", testUser);

        // Assert
        assertNotNull(created);
        assertEquals("Favorites", created.getName());
        assertEquals(testUser, created.getUser());
        verify(listRepo).save(any(UserGameList.class));
    }

    @Test
    void getUserLists_Success() {
        // Arrange
        List<UserGameList> lists = new ArrayList<>();
        lists.add(testList);

        when(listRepo.findByUser(testUser)).thenReturn(lists);

        // Act
        List<UserGameList> result = userGameListService.getUserLists(testUser);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Favorites", result.get(0).getName());
        verify(listRepo).findByUser(testUser);
    }

    @Test
    void addGameToList_GameExists() {
        // Arrange
        when(listRepo.findById(1L)).thenReturn(Optional.of(testList));
        when(gameRepo.findById(100L)).thenReturn(Optional.of(testGame));
        when(listRepo.save(any(UserGameList.class))).thenReturn(testList);

        // Act
        userGameListService.addGameToList(1L, 100L);

        // Assert
        verify(listRepo).findById(1L);
        verify(gameRepo).findById(100L);
        verify(videoGameService, never()).fetchAndSaveFromRawg(anyLong());
        verify(listRepo).save(testList);
    }

    @Test
    void addGameToList_GameDoesNotExist() {
        // Arrange
        when(listRepo.findById(1L)).thenReturn(Optional.of(testList));
        when(gameRepo.findById(100L)).thenReturn(Optional.empty());
        when(videoGameService.fetchAndSaveFromRawg(100L)).thenReturn(testGame);
        when(listRepo.save(any(UserGameList.class))).thenReturn(testList);

        // Act
        userGameListService.addGameToList(1L, 100L);

        // Assert
        verify(listRepo).findById(1L);
        verify(gameRepo).findById(100L);
        verify(videoGameService).fetchAndSaveFromRawg(100L);
        verify(listRepo).save(testList);
    }

    @Test
    void addGameToList_ListNotFound() {
        // Arrange
        when(listRepo.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            userGameListService.addGameToList(999L, 100L);
        });

        assertEquals("List not found", exception.getMessage());
        verify(listRepo).findById(999L);
        verify(gameRepo, never()).findById(anyLong());
        verify(videoGameService, never()).fetchAndSaveFromRawg(anyLong());
        verify(listRepo, never()).save(any());
    }

    @Test
    void removeGameFromList_Success() {
        // Arrange
        List<VideoGame> games = new ArrayList<>();
        games.add(testGame);
        testList.setVideoGames(games);

        when(listRepo.findById(1L)).thenReturn(Optional.of(testList));
        when(listRepo.save(any(UserGameList.class))).thenReturn(testList);

        // Act
        userGameListService.removeGameFromList(1L, 100L);

        // Assert
        verify(listRepo).findById(1L);
        verify(listRepo).save(testList);
        assertTrue(testList.getVideoGames().isEmpty());
    }

    @Test
    void removeGameFromList_ListNotFound() {
        // Arrange
        when(listRepo.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            userGameListService.removeGameFromList(999L, 100L);
        });

        assertEquals("List not found", exception.getMessage());
        verify(listRepo).findById(999L);
        verify(listRepo, never()).save(any());
    }

    @Test
    void deleteList_Success() {
        // Arrange
        when(listRepo.findById(1L)).thenReturn(Optional.of(testList));
        doNothing().when(listRepo).delete(testList);

        // Act
        userGameListService.deleteList(1L, testUser);

        // Assert
        verify(listRepo).findById(1L);
        verify(listRepo).delete(testList);
    }

    @Test
    void deleteList_ListNotFound() {
        // Arrange
        when(listRepo.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            userGameListService.deleteList(999L, testUser);
        });

        assertEquals("List not found", exception.getMessage());
        verify(listRepo).findById(999L);
        verify(listRepo, never()).delete(any());
    }

    @Test
    void deleteList_NotOwner() {
        // Arrange
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("otherUser");

        when(listRepo.findById(1L)).thenReturn(Optional.of(testList));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            userGameListService.deleteList(1L, otherUser);
        });

        assertEquals("You can only delete your own lists", exception.getMessage());
        verify(listRepo).findById(1L);
        verify(listRepo, never()).delete(any());
    }
}
