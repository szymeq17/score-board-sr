package org.szymonrysz.service;

import org.szymonrysz.exception.GameNotFoundException;
import org.szymonrysz.exception.GameRulesViolationException;
import org.szymonrysz.model.Game;
import org.szymonrysz.model.Score;
import org.szymonrysz.model.Team;
import org.szymonrysz.repository.GameRepository;

import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class ScoreBoardServiceImpl implements ScoreBoardService {

    private final GameRepository gameRepository;
    private final Clock clock;

    public ScoreBoardServiceImpl(GameRepository gameRepository, Clock clock) {
        this.gameRepository = gameRepository;
        this.clock = clock;
    }

    @Override
    public Game startGame(Team homeTeam, Team awayTeam) {
        validateTeams(homeTeam, awayTeam);

        var game = Game.builder()
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .score(new Score(0, 0))
                .createdAt(Instant.now(clock))
                .build();

        return gameRepository.save(game);
    }

    @Override
    public void finishGame(UUID gameId) {
        gameRepository.findById(gameId).orElseThrow(() -> new GameNotFoundException(gameId));
        gameRepository.deleteById(gameId);
    }

    @Override
    public Game updateScore(UUID gameId, Score score) {
        validateScore(score);
        var game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException(gameId));
        game.setScore(score);

        return gameRepository.save(game);
    }

    @Override
    public List<Game> getSummary() {
        return gameRepository.findAll()
                .sorted(
                        Comparator.comparing(
                                        (Game game) -> game.getScore().homeTeamScore() + game.getScore().awayTeamScore()
                                ).reversed()
                                .thenComparing(Game::getCreatedAt)
                )
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
        if (homeTeam == null) {
            throw new GameRulesViolationException("Home team cannot be null.");
        }

        if (awayTeam == null) {
            throw new GameRulesViolationException("Away team cannot be null.");
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
