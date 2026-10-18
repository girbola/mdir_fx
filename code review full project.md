# Code Review Full Project

## Scope reviewed

This review covers the overall `mdir_fx` project structure and the highest-risk areas across the application, including:

- `pom.xml`
- `src/main/java/com/girbola/Main.java`
- `src/main/java/com/girbola/controllers/**`
- `src/main/java/com/girbola/drive/**`
- `src/main/java/com/girbola/persistence/**`
- `src/main/java/com/girbola/fileinfo/**`
- `src/main/java/com/girbola/filelisting/**`
- `src/main/java/com/girbola/utils/**`
- `src/main/java/common/utils/**`
- representative existing tests under `src/test/java/**`

This is a review only. No source changes were made.

---

## Project overview

`mdir_fx` is a JavaFX desktop application for organizing media, handling duplicates, scanning selected folders, working with external drives, manipulating dates, and persisting state in SQLite. The project uses a fairly broad stack:

- JavaFX
- SQLite
- OSHI
- JavaCV / VLCJ
- metadata-extractor
- JUnit 5 + Mockito
- Maven

The project has useful structure already, but it also has several broad maintainability and correctness risks caused by heavy static coupling, mixed responsibilities, and side-effect-heavy utility/controller logic.

---

## Strengths

1. **Clear package grouping**
   - There is visible package separation between controllers, persistence, drive logic, utils, file info, and SQL concerns.

2. **Real-world functionality is already modeled**
   - The project handles actual end-user concerns like removable drives, persisted folder identities, file metadata, and media-specific workflows.

3. **A meaningful automated test baseline exists**
   - The project already includes many tests across DAO, utility, hashing, image handling, and some controller-adjacent logic.

4. **Use of `Path` and normalization exists in some critical areas**
   - Some newer code paths are moving in the right direction and use normalized `Path` logic instead of raw strings.

5. **Drive identity support is more advanced than simple drive-letter logic**
   - The code attempts to survive changing drive letters via serial / partition identity matching.

6. **Project metadata and dependency management are documented**
   - `pom.xml` is reasonably complete and reflects a modern Java/JavaFX desktop app.

---

## Critical issues

### 1. Global static coupling is very high
**Main hotspots:**
- `src/main/java/com/girbola/Main.java`
- `src/main/java/com/girbola/controllers/main/ModelMain.java`
- many classes reading `Main.conf`, `Main.bundle`, `Main.getProcessCancelled()`, and related static state

This is one of the biggest project-wide design problems.

Why it is harmful:
- classes are hard to instantiate in isolation,
- tests need broad environment setup,
- flow becomes hidden because dependencies are not explicit,
- refactoring becomes risky because many classes depend on global state.

This is not just a style issue. It is a structural project-wide testability and maintainability problem.

---

### 2. Hidden side effects in methods that look like lookups or helpers
**Examples:**
- `src/main/java/com/girbola/controllers/folderscanner/SelectedFolderUtils.java`
- several utility-style methods in controllers and drive logic

The project contains methods that appear to be pure helper/lookups but also:
- mutate model state,
- update connection flags,
- save to the database,
- trigger UI-visible changes.

Why this is dangerous:
- calling code cannot reason safely about read-only operations,
- bugs become very hard to trace,
- one innocent call can trigger persistence or state mutation.

---

### 3. String-based path logic still appears in risky places
**Examples:**
- selected-folder matching logic
- validation helpers
- drive-remap-related checks

Path handling is one of the most important parts of this application, and string prefix checks are too fragile for that responsibility.

Main risks:
- sibling folders can be misidentified,
- Windows case and separator issues,
- normalized vs non-normalized path mismatches,
- UNC path handling surprises,
- false-positive parent/child relationships.

Given the nature of the app, this is a correctness issue, not just a code-style issue.

---

### 4. Several “god methods” combine IO, DB, UI, and business decisions in one place
**Main hotspots:**
- `src/main/java/com/girbola/drive/DriveScanner.java`
- `src/main/java/com/girbola/controllers/datefixer/DateFixerController.java`
- `src/main/java/com/girbola/controllers/main/loadfileinfostableviews/LoadFileInfosUtils.java`
- parts of `ModelMain`

