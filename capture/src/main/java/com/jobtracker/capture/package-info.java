/**
 * Capture: the REST endpoint the browser extension posts captured job postings to.
 * A thin adapter that turns a capture into calls on the applications module; it owns no tables
 * and holds no domain logic. Depends on applications and identity.
 * <p>
 * Public API: the types in this package. Sub-packages are internal to the module.
 */
package com.jobtracker.capture;
