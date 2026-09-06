# Bookshelf

A native Android EPUB reader. Scroll-mode **and** paginated (page-turn)
reading via WebView, 4 fonts (Times New Roman, PT Serif, Domine, Lato) with
real bold/italic weights, custom + preset page/text colors, highlights with
optional notes, TOC navigation, and a library with 3 sort modes
(Alphabetical / Last opened / By author).

## Setup

See `INSTALL.md` at the project root for full unzip-to-running-on-your-phone
steps. Quick version:

1. Open this folder in Android Studio (Koala or newer recommended) as an
   existing project -- it'll sync Gradle automatically.
2. **Add the font files.** They're not included (binary + licensing) --
   see `app/src/main/assets/fonts/README.txt` for exact filenames and
   where to download them (Tinos, PT Serif, Domine, Lato). The app will
   build without them, but text will fall back to whatever the
   `@font-face` `src` fails to load as (likely serif) until you add them.
3. Plug your phone in with developer mode + USB debugging on, hit Run.
   No Play Store signing needed for sideloading to your own device.
4. On first launch, use the "+" icon to pick a single `.epub`, or the
   folder icon to point at a folder and auto-import every `.epub` in it.

## What's implemented

- **Parsing** (`data/epub/`) -- container.xml -> OPF -> manifest/spine,
  plus both EPUB3 nav.xhtml and EPUB2 NCX table-of-contents formats.
- **Rendering** (`ui/reader/ReaderWebView.kt`) -- WebView per chapter,
  CSS + font-face injected into `<head>`, scroll-mode only (see below).
- **App theme** -- light/dark Material3 color scheme for the app shell
  (library, toolbars, dialogs), built from two custom palettes and
  following the system dark-mode setting. See `THEMING.md` at the
  project root for the full color mapping and how to adjust it. This
  is separate from the in-book page colors below.
- **Theming (in-book)** (`ui/settings/`) -- 5 quick presets (White/Sepia/Grey/Dark/
  Black) plus fully custom hex pickers for both background and text,
  with a low-contrast warning. 4 fonts (Times New Roman -- see the note
  in `ReaderFont.kt` about why that's actually the open-source Tinos
  substitute -- plus PT Serif, Domine, Lato), each with real bold/italic
  weight files except Domine, which has none upstream.
- **Page turning** -- both scroll mode (default) and paginated mode,
  toggled in reader settings. Paginated mode reflows the chapter into
  CSS columns sized to one screen each (`reader.js`'s
  `__setupPagination`); tapping the left/right screen edges turns a
  page, and paging past the first/last page of a chapter hands off to
  the previous/next chapter automatically. Progress is tracked and
  restored the same way in both modes (see `ProgressEntity`).
- **Highlights + notes** (`ui/highlights/`, `data/db/entities/HighlightEntity.kt`)
  -- one entity, one flow: select text, pick a color, optionally add a
  note. Offsets are stored against plain-text character position (not
  DOM position) so they survive font/size changes; the raw selected text
  is also stored as a fallback for re-locating a highlight if offsets
  ever drift.
- **Progress tracking** -- chapter index + scroll percent, saved on
  scroll (debounced 200ms in `reader.js`), restored on chapter load.
- **Library** -- grid view, 3 sort modes, single-file import or
  whole-folder scan, both via Storage Access Framework (no storage
  permission needed on API 30+, and books are never copied -- only a
  persistable read URI is retained, so duplicating storage isn't a
  concern).

## What's deliberately NOT implemented yet

- **DRM.** Intentionally out of scope -- this only opens DRM-free EPUBs.
- **Fragment-level TOC jumps** (`href#section-2`) currently jump to the
  right chapter but not the right scroll position within it. Extending
  `reader.js` with a `scrollIntoView` call keyed by the fragment id
  would close this gap.
- **Cross-device sync.** Everything is local-only (Room + DataStore).
- **Pagination edge cases worth knowing about:** the column-width math in
  `reader.js`'s `__setupPagination` assumes a simple text flow; a chapter
  with a large image, table, or embedded content wider than one column
  can still overflow awkwardly. This is the classic hard part of EPUB
  pagination mentioned earlier in the build -- functional for typical
  prose, but not bulletproof for every book layout you'll encounter.

## A note on testing

This was written without access to an Android build environment, so it
hasn't been compiled or run. It should build cleanly against the
dependency versions pinned in `app/build.gradle.kts`, but budget time
for the first-build debugging pass that any from-scratch Android project
needs -- Gradle version mismatches and Room's KSP codegen are the two
most likely early snags.
