# Install

Step-by-step from "I have a zip file" to "the app is on my phone."

## 1. Unzip the project

Download `Bookshelf.zip` to your computer (not just your phone -- you need
Android Studio, which runs on desktop) and extract it. You should end up
with a `Bookshelf` folder containing `app/`, `build.gradle.kts`,
`README.md`, etc. directly inside it -- if you see a `Bookshelf` folder
*inside another* `Bookshelf` folder, you've unzipped one level too deep;
Android Studio needs to open the outer one.

## 2. Add the font files

The app won't render fonts correctly without these -- they're binary files,
so they're not bundled in the zip.

1. Open `app/src/main/assets/fonts/README.txt` inside the extracted folder.
2. Follow the four Google Fonts links in it (Tinos, PT Serif, Domine, Lato).
3. Download each family, pull out the `.ttf` files it lists, and drop them
   directly into `app/src/main/assets/fonts/` -- same folder as that
   README.txt, with the exact filenames it specifies.

## 3. Open the project in Android Studio

1. If you don't have it yet: [developer.android.com/studio](https://developer.android.com/studio)
   -- download and install it like any other app.
2. Open Android Studio -> **File > Open** -> select the `Bookshelf` folder
   from step 1.
3. Android Studio will start "Gradle sync" automatically (a progress bar at
   the bottom) -- this downloads all the dependencies the project needs.
   First sync can take a few minutes; let it finish before doing anything
   else.
4. If sync fails, read the error at the bottom of the screen -- it's almost
   always a specific missing SDK component, and Android Studio usually
   shows a clickable "Install missing X" link right there.

## 4. Turn on developer mode + USB debugging on your phone

You mentioned you already have developer mode on -- if so, skip to step 5.
Otherwise:

1. **Settings > About phone** -> tap **Build number** 7 times in a row.
   You'll see a toast saying "You are now a developer."
2. Go back to **Settings** -> a new **Developer options** menu appears
   (usually under **System**, sometimes at the top level depending on your
   phone).
3. Inside Developer options, turn on **USB debugging**.

## 5. Connect your phone and run

1. Plug your phone into your computer with a USB cable.
2. Your phone will show a popup: **"Allow USB debugging?"** -- tap **Allow**
   (and check "always allow from this computer" if you don't want to
   re-approve it every time).
3. Back in Android Studio, look at the device dropdown near the top toolbar
   (it usually says "No devices" or shows an emulator name) -- click it and
   select your phone from the list.
4. Click the green **Run ▶** button next to that dropdown.
5. Android Studio builds the app and installs it directly on your phone --
   the first build is the slowest (a minute or two); after that it's much
   faster. The app opens automatically once installed.

## Troubleshooting

- **Phone doesn't show up in the device dropdown:** check the USB cable
  actually supports data transfer (some cables are charge-only), and check
  you tapped "Allow" on the debugging popup -- if you missed it, unplug and
  replug the cable to get the popup again.
- **Build fails with a font-related error:** double check the font files
  from step 2 are named *exactly* as `fonts/README.txt` specifies --
  filenames are case-sensitive.
- **Anything else:** copy the exact error text from the bottom panel in
  Android Studio and we can go through it together -- most first-build
  issues are a one-line fix once you can see the actual error message.

## After that

The app is now on your phone like any other app -- no need to keep it
plugged into the computer or re-run this process, except when you want to
push a code change. Each time you edit code and want to test it, just hit
Run again with the phone connected.
