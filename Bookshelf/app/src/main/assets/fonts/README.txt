Font files are not included in this project (binary assets) -- download these
four open-source families and drop the files below into this folder with
these exact names. ReaderFont.kt / CssInjector.kt already reference these paths.

--- Tinos (default font, labelled "Times New Roman" in the app) ---
  https://fonts.google.com/specimen/Tinos
  -> Tinos-Regular.ttf
  -> Tinos-Bold.ttf
  -> Tinos-Italic.ttf
  -> Tinos-BoldItalic.ttf

  Why Tinos and not actual Times New Roman: real Times New Roman is a Monotype
  font licensed only for bundling with Windows/Office -- it legally can't ship
  inside this app's APK. Tinos is Google's open-source (Apache 2.0), metrically
  compatible clone, built specifically to substitute for Times New Roman --
  same character widths, near-identical look. It's labelled "Times New Roman"
  in the reader settings since that's the reading feel being asked for, but
  it's worth knowing it's a substitute, not the literal font.

--- PT Serif ---
  https://fonts.google.com/specimen/PT+Serif
  -> PTSerif-Regular.ttf
  -> PTSerif-Bold.ttf
  -> PTSerif-Italic.ttf
  -> PTSerif-BoldItalic.ttf

--- Domine ---
  https://fonts.google.com/specimen/Domine
  -> Domine-Regular.ttf
  -> Domine-Bold.ttf

  Domine doesn't have an italic release upstream (as of the version available
  when this was written) -- only Regular and Bold exist. ReaderFont.kt's
  fontAssetMap already accounts for this: it points Domine's italic/bold-italic
  slots at the upright Regular/Bold files, so <i>/<em> text still renders in
  Domine, just without a slant, instead of silently falling back to a
  different font. No italic/bold-italic files are needed for this one.

--- Lato ---
  https://fonts.google.com/specimen/Lato
  -> Lato-Regular.ttf
  -> Lato-Bold.ttf
  -> Lato-Italic.ttf
  -> Lato-BoldItalic.ttf

All four are SIL Open Font License (Lato) or Apache 2.0 (Tinos) -- free to
bundle and redistribute in an app.
