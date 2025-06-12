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
import org.szymonrysz.converter.GameConverter;
import org.szymonrysz.exception.GameNotFoundException;
import org.szymonrysz.exception.GameRulesViolationException;
import org.szymonrysz.model.Game;
import org.szymonrysz.model.Score;
import org.szymonrysz.model.Team;
import org.szymonrysz.model.dto.GameDto;
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

    @Mock
    private GameConverter gameConverter;

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
        var gameDto = mockGameDto();
        when(clock.instant()).thenReturn(Instant.MIN);
        when(gameRepository.existsByTeamName("Poland")).thenReturn(false);
        when(gameRepository.existsByTeamName("Germany")).thenReturn(false);
        when(gameRepository.save(any(Game.class))).thenReturn(gameFromRepository);
        when(gameConverter.toDto(gameFromRepository)).thenReturn(gameDto);

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
        assertThat(result).isEqualTo(gameDto);
    }

    @ParameterizedTest
    @MethodSource("provideIncorrectTeams")
    void shouldThrowExceptionWhileStartingAGameWhenIncorrectTeams(Team homeTeam, Team awayTeam) {
        //given
        //when
        //then
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
    void shouldThrowExceptionWhenFinishingANonExistingGame() {
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
        var gameDto = mockGameDto();
        var newScore = new Score(1, 0);
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(gameToUpdate));
        when(gameRepository.save(gameToUpdate)).thenReturn(gameFromRepository);
        when(gameConverter.toDto(gameFromRepository)).thenReturn(gameDto);

        //when
        var result = sut.updateScore(gameId, newScore);

        //then
        verify(gameRepository).save(gameCaptor.capture());
        var updatedGame = gameCaptor.getValue();
        var updatedGameScore = updatedGame.getScore();
        assertThat(updatedGameScore.homeTeamScore()).isEqualTo(1);
        assertThat(updatedGameScore.awayTeamScore()).isEqualTo(0);
        assertThat(result).isEqualTo(gameDto);
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

    @ParameterizedTest
    @MethodSource("provideIncorrectScores")
    void shouldThrowExceptionWhileUpdatingScoreWhenScoreIsIncorrect(Score score) {
        //given
        var gameId = UUID.randomUUID();

        //when
        //then
        assertThrows(GameRulesViolationException.class, () -> sut.updateScore(gameId, score));
    }

    @Test
    void shouldReturnGamesInSummaryInCorrectOrder() {
        //given
        var game1 = Game.builder()
                .score(new Score(1, 1))
                .createdAt(Instant.MIN)
                .build();
        var gameDto1 = mockGameDto();
        var game2 = Game.builder()
                .score(new Score(1, 2))
                .createdAt(Instant.MAX)
                .build();
        var gameDto2 = mockGameDto();
        var game3 = Game.builder()
                .score(new Score(1, 2))
                .createdAt(Instant.MIN)
                .build();
        var gameDto3 = mockGameDto();
        var game4 = Game.builder()
                .score(new Score(3, 3))
                .createdAt(Instant.MAX)
                .build();
        var gameDto4 = mockGameDto();
        when(gameRepository.findAll()).thenReturn(Stream.of(game1, game2, game3, game4));
        when(gameConverter.toDto(game1)).thenReturn(gameDto1);
        when(gameConverter.toDto(game2)).thenReturn(gameDto2);
        when(gameConverter.toDto(game3)).thenReturn(gameDto3);
        when(gameConverter.toDto(game4)).thenReturn(gameDto4);

        //when
        var result = sut.getSummary();

        //then
        assertThat(result).containsExactly(gameDto4, gameDto3, gameDto2, gameDto1);
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

    private static Stream<Arguments> provideIncorrectScores() {
        return Stream.of(
                Arguments.of(new Score(-2, 0)),
                Arguments.of(new Score(0, -1)),
                Arguments.of(new Score(-5, -3))
        );
    }

    private static GameDto mockGameDto() {
        return new GameDto(
                UUID.randomUUID(),
                new Team("Poland"),
                new Team("Germany"),
                new Score(1, 0),
                Instant.MIN
        );
    }
}