-- Applications added by hand don't always have a posting to link to, e.g. a referral or a recruiter's email.
ALTER TABLE applications ALTER COLUMN link DROP NOT NULL;
