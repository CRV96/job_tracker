/**
 * Capture: the REST endpoint the browser extension posts captured job postings to.
 * A thin adapter that turns a capture into calls on the jobs and identity modules; it owns no tables
 * and holds no domain logic. No other module depends on it.
 */
package com.jobtracker.capture;
