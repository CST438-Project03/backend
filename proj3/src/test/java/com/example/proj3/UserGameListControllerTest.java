package com.example.proj3.controller;

import com.example.proj3.model.User;
import com.example.proj3.model.UserGameList;
import com.example.proj3.service.UserGameListService;
import com.example.proj3.service.VideoGameService;
import com.example.proj3.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserGameListControllerTest {

    private UserGameListService listService;
    private VideoGameService videoGameService;
    private UserRepository userRepository;
    private UserGameListController controller;

    private final String TEST_USERNAME = "testuser";

    @BeforeEach
    void setUp() {
        listService = mock(UserGameListService.class);
        videoGameService = mock(VideoGameService.class);
        userRepository = mock(UserRepository.class);
        controller = new UserGameListController(listService, videoGameService);
        controller.setUserRepository(userRepository); // use setter instead of accessing private field
    }

    @Test
    void testCreateListSuccess() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(TEST_USERNAME);

        User user = new User();
        user.setUsername(TEST_USERNAME);
        UserGameList mockList = new UserGameList("Favorites", user);

        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(user));
        when(listService.createList("Favorites", user)).thenReturn(mockList);

        ResponseEntity<?> response = controller.createList("Favorites", userDetails);

        assertEquals(201, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("List created successfully", body.get("message"));
    }

    @Test
    void testCreateListEmptyName() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(TEST_USERNAME);

        ResponseEntity<?> response = controller.createList("", userDetails);
        assertEquals(400, response.getStatusCodeValue());
    }

    @Test
    void testGetUserListsSuccess() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(TEST_USERNAME);

        User user = new User();
        user.setUsername(TEST_USERNAME);
        List<UserGameList> mockLists = List.of(new UserGameList("My List", user));

        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(user));
        when(listService.getUserLists(user)).thenReturn(mockLists);

        ResponseEntity<?> response = controller.getUserLists(userDetails);
        assertEquals(200, response.getStatusCodeValue());

        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("Lists retrieved successfully", body.get("message"));
        assertEquals(1, ((List<?>) body.get("lists")).size());
    }

    @Test
    void testAddGameToListSuccess() {
        ResponseEntity<?> response = controller.addGameToList(1L, 100L);
        verify(listService, times(1)).addGameToList(1L, 100L);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testAddGameToListListNotFound() {
        doThrow(new RuntimeException("List not found")).when(listService).addGameToList(1L, 100L);

        ResponseEntity<?> response = controller.addGameToList(1L, 100L);
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void testRemoveGameFromListSuccess() {
        ResponseEntity<?> response = controller.removeGameFromList(1L, 100L);
        verify(listService).removeGameFromList(1L, 100L);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testDeleteListUnauthorizedUser() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(TEST_USERNAME);

        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.deleteList(1L, userDetails);
        assertEquals(401, response.getStatusCodeValue());
    }

    @Test
    void testDeleteListSuccess() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(TEST_USERNAME);

        User user = new User();
        user.setUsername(TEST_USERNAME);
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(user));

        ResponseEntity<?> response = controller.deleteList(1L, userDetails);
        verify(listService).deleteList(1L, user);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testDeleteListForbidden() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(TEST_USERNAME);

        User user = new User();
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(user));

        doThrow(new RuntimeException("You can only delete your own lists")).when(listService).deleteList(any(), any());

        ResponseEntity<?> response = controller.deleteList(1L, userDetails);
        assertEquals(403, response.getStatusCodeValue());
    }
}
