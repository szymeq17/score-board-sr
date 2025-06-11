package org.szymonrysz.service;

import org.szymonrysz.model.Game;
import org.szymonrysz.model.Score;
import org.szymonrysz.model.Team;

import java.util.List;
import java.util.UUID;

public interface ScoreBoardService {
    Game startGame(Team homeTeam, Team awayTeam);

    void finishGame(UUID gameId);

    Game updateScore(UUID gameId, Score score);

    List<Game> getSummary();
}
