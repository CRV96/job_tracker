# Job Tracker Application

[![Build](https://github.com/CRV96/job_tracker/actions/workflows/build.yml/badge.svg)](https://github.com/CRV96/job_tracker/actions/workflows/build.yml)

## Running locally

Requires JDK 25+ and Docker. The app starts its own Postgres container from [app/compose.yaml](app/compose.yaml).

```bash
./mvnw -pl app -am spring-boot:run
```

Build and run all tests (tests use Testcontainers, so Docker must be running):

```bash
./mvnw verify
```

### Frontend

Pages are server-rendered with Thymeleaf; HTMX handles partial updates without a page reload. Tailwind CSS is compiled during the Maven build, which downloads its own Node into `web/target/`, so nothing extra needs installing. While editing templates, keep the CSS up to date with:

```bash
./mvnw -pl web frontend:npm@tailwind-watch
```

### Browser extension

Open `chrome://extensions`, turn on Developer mode, click **Load unpacked** and pick the `extension/` folder. Set the server URL and your profile id on the extension's options page.

## Project structure

A modular monolith: one Spring Boot application split into Maven modules. The domain modules hold the business logic and know nothing about the web. Two adapter modules expose them: `web` to the browser, `capture` to the extension.

| Module | Responsibility | Depends on |
|---|---|---|
| `identity` | Domain: local profiles (`users` table) | none |
| `jobs` | Domain: jobs saved as favorites or applied to, with each application's status and event timeline | none |
| `search` | Domain: Boolean job-search query generator; stateless | none |
| `web` | Adapter: browser UI. Page controllers, templates, shared layout, Tailwind and HTMX | `identity`, `jobs`, `search` |
| `capture` | Adapter: REST endpoint for the browser extension | `identity`, `jobs` |
| `app` | Runnable application and config; wires the modules together | all |
| `extension/` | Chrome extension (Manifest V3, plain JavaScript); not a Maven module | the `capture` API |

Every module groups its classes by type, with the same package names everywhere:

| Package | Holds | Where |
|---|---|---|
| `dto` | Records that carry data: service inputs and outputs, form objects, REST request and response bodies | all modules |
| `enums` | Enums | `jobs`, `capture` |
| `service` | Service interfaces | domain modules |
| `service.impl` | Service implementations (`Default*Service`) | domain modules |
| `entity` | JPA entities | domain modules |
| `repository` | Spring Data repositories | domain modules |
| `mapper` | Entity → DTO conversion (`*Mapper`, static methods) | domain modules |
| `exception` | Exceptions the service throws, e.g. `ApplicationNotFoundException` | domain modules |
| `controller` | Controllers and their exception handlers | `web`, `capture` |
| `constants`, `user` | Route paths; `CurrentUser` | `web` |

In the domain modules, `dto`, `enums`, `service` and `exception` are the public API. Each is marked `@NamedInterface` in its `package-info.java`, which tells Spring Modulith that other modules may use it. `service` uses `propagate = false`, so `service.impl` stays internal, as do `entity` and `repository`.

Conventions:

- **Records for DTOs:** they need no constructors, getters or `equals`.
- **Lombok for entities:** only `@Getter` and `@NoArgsConstructor(access = PROTECTED)`. Never use `@Data`, `@EqualsAndHashCode` or `@ToString` on entities. They trigger lazy loading, and on the two-way `ApplicationEntity` ↔ `TimelineEventEntity` link `toString` would recurse forever.
- **Package-private controllers and service implementations:** only the service interfaces can be called from Java code.
- **Entities never leave their module:** services return records, converted with the module's mapper.
- **Errors:**
  - Services throw the module's own exceptions, such as `ApplicationNotFoundException`.
  - In `web`, `WebExceptionHandler` turns them into an HTTP status, and every error page renders with [error.html](web/src/main/resources/templates/error.html) inside the layout.
  - In `capture`, `CaptureExceptionHandler` returns [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457) problem details (JSON) for the extension.
- **Formatting:** [.editorconfig](.editorconfig) sets tabs for Java and XML plus the import order. In IntelliJ, *Code → Reformat Code* with *Optimize imports* applies it.

Templates live in `web/src/main/resources/templates/<feature>/`, and all pages share [layout.html](web/src/main/resources/templates/layout.html).

Rules:

- A module's public API is its root package plus the packages marked `@NamedInterface`. Everything else is private to that module. Other modules never touch another module's entities or repositories.
- Domain modules never depend on the adapters. Business logic belongs in a domain module, never in a controller.
- [ModularityTests](app/src/test/java/com/jobtracker/ModularityTests.java) (Spring Modulith) fails the build if a module uses another module's internals or if module dependencies form a cycle.
- Each module keeps its own Flyway migrations in `src/main/resources/db/migration`, named `V<n>__<module>_<description>.sql`. Version numbers are shared by all modules: a new migration takes the next free number across the whole project, not just its own module. Hibernate only validates the schema; it never changes it.

## Testing

`./mvnw verify` runs everything; so does CI on every pull request and on every push to `main`. Each kind of test has one place:

| Test | Where | Example |
|---|---|---|
| Unit tests (plain JUnit + Mockito) | the module itself | none yet |
| Controller tests (`@WebMvcTest`) | `web` and `capture`, next to the controller | [HomeControllerTests](web/src/test/java/com/jobtracker/web/controller/HomeControllerTests.java), [CaptureControllerTests](capture/src/test/java/com/jobtracker/capture/controller/CaptureControllerTests.java) |
| Service tests against a real database (`@ApplicationModuleTest`) | `app`, in the module's package, e.g. `app/src/test/java/com/jobtracker/jobs/` | [JobsModuleTests](app/src/test/java/com/jobtracker/jobs/JobsModuleTests.java) |
| Whole application and module boundaries | `app` | [ModularityTests](app/src/test/java/com/jobtracker/ModularityTests.java) |

Why database tests live in `app`: the migrations only run there, and `jobs`' tables reference `identity`'s. `@ApplicationModuleTest` still starts just the one module, which also proves the module doesn't depend on other modules' beans.

`web` and `capture` can't see the real application class, so each has a small `*TestApplication` in its test sources for `@WebMvcTest` to start from.

## License

Source-available under the [PolyForm Shield License 1.0.0](LICENSE.md). You are free to clone, use, modify and self-host it. You may not offer it, or anything built from it, as a competing product, whether hosted, sold or given away.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md). Contributions require agreeing to the Contributor License Agreement described there.
