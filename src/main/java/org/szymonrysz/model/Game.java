package org.szymonrysz.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
public class Game {
    private UUID id;
    private Team homeTeam;
    private Team awayTeam;
    private Score score;
    private Instant createdAt;
}

