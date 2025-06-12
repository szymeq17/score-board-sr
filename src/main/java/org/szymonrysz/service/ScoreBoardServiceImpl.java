package org.szymonrysz.service;

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
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class ScoreBoardServiceImpl implements ScoreBoardService {

    private final GameRepository gameRepository;
    private final Clock clock;
    private final GameConverter gameConverter;

    public ScoreBoardServiceImpl(GameRepository gameRepository, Clock clock, GameConverter gameConverter) {
        this.gameRepository = gameRepository;
        this.clock = clock;
        this.gameConverter = gameConverter;
    }

    @Override
    public GameDto startGame(Team homeTeam, Team awayTeam) {
        validateTeams(homeTeam, awayTeam);

        var game = Game.builder()
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .score(new Score(0, 0))
                .createdAt(Instant.now(clock))
                .build();

        var savedGame = gameRepository.save(game);
        return gameConverter.toDto(savedGame);
    }

    @Override
    public void finishGame(UUID gameId) {
        gameRepository.findById(gameId).orElseThrow(() -> new GameNotFoundException(gameId));
        gameRepository.deleteById(gameId);
    }

    @Override
    public GameDto updateScore(UUID gameId, Score score) {
        validateScore(score);
        var game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException(gameId));
        game.setScore(score);

        var savedGame = gameRepository.save(game);
        return gameConverter.toDto(savedGame);
    }

    @Override
    public List<GameDto> getSummary() {
        return gameRepository.findAll()
                .sorted(
                        Comparator.comparing(Game::getTotalScore)
                                .reversed()
                                .thenComparing(Game::getCreatedAt)
                )
                .map(gameConverter::toDto)
                .toList();
    }

    private void validateScore(Score score) {
        if (score.homeTeamScore() < 0) {
            throw new GameRulesViolationException("Home team score must be non-negative.");
        }

        if (score.awayTeamScore() < 0) {
            throw new GameRulesViolationException("Away team score must be non-negative.");
        }
    }

    private void validateTeams(Team homeTeam, Team awayTeam) {
        if (homeTeam == null || homeTeam.name().isBlank()) {
            throw new GameRulesViolationException("Home team cannot be null nor blank.");
        }

        if (awayTeam == null || awayTeam.name().isBlank()) {
            throw new GameRulesViolationException("Away team cannot be null nor blank.");
        }

        if (homeTeam.equals(awayTeam)) {
            throw new GameRulesViolationException("Team cannot play against itself.");
        }

        if (isTeamAlreadyPlaying(homeTeam) || isTeamAlreadyPlaying(awayTeam)) {
            throw new GameRulesViolationException("The team must be available (not currently playing)" +
                    " to start the game.");
        }
    }

    private boolean isTeamAlreadyPlaying(Team team) {
        return gameRepository.existsByTeamName(team.name());
    }
}
