# Theming

The app shell (library screen, toolbars, dialogs, bottom sheets) has a proper
light/dark Material3 theme, built from two color palettes. It follows the
phone's system light/dark setting automatically -- no in-app toggle, since
Compose's `isSystemInDarkTheme()` already covers "phone in dark mode at
night" without extra code.

**This is separate from the in-book reading colors.** What color the *pages
of a book* render in (background/text color, per-book, changeable any time)
is still fully user-controlled via the reader settings sheet -- see
`ReaderSettingsSheet.kt` / `CssInjector.kt`. This document is only about the
chrome around the reading experience: the library grid, app bars, dialogs.

## Where the palettes came from

**Light theme** -- "Foggy Snow Scenery" (雾凇雪景):

| Role | Hex | Name |
|---|---|---|
| Background / surface | `#EDE9DA` | Warm Cream |
| Secondary surface | `#BAC8BC` | Sage Mist |
| Primary accent | `#87A0AC` | Slate Blue |
| Body text | `#5F5F5F` | Charcoal Text |

**Dark theme** -- "Black Cherry Bark":

| Role | Hex | Name |
|---|---|---|
| Background | `#271118` | Black Cherry Bark |
| Surface (cards, sheets) | `#68525C` | Smoked Mauve |
| Primary accent / body text | `#C4BFAC` | Warm Parchment |

## Where it's implemented

- `ui/theme/Color.kt` -- the named constants above, plus three derived tones
  (`SlateBlueContainer`, `SmokedMauveContainer`, `WarmParchmentDim`) that
  aren't from either source palette -- they exist only to fill Material3's
  `*Container` roles so buttons/chips have a sensible tinted background
  instead of falling back to stock Material defaults that would clash.
- `ui/theme/Theme.kt` -- `BookshelfTheme()`, a `lightColorScheme(...)` /
  `darkColorScheme(...)` pair built from those constants, plus the status
  bar color/icon-contrast handling so the status bar matches whichever mode
  is active.
- `MainActivity.kt` -- wraps the whole app in `BookshelfTheme { ... }`
  instead of a bare `MaterialTheme { ... }`.
- `res/values/themes.xml` and `res/values-night/themes.xml` -- set the
  Android-level window background to the same background colors *before*
  Compose has loaded, so there's no flash of a mismatched white/black frame
  on cold app start.

## How to adjust a color

1. Change the hex value in `Color.kt` -- e.g. if Sage Mist should be a touch
   darker, edit the `SageMist` constant. Every screen using that role
   updates automatically; nothing else needs to change.
2. If you want a *role* to point at a different color entirely (e.g. make
   the Slate Blue the background instead of the primary accent), that's a
   one-line swap in `LightColors`/`DarkColors` inside `Theme.kt` -- just
   move which constant is assigned to which parameter.
3. To add a genuinely new color (not from either palette), add it to
   `Color.kt` first, then reference it from `Theme.kt`.

## A note on contrast

Both schemes were assigned so text sits on a background with reasonable
contrast (dark charcoal text on cream in light mode, light parchment text on
near-black or mauve in dark mode) -- but this wasn't run through a formal
WCAG contrast checker. If a specific screen looks hard to read once it's
running on-device, that's a sign to nudge the relevant `on*` color a shade
lighter/darker in `Color.kt` rather than a sign something is fundamentally
broken.
