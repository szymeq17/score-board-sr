package org.szymonrysz.converter;

import org.junit.jupiter.api.Test;
import org.szymonrysz.model.Game;
import org.szymonrysz.model.Score;
import org.szymonrysz.model.Team;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GameConverterTest {

    private GameConverter sut = new GameConverter();

    @Test
    void shouldConvertGameToDto() {
        //given
        var game = Game.builder()
                .id(UUID.randomUUID())
                .homeTeam(new Team("Poland"))
                .awayTeam(new Team("Germany"))
                .score(new Score(1, 0))
                .createdAt(Instant.MIN)
                .build();

        //when
        var result = sut.toDto(game);

        //then
        assertThat(result.id()).isEqualTo(game.getId());
        assertThat(result.homeTeam()).isEqualTo(game.getHomeTeam());
        assertThat(result.awayTeam()).isEqualTo(game.getAwayTeam());
        assertThat(result.score()).isEqualTo(game.getScore());
        assertThat(result.createdAt()).isEqualTo(game.getCreatedAt());
    }
}