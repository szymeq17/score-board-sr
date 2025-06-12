package org.szymonrysz.model.dto;

import org.szymonrysz.model.Score;
import org.szymonrysz.model.Team;

import java.time.Instant;
import java.util.UUID;

public record GameDto(
        UUID id,
        Team homeTeam,
        Team awayTeam,
        Score score,
        Instant createdAt) {
}
