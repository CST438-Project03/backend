package com.example.proj3.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.proj3.model.VideoGame;
@Repository
public interface VideoGameRepo extends JpaRepository<VideoGame, Long> {
    Optional<VideoGame> findByRawgId(String rawgId); // helpful if syncing with RAWG
}
