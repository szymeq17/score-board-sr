package org.szymonrysz.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.szymonrysz.model.Game;
import org.szymonrysz.model.Score;
import org.szymonrysz.model.Team;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InMemoryGameRepositoryTest {

    @Mock
    private HashMap<UUID, Game> gamesMap;

    @InjectMocks
    private InMemoryGameRepository sut;

    @Test
    void shouldSaveAGameWhenItDoesNotExist() {
        //given
        var gameToSave = mockGame();
        gameToSave.setId(null);

        //when
        var result = sut.save(gameToSave);

        //then
        verify(gamesMap).put(gameToSave.getId(), gameToSave);
        verifyNoMoreInteractions(gamesMap);
        assertThat(result).isEqualTo(gameToSave);
        assertThat(result.getId()).isNotNull();
    }

    @Test
    void shouldSaveAGameWhenItExists() {
        //given
        var gameToSave = mockGame();

        //when
        var result = sut.save(gameToSave);

        //then
        verify(gamesMap).put(gameToSave.getId(), gameToSave);
        verifyNoMoreInteractions(gamesMap);
        assertThat(result).isEqualTo(gameToSave);
    }

    @Test
    void shouldFindGameById() {
        //given
        var game = mockGame();
        var gameId = game.getId();
        when(gamesMap.get(gameId)).thenReturn(game);

        //when
        var result = sut.findById(gameId);

        //then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(game);
    }

    @Test
    void shouldReturnEmptyOptionalWhenGameWithGivenIdDoesNotExist() {
        //given
        var gameId = UUID.randomUUID();
        when(gamesMap.get(gameId)).thenReturn(null);

        //when
        var result = sut.findById(gameId);

        //then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldDeleteGameById() {
        //given
        var gameId = UUID.randomUUID();

        //when
        sut.deleteById(gameId);

        //then
        verify(gamesMap).remove(gameId);
        verifyNoMoreInteractions(gamesMap);
    }

    @Test
    void shouldFindAllGames() {
        //given
        var savedGames = List.of(mockGame(), mockGame());
        when(gamesMap.values()).thenReturn(savedGames);

        //when
        var result = sut.findAll();

        //then
        var games = result.collect(Collectors.toSet());
        assertThat(games).containsAll(savedGames);
    }

    private static Game mockGame() {
        return Game.builder()
                .id(UUID.randomUUID())
                .score(new Score(0, 0))
                .homeTeam(new Team("Poland"))
                .awayTeam(new Team("Germany"))
                .createdAt(Instant.MIN)
                .build();
    }

    @Test
    void shouldReturnTrueIfGameWithGivenTeamNameExists() {
        //given
        var savedGame = mockGame();
        when(gamesMap.values()).thenReturn(List.of(savedGame));

        //when
        var result = sut.existsByTeamName("Poland");

        //then
        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnTrueIfGameWithGivenTeamNameDoesNotExist() {
        //given
        var savedGame = mockGame();
        when(gamesMap.values()).thenReturn(List.of(savedGame));

        //when
        var result = sut.existsByTeamName("France");

        //then
        assertThat(result).isFalse();
    }
}