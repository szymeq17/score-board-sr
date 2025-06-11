package org.szymonrysz.repository;

import org.szymonrysz.model.Game;
import org.szymonrysz.model.Team;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public class InMemoryGameRepository implements GameRepository{

    private final HashMap<UUID, Game> gamesMap;

    public InMemoryGameRepository(HashMap<UUID, Game> gamesMap) {
        this.gamesMap = gamesMap;
    }

    @Override
    public Game save(Game game) {
        if (game.getId() == null) {
            var uuid = UUID.randomUUID();
            game.setId(uuid);
        }
        gamesMap.put(game.getId(), game);

        return game;
    }

    @Override
    public Optional<Game> findById(UUID id) {
        return Optional.ofNullable(gamesMap.get(id));
    }

    @Override
    public void deleteById(UUID id) {
        gamesMap.remove(id);
    }

    @Override
    public Stream<Game> findAll() {
        return gamesMap.values().stream();
    }

    @Override
    public boolean existsByTeamName(String teamName) {
        return gamesMap.values().stream()
                .flatMap(game -> Stream.of(game.getHomeTeam(), game.getAwayTeam()))
                .map(Team::name)
                .anyMatch(teamName::equals);
    }
}