The project has several methods/classes that do too many things at once.

Typical mixed concerns inside single methods include:
- filesystem checks,
- drive scanning,
- SQL access,
- domain decision making,
- mutation of JavaFX state,
- logging,
- persistence updates.

This creates fragile code that is hard to test, hard to debug, and easy to regress.

---

### 5. Controller classes are carrying too much business logic
**Main hotspot:**
- `src/main/java/com/girbola/controllers/datefixer/DateFixerController.java`

Large JavaFX controllers with business logic inside them are a project-wide architecture smell.

Why this is harmful:
- UI classes become difficult to unit test,
- workflow logic is locked inside controller lifecycle and JavaFX thread rules,
- business behavior becomes difficult to reuse.

---

### 6. Error signaling is often weak or ambiguous
**Examples:**
- methods that return `null` for multiple unrelated failure modes,
- methods that log and continue without structured error reporting,
- file operation helpers that fail ambiguously.

In a project that manipulates real media files and drive-mounted storage, ambiguous failure states are dangerous.

---

## Medium-priority issues

### 7. Persistence patterns are inconsistent
**Areas:**
- `src/main/java/com/girbola/persistence/**`
- `src/main/java/com/girbola/sql/**`

Observed style risks:
- some places use proper try-with-resources,
- some places use manual resource cleanup,
- some methods open or fetch connections in ways that are easy to misuse,
- result/error semantics are not unified.

This makes the persistence layer harder to reason about and harder to harden.

---

### 8. Logging is deeply mixed into business flow
The code often interleaves logic and `Messages.sprintf(...)` / `Messages.sprintfError(...)` calls in nearly every branch.

Why it matters:
- logic becomes noisy,
- test readability drops,
- core behavior is harder to follow,
- branch structure gets obscured.

---

### 9. Domain objects appear relatively anemic
**Examples:**
- `FileInfo`
- `FolderInfo`
- `DriveInfo`
- some saved state models

A lot of business logic lives in utils and scanners instead of inside domain-centered abstractions.

That increases:
- utility-class sprawl,
- duplication,
- weak invariants,
- mutation from too many places.

---

### 10. Repeated scanning over lists/tables may become brittle and inefficient
There are many loops over:
- selected folders,
- folder infos,
- table items,
- drive lists.

Even when performance is still acceptable, repeated scan-based logic often signals missing abstractions such as indexes, lookup services, or canonical matchers.

---

### 11. File operation safety needs careful review
**Likely hotspots:**
- `src/main/java/com/girbola/controllers/operate/CopyHelper.java`
- `src/main/java/common/utils/FileUtils.java`
- file rename/move/copy flows

Because this project touches real user media on external drives, file operation correctness and rollback behavior should be treated as a top-level reliability concern.

Potential risks to review further:
- partial rename/copy scenarios,
- ambiguous return values,
- weak error reporting,
- handling of disconnected drives during operation.

---

## Testing quality and gaps

### Current positives
- There is already a decent number of tests.
- Utility and DAO style testing exists.
- Mockito static mocking is already used, which helps with the current architecture.

### Major gaps
1. **Too few tests around high-risk workflows**
   - drive remapping,
   - selected folder state transitions,
   - copy/rename recovery,
   - partial-failure file operations.

2. **Heavy static coupling makes tests harder than they should be**
   - many tests likely need broad environment assumptions.

3. **Controller logic is hard to test**
   - because business logic is trapped inside JavaFX-heavy classes.

4. **Integration coverage appears limited**
   - there should ideally be scenario-driven tests around:
     - saved folder restore,
     - drive letter changes,
     - disconnected drive recovery,
     - media folder reload,
     - date-fixer file updates.

5. **Edge-case path behavior deserves more parameterized testing**
   - siblings sharing prefixes,
   - UNC paths,
   - case differences,
   - invalid paths,
   - missing roots,
   - stale DB state.

