-- Profile names are unique ignoring case: the rule UserService checks, now also enforced by the database, so two
-- requests arriving together can't create both "Robert" and "robert".
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_name_key;
CREATE UNIQUE INDEX users_name_lower_key ON users (lower(name));
