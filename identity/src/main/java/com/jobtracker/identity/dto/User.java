package com.jobtracker.identity.dto;

import java.time.Instant;

/**
 * A profile. In the self-hosted version it has no password: anyone with access to the machine
 * already has full access to the database, so there is no real permission boundary to enforce.
 */
public record User(long id, String name, Instant createdAt) {
}
