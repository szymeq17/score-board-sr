package org.szymonrysz.service;

import org.szymonrysz.model.Score;
import org.szymonrysz.model.Team;
import org.szymonrysz.model.dto.GameDto;

import java.util.List;
import java.util.UUID;

public interface ScoreBoardService {
    GameDto startGame(Team homeTeam, Team awayTeam);

    void finishGame(UUID gameId);

    GameDto updateScore(UUID gameId, Score score);

    List<GameDto> getSummary();
}
