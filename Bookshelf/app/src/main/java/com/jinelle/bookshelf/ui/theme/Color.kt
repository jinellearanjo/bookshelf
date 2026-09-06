package com.jinelle.bookshelf.ui.theme

import androidx.compose.ui.graphics.Color

// ---- Light theme palette: "Foggy Snow Scenery" (雾凇雪景) ----
val WarmCream = Color(0xFFEDE9DA)      // background
val SageMist = Color(0xFFBAC8BC)       // surfaceVariant / secondary surfaces
val SlateBlue = Color(0xFF87A0AC)      // primary accent
val CharcoalText = Color(0xFF5F5F5F)   // body text on light backgrounds

// ---- Dark theme palette: "Black Cherry Bark" ----
val WarmParchment = Color(0xFFC4BFAC)  // primary accent / light text on dark backgrounds
val BlackCherryBark = Color(0xFF271118) // background -- the darkest tone
val SmokedMauve = Color(0xFF68525C)    // surface / elevated cards

// Derived tones -- not from either source image, but needed to fill out the full
// Material3 scheme (container/outline roles) without leaving them at stock defaults
// that would clash with the two custom palettes above.
val SlateBlueContainer = Color(0xFFD3DEE2)
val SmokedMauveContainer = Color(0xFF4E3B44)
val WarmParchmentDim = Color(0xFF9C978A)
