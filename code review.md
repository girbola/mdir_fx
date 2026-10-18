# Code Review

## Scope reviewed

- `src/main/java/com/girbola/controllers/main/loadfileinfostableviews/LoadFileInfosBackToTableViews.java`
- `src/main/java/com/girbola/controllers/main/loadfileinfostableviews/LoadFileInfosUtils.java`
- `src/main/java/com/girbola/controllers/folderscanner/SelectedFolderUtils.java`
- `src/main/java/com/girbola/controllers/main/ModelMain.java`
- `src/main/java/com/girbola/drive/DriveScanner.java`
- `src/main/java/com/girbola/controllers/main/tables/model/SavedFolderInfos.java`
- `src/main/java/com/girbola/persistence/configuration/ConfigurationSavedFoldersDao.java`

## Quick summary

The code already has some good building blocks: `SavedFolderInfos.resolveCurrentFolderPath(...)` is a useful abstraction, the new `LoadFileInfosUtils` extraction is a step in the right direction, and path normalization is used in several important places.

The biggest problems are not syntax-level issues. They are logic-design issues:

- methods that mix too many responsibilities,
- query methods that also mutate state and save to the database,
- heavy coupling between filesystem, persistence, UI state, and drive remapping,
- control flow where `null` means too many different things,
- string-based path logic in places where path-aware comparisons should be used.

---

## Strengths

1. `SavedFolderInfos.resolveCurrentFolderPath(...)` is a good direction. It concentrates path resolution and drive identity matching into one place.
2. Path normalization is used often enough to reduce some path mismatch issues.
3. The extraction into `LoadFileInfosUtils` already improves readability compared to leaving everything inside the JavaFX `Service`.
4. The load flow now supports cancellation in a clearer way through `BooleanSupplier`.
5. Drive identity fields such as serial, UUID, and identifier give the project a better chance of surviving drive-letter changes.

---

## Critical issues

### 1. `SelectedFolderUtils.getSelectedFolderParent(...)` has hidden side effects
**File:** `src/main/java/com/girbola/controllers/folderscanner/SelectedFolderUtils.java`

This method looks like a pure lookup, but it is not.

It does all of this:
- searches for a matching selected folder,
- checks filesystem existence,
- mutates `selectedFolder.connected`,
- writes selected folders back to the configuration DB,
- logs an error.

That is a bad command/query separation break.

Why this is bad:
- calling code cannot safely use it as a normal finder,
- lookups unexpectedly write data,
- testing becomes much harder,
- a simple read can trigger persistent side effects.

Also, the match itself is string-based:
```java
if (folderPath.startsWith(selectedFolder.getFolder()))
```
That is brittle. String prefix matching can produce false positives.

Example of surprising behavior:
- selected folder: `C:\Media`
- checked path: `C:\MediaOld\video`

String `startsWith(...)` can treat that as a match even though path semantics say it is not a child path.

---

### 2. `LoadFileInfosUtils.loadSavedFolderToTable(...)` still does too much
**File:** `src/main/java/com/girbola/controllers/main/loadfileinfostableviews/LoadFileInfosUtils.java`

This method currently performs many responsibilities in one place:
- normalize original path,
- resolve current path from drive identities,
- decide whether remapping is needed,
- mutate selected folder state through remap,
- check whether folder belongs under selected folders,
- find selected folder parent,
- load `FolderInfo` from DB,
- create new `FolderInfo` from filesystem fallback,
- mutate `SavedFolderInfos.connected`,
- add folder to UI table,
- log every branch.

This is still the main “god method” in this flow, even after extraction.

Why this is bad:
- hard to unit test in isolation,
- hard to know which branch failed,
- high coupling to static methods and UI model state,
- bugs become hard to localize.

This method should eventually be split into smaller steps with explicit results.

---

### 3. `ConfigurationSavedFoldersDao.loadSavedFolderDetails(...)` overloads `null` and ignores saved connected state
**File:** `src/main/java/com/girbola/persistence/configuration/ConfigurationSavedFoldersDao.java`

Observed issues:

#### 3.1 `null` means too many things
This method returns `null` for:
- cancellation,
- DB creation/setup failure,
- row validation failure,
- thrown exceptions.

That makes the caller unable to distinguish:
- cancelled work,
- broken database,
- corrupt row,
- unexpected exception.

#### 3.2 Connected flag is reconstructed from `Files.exists(...)`
This line rebuilds the `connected` state directly from the filesystem:
```java
Files.exists(Paths.get(path))
```
That means the saved boolean is not really being loaded as saved state. It is recalculated immediately.

That may be intentional, but if so, the model name/value is misleading.
If it is not intentional, then the persisted `connected` field is effectively being ignored.

---

### 4. `DriveScanner.updateRootDrives(...)` is doing too many unrelated jobs
**File:** `src/main/java/com/girbola/drive/DriveScanner.java`

This method currently combines:
- root enumeration,
- drive identity resolution,
- deduplication,
- mutation of in-memory drive lists,
- connectivity updates,
- UI-thread scheduling,
- persistence through `DriveInfoDao.addDriveInfos(...)`,
- triggering selected-folder remapping.

That is too much responsibility for one method.

Why this is dangerous:
- hard to reason about partial failure,
- high risk of hidden regressions,
- difficult to test because it mixes IO, state mutation, and UI work.

This is one of the biggest “horrible logic” hotspots in the code I reviewed.

---

### 5. `DriveScanner.remapSelectedFoldersToCurrentDriveLetters(...)` mixes scan logic, validation, remapping, UI mutation, and persistence
**File:** `src/main/java/com/girbola/drive/DriveScanner.java`

