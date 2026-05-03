# Android Native Minimal TXT Reader (with Bookshelf) - Design

Date: 2026-05-03  
Status: Draft Approved in Conversation, Written for Review

## Scope in one line
Build an Android native minimal TXT reader with bookshelf, system file picker import, URI-based storage, overlay page-turn reading, and essential reading settings.

## Goals
- Read local `.txt` files reliably in a native Android experience.
- Provide a lightweight bookshelf for imported books.
- Support overlay page-turn interaction in reader mode.
- Persist reading progress and restore accurately.
- Keep first release minimal but structurally extensible.

## Non-goals (v1)
- Folder scanning import.
- Copying files into app-private storage.
- Cloud sync and multi-device sync.
- Rich text formats (`epub`, `pdf`, etc.).

## Product decisions confirmed
- Import method: system file picker only.
- Storage method: persist SAF URI only (no file copy).
- Reader interaction: overlay page-turn.
- Settings scope: font size, line spacing, background theme (light/eye-care), brightness, manual chapter switch, progress jump.

## Architecture
Use Compose UI for bookshelf/settings and a native pagination core for reader logic.

### Layers
1. UI Layer (`bookshelf`, `reader`, `settings` screens in Compose)
2. State Layer (`ReaderViewModel`, screen state reducers, interaction handlers)
3. Domain/Core Layer (`PaginationEngine`, chapter split and position mapping)
4. Data Layer (`Room`, `DataStore`, `ContentResolver` file access)

This keeps rendering logic and pagination logic separated so pagination can be tested with JVM tests without UI dependencies.

## Components

### 1) FilePickerGateway
- Responsibilities:
- Launch system picker and return selected file URI.
- Request and persist URI read permission via `takePersistableUriPermission`.
- Read metadata (display name, size) from `ContentResolver`.
- Output:
- `BookSource(uri, name, sizeBytes)`.

### 2) BookshelfRepository (Room)
- Responsibilities:
- Manage bookshelf entries (insert, delete, query, unavailable flag).
- Maintain `lastReadAt` ordering for recent reads.
- Output:
- Reactive bookshelf list stream for UI.

### 3) ReaderRepository
- Responsibilities:
- Open text input stream from persisted URI.
- Charset detection fallback path: UTF-8 -> GBK.
- Build chapter index using pragmatic heuristics (title-like lines and blank-line boundaries).
- Output:
- Text/chapter access abstraction consumed by `PaginationEngine`.

### 4) PaginationEngine (Core)
- Responsibilities:
- Build pages from text + typography + viewport.
- Provide `prev/current/next` page snapshots.
- Remap visible location when font size/line spacing changes using global text offset.
- Output:
- `PageSnapshot` with page text block and positioning metadata.

### 5) ReaderViewModel + ReaderState
- Responsibilities:
- Handle gestures and page-turn commands.
- Coordinate loading, pagination, animation state, settings, and jump actions.
- Serialize progress updates through throttled persistence.
- Output:
- UI-ready state for rendering and controls.

### 6) ProgressManager (DataStore + Room)
- Responsibilities:
- Persist reading position per book (chapter index + global offset).
- Persist reading preferences (font size, line spacing, theme, brightness).
- Restore progress/settings at reader startup.

## Data flow

### A. Import to bookshelf
1. User taps add-book.
2. `FilePickerGateway` opens system picker.
3. On selection, persist URI permission.
4. Build book metadata and insert into `BookshelfRepository`.
5. UI updates bookshelf list sorted by `lastReadAt`.

### B. Open reader
1. `ReaderViewModel` loads saved progress/settings.
2. `ReaderRepository` opens URI stream and resolves charset.
3. Chapter index is prepared.
4. `PaginationEngine` generates first `PageSnapshot` using viewport + settings + saved offset.
5. Reader UI renders first screen.

### C. Overlay page-turn
1. Gesture triggers `next` or `prev` request.
2. Engine returns adjacent `PageSnapshot`.
3. UI animates overlay transition.
4. On animation finish, throttled progress write executes.

### D. Settings change
1. User updates typography/theme/brightness.
2. State updates immediately in UI.
3. Engine recalculates affected pagination.
4. Reader location is restored by global offset mapping.
5. Preferences persist through `ProgressManager`.

### E. Chapter/progress jump
1. Chapter jump maps to target chapter start offset.
2. Progress jump maps percentage to global offset.
3. Engine materializes target page.
4. Progress write occurs immediately after successful jump.

## Error handling

### 1) URI unavailable / permission lost
- Symptom: open fails due to moved/deleted file or permission invalidation.
- Behavior:
- Mark book as `unavailable` on bookshelf.
- Show clear rebind action.
- Rebind flow opens system picker and updates URI while keeping existing progress record.

### 2) Encoding failure / unreadable text
- Behavior:
- Try UTF-8, fallback to GBK.
- If still unreadable, show non-blocking error with manual encoding switch.
- Rebuild chapter/pagination after encoding switch, without wiping progress data.

### 3) Large file startup latency
- Behavior:
- Show loading skeleton and status text.
- Prioritize first readable page, continue indexing/pagination incrementally in background.
- Keep interaction responsive after first page is shown.

### 4) Layout change drift
- Behavior:
- Do not trust old page index after typography changes.
- Recover by global offset remap.
- If target invalid, fallback to nearest valid chapter start and show one-time hint.

### 5) Persistence write failure
- Behavior:
- Keep in-memory reading state.
- Retry persistence with bounded backoff.
- Show lightweight warning in settings/status area; do not block reading.

### 6) Gesture conflict during animation
- Behavior:
- While overlay animation is active, ignore new page-turn commands.
- Accept only cancel/finish flow to prevent state overlap.

## Testing strategy

### Layer 1: JVM unit tests (core-first)
- `PaginationEngineTest`:
- Next/prev boundary behavior.
- Global offset remap after typography changes.
- Chapter/progress jump mapping correctness.
- `ReaderRepositoryTest`:
- UTF-8 and GBK fallback behavior.
- Empty file and malformed byte-stream cases.
- `ProgressManagerTest`:
- Throttled writes.
- Restart restore consistency.

### Layer 2: Instrumentation tests (critical paths)
- Import flow via file picker updates bookshelf.
- Reader restores last progress after app relaunch.
- Overlay animation lock prevents overlapping page-turn actions.
- Unavailable URI state triggers rebind path.

### Layer 3: Manual acceptance checklist
- Import and read small/medium/large TXT files.
- 100 continuous page-turn actions without crash or stuck state.
- Settings and progress persist after restart.
- Deleted source file shows unavailable state and successful rebind recovery.

## Top 3 constraints
1. SAF URI-only model means source files can disappear outside app control.
2. Overlay page-turn requires tight state sequencing to avoid gesture-animation races.
3. Re-pagination cost rises with file size and typography changes.

## Risks and mitigations
- Risk: slow first open on large files.
- Mitigation: staged load (first page priority) + incremental background pagination.
- Risk: progress drift after style change.
- Mitigation: global text offset as canonical position model.
- Risk: unstable URI lifecycle.
- Mitigation: explicit unavailable state + one-tap rebind flow.

## Delivery notes
- This design intentionally favors correctness and recoverability over feature breadth in v1.
- If approved, next step is an implementation plan with milestones and test-first slices.

