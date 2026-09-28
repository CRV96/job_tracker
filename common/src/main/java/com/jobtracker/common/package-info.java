/**
 * Common: code every module shares, currently error codes, base exceptions and the application logger.
 * Owns no tables, has no Spring beans and depends on no other module.
 * <p>
 * Public API: the {@code exception} and {@code logging} packages, marked with {@code @NamedInterface}.
 * Business logic never goes here; it belongs in a domain module.
 */
package com.jobtracker.common;