This method does all of the following:
- iterates selected folders,
- parses/normalizes paths,
- skips existing folders,
- derives relative paths from previous roots,
- probes all current roots,
- checks for `mdir` database existence,
- loads `FolderInfo` from the DB,
- validates file drive serials,
- collects remap candidates,
- mutates UI-visible selected folders,
- rebases already-loaded `FolderInfo` paths,
- saves selected folders back to DB.

This is a very large behavioral surface inside one method.

Main logic smell:
- one method decides, validates, mutates, and persists.

That is exactly the kind of method that becomes scary to touch later.

---

### 6. `ConfigurationSavedFoldersDao.dropTable()` has resource handling and debugging problems
**File:** `src/main/java/com/girbola/persistence/configuration/ConfigurationSavedFoldersDao.java`

Issues:
- obtains a connection and never closes it in the method,
- uses manual statement close instead of try-with-resources,
- uses `printStackTrace()`.

This is not the worst logic bug in the project, but it is still low-quality infrastructure code.

---

## Medium-priority issues

### 7. String-based path logic exists in places where path-based logic should be used
Relevant files:
- `SelectedFolderUtils.java`
- `DriveScanner.java`

There are still places where string comparison or string prefix logic decides path relationships.
That is risky on Windows and for edge cases such as:
- case differences,
- trailing separators,
- sibling names sharing prefixes,
- UNC paths,
- normalized vs non-normalized forms.

---

### 8. `DriveScanner.hasMatchingFileInfoDriveSerial(...)` may be stricter than intended
**File:** `src/main/java/com/girbola/drive/DriveScanner.java`

Current logic:
- collect all non-blank serials from file infos,
- require all of them to match the drive identity.

That may be okay, but it is worth reviewing.

Potential surprise:
- one stale or bad file serial can invalidate an otherwise valid remap candidate.

If that strictness is intentional, keep it.
If not, the matching rule likely needs redesign.

---

### 9. Logging is extremely branch-heavy and often mixed into decision logic
Relevant files:
- `LoadFileInfosUtils.java`
- `DriveScanner.java`
- `ConfigurationSavedFoldersDao.java`

A lot of control flow is interleaved with `Messages.sprintf(...)` and `Messages.sprintfError(...)`.

This makes the business logic noisier and harder to read, and it makes tests more annoying when behavior is spread across branching plus logs.

---

### 10. Repeated scans over observable collections and tables
Relevant files:
- `SelectedFolderUtils.java`
- `LoadFileInfosUtils.java`
- `DriveScanner.java`

The code repeatedly scans selected folders and table contents with nested loops.
That is not necessarily a performance bug today, but it makes the logic more complex and encourages hidden duplication.

---

## Likely bugs or surprising behavior

### A. False-positive parent match because of string prefix logic
**File:** `SelectedFolderUtils.java`

`folderPath.startsWith(selectedFolder.getFolder())` is not a safe child-path test.
This can misclassify unrelated folders with the same string prefix.

### B. Lookup can disconnect a selected folder and save it immediately
**File:** `SelectedFolderUtils.java`

Calling `getSelectedFolderParent(...)` can mutate data and persist to the DB as a side effect.
That is surprising and dangerous.

### C. `connected` state loaded from DB is not really “loaded”
**File:** `ConfigurationSavedFoldersDao.java`

The `connected` value is rebuilt from `Files.exists(...)`, so users may think they are loading persisted state when they are really loading recomputed state.

### D. Partial remap behavior may be hard to reason about
**Files:** `LoadFileInfosUtils.java`, `DriveScanner.java`

Several flows mutate selected folders or loaded folder paths mid-process. If cancellation or later failure happens, it may be possible to leave in-memory state partially updated.

### E. Drive remap logic is spread across multiple places
**Files:**
- `SavedFolderInfos.java`
- `LoadFileInfosUtils.java`
- `DriveScanner.java`

There are several different drive/path remap decisions in the codebase. Even if each one is individually reasonable, spreading the rules across multiple classes raises the risk of behavior drift.

---

## Best refactor order

### Phase 1
1. Split `SelectedFolderUtils.getSelectedFolderParent(...)` into:
   - a pure finder,
   - a separate validation/update method.
2. Replace string prefix path checks with path-aware checks.
3. Replace `null` returns in `ConfigurationSavedFoldersDao.loadSavedFolderDetails(...)` with an explicit result type.

### Phase 2
4. Break `LoadFileInfosUtils.loadSavedFolderToTable(...)` into smaller private operations with explicit return values.
5. Isolate “load from DB or create from filesystem” into a dedicated helper/service.
6. Centralize selected-folder remap decisions into one place.

### Phase 3
7. Break `DriveScanner.updateRootDrives(...)` into separate responsibilities.
8. Break `DriveScanner.remapSelectedFoldersToCurrentDriveLetters(...)` into:
   - candidate discovery,
   - validation,
   - apply remap,
   - persistence.
9. Consolidate drive identity matching rules into a single reusable component.

---

## Worst hotspots right now

If only the worst logic is prioritized, I would start with these three:

1. `SelectedFolderUtils.getSelectedFolderParent(...)`
2. `DriveScanner.remapSelectedFoldersToCurrentDriveLetters(...)`
3. `DriveScanner.updateRootDrives(...)`

Those three have the biggest mix of hidden side effects, multi-responsibility logic, and future bug risk.

---

## Final note

The recent extraction into `LoadFileInfosUtils` was still a good move. The code is cleaner than before. But the real next gain will come from reducing side effects and centralizing path/remap rules, not just moving large methods between classes.