---

## Architectural smells

### 1. Service locator / global singleton style
Global `Main` access acts like a hidden dependency provider.

### 2. Controllers acting as services
Controllers contain logic that should live in application services or domain services.

### 3. Utility-class sprawl
Important behavior is spread across many utility classes instead of being modeled as focused services or domain operations.

### 4. Implicit state machine behavior
Folder selection, drive connectivity, copy state, and load state appear to be represented via booleans and scattered checks rather than explicit workflow/state modeling.

### 5. Persistence and domain models are tightly coupled
DAO methods and business code often interact too directly, which makes both evolution and testing harder.

---

## Safest refactor order

### Phase 1: reduce correctness risk in path/file/drive handling
1. Replace dangerous string-based path checks with path-aware matching.
2. Separate pure lookup methods from mutating/persisting behavior.
3. Clarify failure signaling in persistence/load methods.

### Phase 2: reduce project-wide coupling
4. Start removing direct `Main.*` dependencies from non-entry-point classes.
5. Introduce explicit services/config dependencies through constructors where possible.

### Phase 3: split high-risk god methods
6. Refactor `DriveScanner` hotspots into smaller units.
7. Split `LoadFileInfosUtils` logic further into pure steps.
8. Move controller business logic into service classes.

### Phase 4: harden persistence and file operations
9. Standardize connection/resource handling.
10. Improve file-operation result/error modeling.
11. Add stronger recovery/rollback behavior where needed.

### Phase 5: strengthen tests around workflows
12. Add focused tests for drive remap and saved-folder restore.
13. Add copy/move failure scenario tests.
14. Add path edge-case parameterized tests.

---

## Top 10 hotspots

1. `src/main/java/com/girbola/Main.java`
   - Massive global coupling point for config, bundle, cancellation, and application state.

2. `src/main/java/com/girbola/controllers/main/ModelMain.java`
   - Central coordinator with many responsibilities and broad dependency fan-out.

3. `src/main/java/com/girbola/drive/DriveScanner.java`
   - High-complexity drive and remap logic; strong candidate for extraction and simplification.

4. `src/main/java/com/girbola/controllers/datefixer/DateFixerController.java`
   - Very large controller carrying too much business logic.

5. `src/main/java/com/girbola/controllers/folderscanner/SelectedFolderUtils.java`
   - Hidden side effects in helper-style methods; unsafe path matching semantics.

6. `src/main/java/com/girbola/controllers/main/loadfileinfostableviews/LoadFileInfosUtils.java`
   - Better than before, but still too responsibility-heavy in core load path.

7. `src/main/java/com/girbola/persistence/configuration/ConfigurationSavedFoldersDao.java`
   - Ambiguous failure signaling and inconsistent persistence semantics around saved folder state.

8. `src/main/java/com/girbola/controllers/operate/CopyHelper.java`
   - High-risk file operation behavior deserving dedicated review and stronger guarantees.

9. `src/main/java/common/utils/FileUtils.java`
   - Common file behavior utilities can amplify bugs project-wide if error semantics are weak.

10. `src/main/java/com/girbola/filelisting/ValidatePathUtils.java`
   - Path validation is central to correctness and should avoid brittle string logic.

---

## Highest-value next steps

If only a few things are tackled first, the highest-value order is:

1. fix path matching correctness,
2. remove hidden side effects from lookup/helper methods,
3. reduce `Main.*` static coupling in newly touched code,
4. split `DriveScanner` remap logic,
5. harden file operation error semantics,
6. add tests around real workflow failures.

---

## Final assessment

The project has real depth and useful structure, but the biggest long-term risks are architectural rather than cosmetic. The codebase would benefit most from:

- fewer hidden side effects,
- clearer dependency boundaries,
- path handling correctness,
- smaller focused services,
- stronger workflow-level tests.

The code is not beyond recovery at all. In fact, it looks very recoverable. But it is at the point where continued feature work without targeted cleanup will likely make the hardest parts significantly more fragile.

