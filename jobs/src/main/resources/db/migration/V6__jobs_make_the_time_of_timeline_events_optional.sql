-- A timeline event has a date, and a time of day only when it's known: some events are remembered by their date
-- alone. Both are wall-clock values, read in the time zone the app runs in.
ALTER TABLE application_events ADD COLUMN event_date DATE, ADD COLUMN event_time TIME;

-- In the session's time zone. V5 gave events whose time wasn't known the start of their day, so that means no time.
UPDATE application_events
SET event_date = occurred_at::date,
    event_time = CASE WHEN occurred_at::time = TIME '00:00' THEN NULL ELSE occurred_at::time END;

ALTER TABLE application_events ALTER COLUMN event_date SET NOT NULL;
ALTER TABLE application_events DROP COLUMN occurred_at;
