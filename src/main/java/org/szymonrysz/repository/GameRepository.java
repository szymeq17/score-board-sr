package org.szymonrysz.repository;

import org.szymonrysz.model.Game;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public interface GameRepository {

    Game save(Game game);
    Optional<Game> findById(UUID id);
    void deleteById(UUID id);
    Stream<Game> findAll();
    boolean existsByTeamName(String teamName);
}
