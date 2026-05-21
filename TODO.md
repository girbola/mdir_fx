## Refactor checklist with concrete class-by-class targets

### 1) Media processing layer
**Target classes**
- `ImageUtils`
- `ImageOffsetUtils`
- `ImageComparionUtils`
- `FileInfoUtils`

**Checklist**
- [ ] Move thumbnail extraction into one dedicated service
- [ ] Keep only one hash pipeline per hash type
- [ ] Remove duplicate resize/decode logic
- [ ] Make metadata read/extract operations return consistent results
- [ ] Add cache keys based on path + size + last modified time

---

### 2) File info creation and classification
**Target classes**
- `FileInfoUtils`
- `FileInfo`
- `FileInfoOperations`

**Checklist**
- [ ] Extract file-type-specific creation logic into separate methods or services
- [ ] Replace scattered status flag updates with one state field
- [ ] Centralize duplicate detection rules
- [ ] Separate file I/O from model mutation
- [ ] Remove placeholder methods in `FileInfoOperations` or implement them properly

---

### 3) JavaFX controller cleanup
**Target classes**
- `DateFixerController`
- `OperateFiles`
- `MainController`
- `BottomController`
- `TableController`
- `SelectorController`

**Checklist**
- [ ] Move heavy file/image work out of controllers
- [ ] Keep event handlers thin
- [ ] Replace repeated UI toggling code with helper methods
- [ ] Stop doing long scans inside button handlers
- [ ] Make background task boundaries explicit

---

### 4) Table and selection logic
**Target classes**
- `Buttons`
- `TableUtils`
- `SelectionModel`
- `CheckTableContent`
- `RemoveDuplicates`
- `FolderInfoUtils`

**Checklist**
- [ ] Consolidate table selection helpers
- [ ] Remove repeated selection/filter loops
- [ ] Use shared predicate-based helpers for “good/bad/modified/etc.”
- [ ] Simplify duplicate and sort logic
- [ ] Make selection state updates predictable

---

### 5) Duplicate detection and similarity
**Target classes**
- `ImageComparionUtils`
- `FileInfoUtils`
- `AHash`
- `DHash`

**Checklist**
- [ ] Pick one canonical implementation per similarity algorithm
- [ ] Normalize hash output format
- [ ] Centralize comparison threshold logic
- [ ] Cache computed hashes per file
- [ ] Avoid re-reading the same image for multiple similarity checks

---

### 6) Drive and path handling
**Target classes**
- `OSHI_Utils`
- `StableDriveIdentifier`
- `DriveInfoUtils`
- `CommonUserFolders`
- `WorkdirUtils`

**Checklist**
- [ ] Cache drive/root lookup results
- [ ] Avoid repeated mount/serial resolution
- [ ] Separate platform detection from lookup logic
- [ ] Replace ad-hoc path checks with reusable utilities
- [ ] Standardize path normalization rules

---

### 7) Database and persistence
**Target classes**
- `DriveInfoSQL`
- `FileInfo_SQL`
- `FolderInfo_SQL`
- `ThumbInfoSQL`
- `SelectedFolderInfoSQL`
- `ConfigurationSavedFoldersDao`
- `SqliteConnection`

**Checklist**
- [ ] Review repeated queries and batch opportunities
- [ ] Centralize connection lifecycle handling
- [ ] Reduce duplicated SQL-building patterns
- [ ] Add result caching where safe
- [ ] Make DAO responsibilities narrower

---

### 8) Test cleanup
**Target classes**
- `FileInfoUtilsTest`
- `ImageComparationUtilsTest`
- `OSHI_UtilsTest`
- `SimpleDatesTest`
- `TableUtilsTest`

**Checklist**
- [ ] Replace exploratory tests with behavior-based assertions
- [ ] Remove tests that only print output
- [ ] Add tests for extracted services
- [ ] Add cache invalidation tests
- [ ] Add edge-case tests for null, empty, and unsupported files

---

## Suggested execution order
1. **`ImageUtils` / `ImageComparionUtils` / `ImageOffsetUtils`**
2. **`FileInfoUtils`**
3. **`DateFixerController`**
4. **`OperateFiles`**
5. **`Buttons` / `TableUtils` / selection helpers**
6. **DAO and path/drive utilities**
7. **Tests**

## Small rule of thumb
If a class:
- touches files,
- reads metadata,
- updates UI,
- and decides business rules,

it should probably be split.

If you want, I can turn this into a **prioritized TODO list with estimated effort**.