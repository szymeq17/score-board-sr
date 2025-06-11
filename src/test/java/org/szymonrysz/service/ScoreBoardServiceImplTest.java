package org.szymonrysz.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.szymonrysz.exception.GameNotFoundException;
import org.szymonrysz.exception.GameRulesViolationException;
import org.szymonrysz.model.Game;
import org.szymonrysz.model.Score;
import org.szymonrysz.model.Team;
import org.szymonrysz.repository.GameRepository;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScoreBoardServiceImplTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private Clock clock;

    @InjectMocks
    private ScoreBoardServiceImpl sut;

    @Captor
    private ArgumentCaptor<Game> gameCaptor;

    @Test
    void shouldStartAGame() {
        //given
        var homeTeam = new Team("Poland");
        var awayTeam = new Team("Germany");
        var gameFromRepository = Game.builder().build();
        when(clock.instant()).thenReturn(Instant.MIN);
        when(gameRepository.existsByTeamName("Poland")).thenReturn(false);
        when(gameRepository.existsByTeamName("Germany")).thenReturn(false);
        when(gameRepository.save(any(Game.class))).thenReturn(gameFromRepository);

        //when
        var result = sut.startGame(homeTeam, awayTeam);

        //then
        verify(gameRepository).save(gameCaptor.capture());
        var savedGame = gameCaptor.getValue();
        assertThat(savedGame.getHomeTeam()).isEqualTo(homeTeam);
        assertThat(savedGame.getAwayTeam()).isEqualTo(awayTeam);
        assertThat(savedGame.getScore().homeTeamScore()).isEqualTo(0);
        assertThat(savedGame.getScore().awayTeamScore()).isEqualTo(0);
        assertThat(savedGame.getCreatedAt()).isEqualTo(Instant.MIN);
        assertThat(result).isEqualTo(gameFromRepository);
    }

    @ParameterizedTest
    @MethodSource("provideIncorrectTeams")
    void shouldThrowExceptionWhileStartingAGameWhenIncorrectTeams(Team homeTeam, Team awayTeam) {
        assertThrows(GameRulesViolationException.class, () -> sut.startGame(homeTeam, awayTeam));
    }

    @Test
    void shouldThrowExceptionWhenHomeTeamAlreadyPlaying() {
        //given
        when(gameRepository.existsByTeamName("France")).thenReturn(true);

        //when
        //then
        assertThrows(
                GameRulesViolationException.class,
                () -> sut.startGame(new Team("France"), new Team("Poland"))
        );
    }

    @Test
    void shouldThrowExceptionWhenAwayTeamAlreadyPlaying() {
        //given
        when(gameRepository.existsByTeamName("France")).thenReturn(true);
        when(gameRepository.existsByTeamName("Poland")).thenReturn(false);

        //when
        //then
        assertThrows(
                GameRulesViolationException.class,
                () -> sut.startGame(new Team("Poland"), new Team("France"))
        );
    }

    @Test
    void shouldFinishAGame() {
        //given
        var gameId = UUID.randomUUID();
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(Game.builder().build()));

        //when
        sut.finishGame(gameId);

        //then
        verify(gameRepository).deleteById(gameId);
    }

    @Test
    void shouldThrowExceptionWhenFinishigANonExistingGame() {
        //given
        var gameId = UUID.randomUUID();
        when(gameRepository.findById(gameId)).thenReturn(Optional.empty());

        //when
        //then
        assertThrows(GameNotFoundException.class, () -> sut.finishGame(gameId));
    }

    @Test
    void shouldUpdateScore() {
        //given
        var gameId = UUID.randomUUID();
        var gameToUpdate = Game.builder()
                .id(gameId)
                .score(new Score(0, 0))
                .build();
        var gameFromRepository = Game.builder().build();
        var newScore = new Score(1, 0);
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(gameToUpdate));
        when(gameRepository.save(gameToUpdate)).thenReturn(gameFromRepository);

        //when
        var result = sut.updateScore(gameId, newScore);

        //then
        verify(gameRepository).save(gameCaptor.capture());
        var updatedGame = gameCaptor.getValue();
        var updatedGameScore = updatedGame.getScore();
        assertThat(updatedGameScore.homeTeamScore()).isEqualTo(1);
        assertThat(updatedGameScore.awayTeamScore()).isEqualTo(0);
        assertThat(result).isEqualTo(gameFromRepository);
    }

    @Test
    void shouldThrowExceptionWhileUpdatingScoreWhenGameDoesNotExist() {
        //given
        var gameId = UUID.randomUUID();
        var newScore = new Score(1, 0);
        when(gameRepository.findById(gameId)).thenReturn(Optional.empty());

        //when
        //then
        assertThrows(GameNotFoundException.class, () -> sut.updateScore(gameId, newScore));
    }

    @Test
    void shouldReturnGamesInSummaryInCorrectOrder() {
        //given
        var game1 = Game.builder()
                .score(new Score(1, 1))
                .createdAt(Instant.MIN)
                .build();
        var game2 = Game.builder()
                .score(new Score(1, 2))
                .createdAt(Instant.MAX)
                .build();
        var game3 = Game.builder()
                .score(new Score(1, 2))
                .createdAt(Instant.MIN)
                .build();
        var game4 = Game.builder()
                .score(new Score(3, 3))
                .createdAt(Instant.MAX)
                .build();
        when(gameRepository.findAll()).thenReturn(Stream.of(game1, game2, game3, game4));

        //when
        var result = sut.getSummary();

        //then
        assertThat(result).containsExactly(game4, game3, game2, game1);
    }

    private static Stream<Arguments> provideIncorrectTeams() {
        return Stream.of(
                Arguments.of(new Team("Poland"), null),
                Arguments.of(null, new Team("Poland")),
                Arguments.of(new Team("Poland"), new Team("Poland")),
                Arguments.of(new Team(""), new Team("Poland")),
                Arguments.of(new Team("Poland"), new Team(""))
        );
    }
}