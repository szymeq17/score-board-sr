package org.szymonrysz.converter;

import org.szymonrysz.model.Game;
import org.szymonrysz.model.dto.GameDto;

public class GameConverter {

    public GameDto toDto(Game game) {
        return new GameDto(
                game.getId(),
                game.getHomeTeam(),
                game.getAwayTeam(),
                game.getScore(),
                game.getCreatedAt()
        );
    }
}
