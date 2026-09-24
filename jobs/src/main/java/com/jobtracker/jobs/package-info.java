/**
 * Jobs: the core domain. Job postings the user saved as favorites or applied to, their status,
 * and the event timeline of each application. Owns the {@code applications} and {@code application_events} tables.
 * <p>
 * Public API: the {@code dto}, {@code enums} and {@code service} packages, marked with {@code @NamedInterface}.
 * {@code entity}, {@code repository} and {@code service.impl} are internal to the module.
 */
package com.jobtracker.jobs;
