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

Open `chrome://extensions`, turn on Developer mode, click **Load unpacked** and pick the `extension/` folder. Set the server URL and your profile id on the extension's options page; the Profiles page shows your id. The extension posts to `/api/captures`, set by `jobtracker.capture.path` in [application.properties](app/src/main/resources/application.properties); if you change it, change the extension too. Saving a posting you already saved doesn't create a duplicate, and saving a favorite marks it applied.

## Project structure

A modular monolith: one Spring Boot application split into Maven modules. The domain modules hold the business logic and know nothing about the web. Two adapter modules expose them: `web` to the browser, `capture` to the extension. `common` holds the little code they all share.

| Module | Responsibility | Depends on |
|---|---|---|
| `common` | Shared: `ErrorCode`, the base exceptions and the application logger (`JobTrackerLogger`). No beans, no tables, no business logic | none |
| `identity` | Domain: local profiles (`users` table) | `common` |
| `jobs` | Domain: jobs saved as favorites or applied to, with each application's status and event timeline | `common` |
| `search` | Domain: Boolean job-search query generator; stateless | none |
| `web` | Adapter: browser UI. Page controllers, templates, shared layout, Tailwind and HTMX | `common`, `identity`, `jobs`, `search` |
| `capture` | Adapter: REST endpoint for the browser extension | `common`, `identity`, `jobs` |
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
| `exception` | Exceptions the service throws, e.g. `ApplicationNotFoundException`, and the module's error codes, e.g. `JobsErrorCode`; in `common`, `ErrorCode`, `CommonErrorCode` and the base exceptions | domain modules, `common` |
| `logging` | `JobTrackerLogger` | `common` |
| `controller` | Controllers and their exception handlers | `web`, `capture` |
| `constants`, `user` | Route paths; `CurrentUser` | `web` |

In the domain modules, `dto`, `enums`, `service` and `exception` are the public API. Each is marked `@NamedInterface` in its `package-info.java`, which tells Spring Modulith that other modules may use it. `service` uses `propagate = false`, so `service.impl` stays internal, as do `entity` and `repository`. In `common`, `exception` and `logging` are marked the same way.

Conventions:

- **Records for DTOs:** they need no constructors, getters or `equals`. Records with more than three fields also get Lombok's `@Builder`: build them with `NewApplication.builder().title(...).build()` rather than a constructor call full of `null`s, and leave out the fields that have no value. The records the modules take as input (`NewApplication`, `NewTimelineEvent`) check their required fields in their constructor, since a builder can't make sure every required field is set.
- **Lombok for entities:** only `@Getter` and `@NoArgsConstructor(access = PROTECTED)`. Never use `@Data`, `@EqualsAndHashCode` or `@ToString` on entities. They trigger lazy loading, and on the two-way `ApplicationEntity` ↔ `TimelineEventEntity` link `toString` would recurse forever.
- **Package-private controllers and service implementations:** only the service interfaces can be called from Java code.
- **Entities never leave their module:** services return records, converted with the module's mapper.
- **Errors:**
  - Services throw the module's own exceptions. Each extends a base class from `common`, which decides the HTTP status: `NotFoundException` (404, one subclass per resource, such as `ApplicationNotFoundException`) or `BusinessRuleException` (400, thrown as is with the rule's code).
  - Every exception carries an error code from its module's enum (`IdentityErrorCode`, `JobsErrorCode`), such as `JOBS-001`.
  - The exception handlers log each one once, as a warning. Codes only go into ERROR logs, so these lines have none; `capture` still returns the code to the extension. Services don't log the exceptions they throw.
  - HTMX ignores error responses, so `layout.html` shows a "That didn't work" banner when an HTMX request fails.
  - Any other exception is unexpected: a bug or an outage. The handlers log it at ERROR with `COMMON-001` and its stack trace, and answer 500 without internal details. Spring MVC's own errors, such as text where a number was expected, keep their usual 4xx status.
  - In `web`, `WebExceptionHandler` turns them into an HTTP status, and every error page renders with [error.html](web/src/main/resources/templates/error.html) inside the layout.
  - In `capture`, `CaptureExceptionHandler` returns [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457) problem details (JSON) for the extension, with the code in an `errorCode` field. Every error carries a code, Spring MVC's own ones too: `COMMON-002` for a malformed request, and validation errors add an `errors` object (field → message).
- **Logging:** put Lombok's `@CustomLog` on a class (not `@Slf4j`) to get a `log` field of type `JobTrackerLogger`, set up in [lombok.config](lombok.config). `debug`, `info` and `warn` are plain SLF4J. `error` also takes an error code as its first argument, and needs one: `log.error(JobsErrorCode.SAVE_FAILED, "Could not save application {}", id, exception)`. The code is added to the message and as a separate `errorCode` field. Each module defines its codes as an enum implementing `ErrorCode` in its `exception` package, prefixed with the module's name (`JOBS-001`). Levels:
  - **INFO:** what changes (created, moved to another status, deleted).
  - **DEBUG:** reads, rejected forms and captures. Turn it on with `logging.level.com.jobtracker=DEBUG`; the line is ready in `application.properties`.
  - **WARN:** the modules' own exceptions, logged once by the exception handlers.
  - **ERROR:** only unexpected failures, always with a code.

  Log ids, never names or job details.
