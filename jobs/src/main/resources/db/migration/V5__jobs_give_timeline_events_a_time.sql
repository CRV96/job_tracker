-- Timeline events get a time, not only a date: when the status was set, or when the interview took place.
ALTER TABLE application_events ADD COLUMN occurred_at TIMESTAMPTZ;

-- Existing events: the time they were recorded if that was on their date, otherwise the start of their date
UPDATE application_events
SET occurred_at = CASE WHEN created_at::date = event_date THEN created_at ELSE event_date::timestamptz END;

ALTER TABLE application_events ALTER COLUMN occurred_at SET NOT NULL;
ALTER TABLE application_events DROP COLUMN event_date;
