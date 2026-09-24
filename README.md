# Job Tracker Application

## Running locally

Requires JDK 25+ and Docker. The app starts its own Postgres container from [app/compose.yaml](app/compose.yaml).

```bash
./mvnw -pl app -am spring-boot:run
```

Build and run all tests (tests use Testcontainers, so Docker must be running):

```bash
./mvnw verify
```

## Project structure

A modular monolith: one Spring Boot application split into Maven modules by business area.

| Module | Responsibility | Depends on |
|---|---|---|
| `identity` | Local profiles (`users` table) | none |
| `applications` | Job applications and their event timeline | `identity` |
| `capture` | REST endpoint for the browser extension; no domain logic | `applications`, `identity` |
| `search` | Boolean job-search query generator; stateless | none |
| `app` | Runnable application, shared layout and config; wires the modules together | all |

Rules:

- A module's public API is the types in its root package, e.g. `com.jobtracker.applications`. Anything in a sub-package (`...applications.internal`, `...applications.web`) is private to that module. Other modules never touch another module's entities or repositories.
- [ModularityTests](app/src/test/java/com/jobtracker/ModularityTests.java) (Spring Modulith) fails the build if a module uses another module's internals or if module dependencies form a cycle.
- Each module keeps its own Flyway migrations in `src/main/resources/db/migration`. Name them `V<yyyyMMddHHmm>__<module>_<description>.sql` so versions from different modules never collide. Hibernate only validates the schema; it never changes it.

## License

Source-available under the [PolyForm Shield License 1.0.0](LICENSE.md). You are free to clone, use, modify and self-host it. You may not offer it, or anything built from it, as a competing product, whether hosted, sold or given away.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md). Contributions require agreeing to the Contributor License Agreement described there.