- **Page controllers extend `BaseController`:**
  - Before every handler it puts the selected profile in the model as `currentProfile`, which the header shows. That's the only time a request looks the profile up.
  - Handlers that need a profile call `requireProfile(model)`. With no profile selected it throws `ProfileNotSelectedException`, and `WebExceptionHandler` sends the browser to `/profiles` (HTMX requests get an `HX-Redirect` header instead of a redirect).
  - It also has `redirectTo(path)` and `fieldErrors(result)`, which maps each form field to its first validation message.
  - It gets `CurrentUser` through a setter, so the subclasses keep their Lombok constructors. Its `@WebMvcTest`s need a `@MockitoBean CurrentUser`.
- **Constants instead of repeated strings:**
  - `web`'s `AppConstants` holds the routes, template names, model attribute names and HTMX headers.
  - A form's length limits are constants on the form record (e.g. `NewEventForm.STAGE_LABEL_MAX_LENGTH`), and its validation messages use `{max}`.
  - `ErrorCode.PROPERTY_NAME` is the one name for the error code, in logs and in error responses.
- **Entities guard their own rules:** `ApplicationEntity.getEvents()` is read-only and `TimelineEventEntity`'s constructor is package-private, so events can only be added through `addEvent`, which keeps the status in step with the timeline.
- **Formatting:** [.editorconfig](.editorconfig) sets tabs for Java and XML plus the import order. In IntelliJ, *Code → Reformat Code* with *Optimize imports* applies it.

Templates live in `web/src/main/resources/templates/<feature>/`, and all pages share [layout.html](web/src/main/resources/templates/layout.html). Never name a model attribute `application`, `session` or `param`: Thymeleaf reserves those names, so the template would read something else. HTMX endpoints return a fragment of the page's template, e.g. `applications/detail :: application`.

Rules:

- A module's public API is its root package plus the packages marked `@NamedInterface`. Everything else is private to that module. Other modules never touch another module's entities or repositories.
- Domain modules never depend on the adapters. Business logic belongs in a domain module, never in a controller.
- [ModularityTests](app/src/test/java/com/jobtracker/ModularityTests.java) (Spring Modulith) fails the build if a module uses another module's internals or if module dependencies form a cycle.
- Each module keeps its own Flyway migrations in `src/main/resources/db/migration`, named `V<n>__<module>_<description>.sql`. Version numbers are shared by all modules: a new migration takes the next free number across the whole project, not just its own module. Hibernate only validates the schema; it never changes it.

## Testing

`./mvnw verify` runs everything; so does CI on every pull request and on every push to `main`. Each kind of test has one place:

| Test | Where | Example |
|---|---|---|
| Unit tests (plain JUnit + Mockito) | the module itself | [JobTrackerLoggerTests](common/src/test/java/com/jobtracker/common/logging/JobTrackerLoggerTests.java), [CurrentUserTests](web/src/test/java/com/jobtracker/web/user/CurrentUserTests.java) |
| Controller tests (`@WebMvcTest`) | `web` and `capture`, next to the controller | [ApplicationControllerTests](web/src/test/java/com/jobtracker/web/controller/ApplicationControllerTests.java), [CaptureControllerTests](capture/src/test/java/com/jobtracker/capture/controller/CaptureControllerTests.java), [CaptureExceptionHandlerTests](capture/src/test/java/com/jobtracker/capture/controller/CaptureExceptionHandlerTests.java) |
| Service tests against a real database (`@ApplicationModuleTest`) | `app`, in the module's package, e.g. `app/src/test/java/com/jobtracker/jobs/` | [ApplicationServiceTests](app/src/test/java/com/jobtracker/jobs/ApplicationServiceTests.java) |
| Whole application and module boundaries | `app` | [ModularityTests](app/src/test/java/com/jobtracker/ModularityTests.java) |

Why database tests live in `app`: the migrations only run there, and `jobs`' tables reference `identity`'s. `@ApplicationModuleTest` still starts just the one module, which also proves the module doesn't depend on other modules' beans.

`web` and `capture` can't see the real application class, so each has a small `*TestApplication` in its test sources for `@WebMvcTest` to start from.

## License

Source-available under the [PolyForm Shield License 1.0.0](LICENSE.md). You are free to clone, use, modify and self-host it. You may not offer it, or anything built from it, as a competing product, whether hosted, sold or given away.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md). Contributions require agreeing to the Contributor License Agreement described there.
