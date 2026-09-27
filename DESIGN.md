---
name: Tarsika Mobile Gallery — Unified Sanctuary
description: Unified design system with light and dark modes for the Tarsika personal photo gallery.
themes:
  - light: Tarsika Sanctuary
  - dark: Nocturnal Sanctuary
---

# Tarsika Mobile Gallery — Unified Design System

A single design system for a private, calm photo gallery, with two visual modes. The **light mode** uses warm archival surfaces and botanical greens; the **dark mode** uses low-luminance forest and obsidian surfaces for comfortable night browsing. Typography, spacing, elevation, shapes, and component guidance are documented per mode where their values differ.

## Light mode — Tarsika Sanctuary

### Design tokens

```yaml
name: Tarsika Sanctuary
colors:
  surface: '#fdf9f0'
  surface-dim: '#dddad1'
  surface-bright: '#fdf9f0'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f7f3ea'
  surface-container: '#f1eee5'
  surface-container-high: '#ece8df'
  surface-container-highest: '#e6e2d9'
  on-surface: '#1c1c16'
  on-surface-variant: '#414844'
  inverse-surface: '#31302b'
  inverse-on-surface: '#f4f0e7'
  outline: '#717974'
  outline-variant: '#c1c8c2'
  surface-tint: '#426655'
  primary: '#103426'
  on-primary: '#ffffff'
  primary-container: '#284b3c'
  on-primary-container: '#94baa7'
  inverse-primary: '#a9cfbb'
  secondary: '#55624c'
  on-secondary: '#ffffff'
  secondary-container: '#d5e5c8'
  on-secondary-container: '#596750'
  tertiary: '#402a00'
  on-tertiary: '#ffffff'
  tertiary-container: '#5d3e00'
  on-tertiary-container: '#d9a95d'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#c4ebd7'
  primary-fixed-dim: '#a9cfbb'
  on-primary-fixed: '#002115'
  on-primary-fixed-variant: '#2b4e3e'
  secondary-fixed: '#d8e7cb'
  secondary-fixed-dim: '#bccbb0'
  on-secondary-fixed: '#131f0d'
  on-secondary-fixed-variant: '#3d4b35'
  tertiary-fixed: '#ffdeae'
  tertiary-fixed-dim: '#f0be70'
  on-tertiary-fixed: '#281900'
  on-tertiary-fixed-variant: '#604100'
  background: '#fdf9f0'
  on-background: '#1c1c16'
  surface-variant: '#e6e2d9'
typography:
  headline-xl:
    fontFamily: Nunito Sans
    fontSize: 40px
    fontWeight: '700'
    lineHeight: 48px
    letterSpacing: -0.02em
  headline-xl-mobile:
    fontFamily: Nunito Sans
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 38px
    letterSpacing: -0.015em
  headline-lg:
    fontFamily: Nunito Sans
    fontSize: 30px
    fontWeight: '600'
    lineHeight: 36px
    letterSpacing: -0.01em
  headline-lg-mobile:
    fontFamily: Nunito Sans
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 30px
    letterSpacing: -0.01em
  headline-md:
    fontFamily: Nunito Sans
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 26px
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  body-sm:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 16px
  label-lg:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 18px
    letterSpacing: 0.01em
  label-md:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.02em
  label-sm:
    fontFamily: Inter
    fontSize: 10px
    fontWeight: '600'
    lineHeight: 14px
    letterSpacing: 0.04em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 0.75rem
  margin: 1rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 0.75rem
  space-lg: 1.25rem
  space-xl: 2rem
```

## Brand & Style

This design system embodies the ethos of a quiet sanctuary for personal memories. Built around the promise "Momenmu, tetap milikmu" (Your moments, forever yours), the visual philosophy rejects dopamine-driven social cues, cloud badges, and algorithmic noise in favor of serene local preservation.

The design movement combines **Minimalism** with an **Organic Tactile Warmth**:
- **Content-First Canvas:** Photography is treated with gallery-grade curation—generous breathability, unobtrusive chrome, and quiet edge treatments.
- **Privacy as Peace:** Visual cues highlight physical, on-device containment. Status indicators emphasize storage health and local encryption with organic calm rather than cold cybersecurity alerts.
- **Emotional Tone:** Meditative, grounded, archival, and restorative. Interactions feel deliberate and gentle, mirroring the sensation of handling a bespoke paper photo album.

## Colors

The palette draws directly from botanical and archival landscapes, anchoring digital media into earthly permanence:

- **Primary (`#284B3C` - Deep Forest Pine):** Establishes focal weight, high-contrast actions, and authoritative navigation structures. Evokes permanence and shelter.
- **Neutral Surface (`#F5F1E8` - Warm Cream Alabaster):** Replaces harsh digital white with an unbleached archival linen tone. Reduces eye fatigue during prolonged viewing.
- **Secondary (`#A9B89D` - Soft Sage Leaf):** Acts as secondary functional accents, filter chips, selection states, and quiet supportive surfaces.
- **Tertiary (`#D5A65A` - Wild Honey Amber):** Reserved for moments of warmth, favorites, key highlights, and delicate confirmation cues.

### Semantic Tones & Role Allocation
- **Surface Level 0 (Canvas):** `#F5F1E8`
- **Surface Level 1 (Card/Container):** `#EAE5DB`
- **Surface Level 2 (Floating Sheets/Modals):** `#DFD9CD`
- **Text Primary (Foreground):** `#1E2A23`
- **Text Secondary (Subtle Metadata):** `#5E6C63`
- **Local Vault / Privacy Green:** `#284B3C`
- **Destructive / Caution:** `#9E4334` muted terracotta to maintain natural harmony.

## Typography

The pairing reconciles humanity and precision:
- **Nunito Sans** serves as the display and header voice. Its subtle rounded terminals introduce approachability and organic rhythm, softening large date headers and album titles.
- **Inter** handles body copy, file metadata, EXIF details, and system toggles. Its structural neutrality guarantees crisp legibility against micro-scale details (shutter speed, ISO, focal length).

### Rules of Typesetting
- Headers utilize tighter letter-spacing to form cohesive, headline anchors.
- All uppercase metadata labels must apply `letterSpacing: 0.04em` or higher with medium weights to avoid visual heaviness.
- Text content never uses pure black (`#000000`); all primary text defaults to `#1E2A23` to preserve harmony with warm paper tones.

## Layout & Spacing

The layout is built for fluid touch manipulation, high-density photo viewing, and uninterrupted visual scanpaths.

- **Photo Mosaic & Grid Rules:**
  - Mobile default: Dynamic 3-column fluid grid scaling down to 2 columns on pinch-to-zoom, and 1 column for immersive chronological flow.
  - Image gutters are fixed at `space-xs` (4px) in dense grid views to maximize screen estate while preserving discrete separation.
  - Section headers (e.g., "Maret 2024", "Jelajah Lokasi") maintain `space-lg` top padding and `space-sm` bottom padding.

- **Responsive Adaptations:**
  - **Compact (Mobile < 600dp):** Margins set to `1rem` (16px), navigation anchored to a low-profile bottom bar with high hit areas.
  - **Medium / Expanded (Tablet/Foldable ≥ 600dp):** Margins widen to `1.5rem`, switching the navigation model from a bottom bar to a slim, grounded rail on the left.

## Elevation & Depth

This design system avoids steep drop-shadows and stark artificial light sources. Spatial depth relies on **Tonal Stratification** paired with **Whisper Outlines**:

- **Tonal Layers:** Elevation is defined by surface steps:
  - Base canvas (`#F5F1E8`) sits at the lowest tier.
  - Floating bottom action docks and cards sit on `#EAE5DB` or `#DFD9CD`.
- **Soft Ambient Light:** When overlay sheets or dialogs emerge, elevation uses an extra-diffused, botanical-tinted shadow:
  - `box-shadow: 0 8px 32px -4px rgba(40, 75, 60, 0.08)`
- **Whisper Outlines:** Cards and image containers utilize a hairline border (`1px solid rgba(40, 75, 60, 0.06)`) to preserve clean boundaries without requiring heavy contrast drops.
- **True Immersion Overlay:** When expanding an image to full view, the interface gracefully falls away into pure deep forest green or neutral matte black to remove all peripheral distraction.

## Shapes

The design system implements a deliberate 12px baseline curvature (`ROUND_TWELVE` architecture).

- **Standard Containers & Cards:** `0.75rem` (12px) border radius for album thumbnails, info cards, modal sheets, and dialogue panels.
- **Interactive Controls:** Buttons, chips, and text inputs utilize `0.75rem` (12px) to maintain a cohesive tactile handfeel.
- **Media Previews:** Grid photos remain gently softened at `0.25rem` (4px) in dense views, expanding to `0.75rem` (12px) when featured in list or spotlight widgets.
- **Utility / Selection Circles:** Multi-select nodes and floating indicator badges remain circular (`pill-shaped` / 9999px) to contrast with rectangular media grids.

## Components

### Buttons
- **Primary Action:** Solid Deep Forest Pine (`#284B3C`) fill with unbleached cream text (`#F5F1E8`). Height: 48px. Radius: 12px. Mild press state (`scale(0.98)`).
- **Secondary Action:** Tinted Sage Leaf (`#A9B89D` at 20% opacity) with `#284B3C` text.
- **Tertiary / Subtle:** Transparent background with `#284B3C` text, gaining an `#EAE5DB` fill on hover/touch.

### Filter & Date Chips
- Pill-shaped or 12px rounded elements.
- **Inactive:** `#EAE5DB` surface, `#5E6C63` text, no border.
- **Active:** `#284B3C` fill with `#F5F1E8` text, optionally featuring a subtle warm honey (`#D5A65A`) indicator pip.

### Lists & Album Rows
- Clean edge-to-edge layout with `1px` subtle divider (`rgba(40, 75, 60, 0.05)`).
- Leading album thumbnails styled with 12px corner rounding.
- Trailing metadata (file count, size) styled in `body-sm` with `#5E6C63`.

### Checkboxes & Multi-Select
- Circular checkboxes (`20px x 20px`) floating over photo top-right corners with an organic drop shadow.
- Checked state: `#D5A65A` honey fill with a `#FFFFFF` checkmark, signaling precious user collection.

### Input Fields (Search & Tagging)
- Background `#EAE5DB` with 12px corner radius and zero resting border.
- Focus state brings a 1.5px border in `#284B3C` and subtle ambient glow.
- Placeholder text in `#5E6C63`.

### Cards & Grouping Surfaces
- Album cards feature a stacked-photo appearance using offset tonal borders.
- Metadata cards on image inspection screens (EXIF, location map toggle, lens parameters) present unified `#EAE5DB` tiles separated by `space-sm`.

### Privacy & Storage Status Banner
- A dedicated local-first indicator component at the gallery head or settings:
  - Background: Soft Sage Leaf at 15% opacity.
  - Icon: Botanical lock or closed leaf glyph in `#284B3C`.
  - Copy: "Tersimpan aman di perangkat" (Stored securely on device).

## Dark mode — Nocturnal Sanctuary

### Design tokens

```yaml
name: Nocturnal Sanctuary
colors:
  surface: '#101412'
  surface-dim: '#101412'
  surface-bright: '#363a38'
  surface-container-lowest: '#0b0f0d'
  surface-container-low: '#181c1a'
  surface-container: '#1c201e'
  surface-container-high: '#272b29'
  surface-container-highest: '#323633'
  on-surface: '#e0e3df'
  on-surface-variant: '#d3c4b3'
  inverse-surface: '#e0e3df'
  inverse-on-surface: '#2d312f'
  outline: '#9c8f7f'
  outline-variant: '#4f4538'
  surface-tint: '#f2be6d'
  primary: '#ffcf87'
  on-primary: '#442c00'
  primary-container: '#e5b263'
  on-primary-container: '#664400'
  inverse-primary: '#7e570e'
  secondary: '#b8ccae'
  on-secondary: '#243520'
  secondary-container: '#3a4b34'
  on-secondary-container: '#a7bb9e'
  tertiary: '#cbdace'
  on-tertiary: '#26332b'
  tertiary-container: '#afbeb3'
  on-tertiary-container: '#404d44'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#ffddaf'
  primary-fixed-dim: '#f2be6d'
  on-primary-fixed: '#281800'
  on-primary-fixed-variant: '#614000'
  secondary-fixed: '#d4e9c9'
  secondary-fixed-dim: '#b8ccae'
  on-secondary-fixed: '#101f0c'
  on-secondary-fixed-variant: '#3a4b34'
  tertiary-fixed: '#d7e6da'
  tertiary-fixed-dim: '#bbcabf'
  on-tertiary-fixed: '#121e17'
  on-tertiary-fixed-variant: '#3c4a41'
  background: '#101412'
  on-background: '#e0e3df'
  surface-variant: '#323633'
typography:
  display-lg:
    fontFamily: Manrope
    fontSize: 40px
    fontWeight: '500'
    lineHeight: 48px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Manrope
    fontSize: 28px
    fontWeight: '600'
    lineHeight: 36px
    letterSpacing: -0.015em
  headline-lg-mobile:
    fontFamily: Manrope
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
    letterSpacing: -0.01em
  headline-md:
    fontFamily: Manrope
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
    letterSpacing: -0.01em
  title-sm:
    fontFamily: Manrope
    fontSize: 16px
    fontWeight: '500'
    lineHeight: 24px
    letterSpacing: 0em
  body-lg:
    fontFamily: Manrope
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
    letterSpacing: 0em
  body-md:
    fontFamily: Manrope
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
    letterSpacing: 0.01em
  label-md:
    fontFamily: JetBrains Mono
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.04em
  label-sm:
    fontFamily: JetBrains Mono
    fontSize: 10px
    fontWeight: '400'
    lineHeight: 14px
    letterSpacing: 0.06em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 1rem
  gutter-mobile: 0.5rem
  margin: 1.5rem
  margin-mobile: 1rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2.5rem
```

## Brand & Style

The design system is crafted for intimate, late-night photography browsing, prioritizing circadian comfort, privacy, and visual serenity. Designed to disappear into dim ambient environments, the UI avoids stark light pollution while providing rich visual fidelity. The emotional tone is meditative, protective, and luxuriously subdued—like an architectural darkroom nestled in a nocturnal forest.

The visual style blends **Organic Minimalism** with **Tonal Glassmorphism**:
- Low-luminance, rich charcoal obsidian and deep evergreen surfaces prevent ocular fatigue.
- Accents use bioluminescent honey gold and earthy leaf sage to provide clarity without harshness.
- Pure photographic focus: chrome, borders, and overlays stay muted until deliberately engaged.

## Colors

The palette employs deep organic darks, low-stimulus midtones, and high-legibility warm neutrals to preserve night-adjusted vision.

### Palette Architecture
- **Obsidian Ground (`#121614` / `#181d1a`)**: The base background layer, absorbing light and letting photographs stand out without perimeter glare.
- **Forest Sanctuary Tones (`#0f1a14` / `#15241c`)**: Structural base for toolbars, drawers, and modal backdrops.
- **Elevated Surfaces (`#1a251f` / `#223028`)**: Surface containers, popovers, and elevated metadata cards.
- **Borders & Dividers (`#233028`)**: Hairline boundaries used sparingly to structure layout without luminous contrast.
- **Honey Gold Accent (`#e5b263`)**: Used for focal actions, favorites, rating stars, active selection indicators, and subtle focus rings.
- **Sage Leaf Sub-accent (`#7c8f74`)**: Filter chips, passive icons, metadata tags, and secondary action hints.
- **Text Layers**:
  - Primary text: Warm Cream (`#e9ece6`) for anti-fatigue clarity.
  - Secondary text: Subdued Lichen (`#c5cbc1`) for timestamps, camera EXIF tags, and disabled states.

## Typography

The typography couples the refined geometric balance of **Manrope** with the technical precision of **JetBrains Mono** for photographic metadata and EXIF telemetry.

- **Headlines & Body (Manrope)**: Rendered in semi-bold weights for headlines and regular for reading copy, using a softened letterform profile that eliminates harsh optical vibration in pitch darkness.
- **Metadata & Technical Accents (JetBrains Mono)**: ISO ratings, aperture values, timestamps, shutter speeds, and file weights are set in monospaced glyphs to ensure strict alignment across tabular photo inspectors.
- **Contrast Control**: Never apply pure `#ffffff`. All text defaults to Warm Cream (`#e9ece6`) or Lichen (`#c5cbc1`), protecting nighttime dark adaptation.

## Layout & Spacing

The layout is built around a scalable, fluid grid that lets imagery breathe while maintaining structural discipline across multi-pane desktop layouts and edge-to-edge mobile screens.

- **Photo Wall & Grid**:
  - Desktop / Tablet: Fluid dynamic grid (3 to 6 columns) utilizing `gutter: 1rem` to preserve separation without broad negative space.
  - Mobile: Tight 2-column or 3-column mosaic utilizing `gutter-mobile: 0.5rem` to prioritize full preview scale.
- **Canvas Margins**:
  - `margin: 1.5rem` for desktop browser chrome and detail views.
  - `margin-mobile: 1rem` for mobile navigation shells, collapsing to `0` when viewing individual photos in full-screen immersion.
- **Rhythm**: Spacing increments use an 8-point base (`0.25rem`, `0.5rem`, `1rem`, `1.5rem`, `2.5rem`) strictly governing layout intervals and metadata cluster padding.

## Elevation & Depth

Rather than conventional drop shadows—which can disrupt deep black levels—depth is conveyed through **chromatic surface elevation**, **low-contrast forest borders**, and **diffused ambient glows**.

1. **Layer 0 (Base Canvas)**: `#121614` — Deep obsidian background.
2. **Layer 1 (Persistent Chrome & Toolbars)**: `#15241c` with `backdrop-filter: blur(16px)` and 85% opacity, grounded with a 1px border of `#233028`.
3. **Layer 2 (Cards, Trays, Popovers)**: `#1a251f` background with a crisp `#233028` subtle outer outline.
4. **Layer 3 (Floating Modals & Lightbox Controls)**: `#223028` accompanied by an ultra-diffused, tinted shadow: `box-shadow: 0 12px 32px -4px rgba(7, 12, 9, 0.7)`.
5. **Active Focus & Selection Glow**: Elements under selection gain a soft perimeter bloom using Honey Gold (`box-shadow: 0 0 0 1px #e5b263, 0 0 16px -2px rgba(229, 178, 99, 0.25)`).

## Shapes

The design system employs **Rounded (`roundedness: 2`)** geometry to balance modern organic softness with photo framing precision.

- **Image Thumbnails & Cards**: Use `0.5rem` (8px) corners to preserve the rectangular integrity of aspect ratios while eliminating needle-sharp edges.
- **Dialogs & Sheet Panels**: Use `1rem` (16px) corners (`rounded-lg`) for a soft, tactile feel.
- **Buttons, Badges, and Chips**: Use fully rounded pill contours to create distinct tactile interaction targets compared to rectangular photo assets.

## Components

### Buttons
- **Primary**: Solid Honey Gold (`#e5b263`) background with `#121614` text and `600` weight. Pill-shaped (`rounded-full`), padded with `0.5rem 1.25rem`. Hover states introduce a subtle brightness boost without increasing white levels.
- **Secondary / Ghost**: `#1a251f` background with `#233028` border and `#e9ece6` text. Active state transitions border to `#7c8f74`.
- **Icon / Floating Controls**: Circular 40px buttons with `#15241c` background, 80% opacity, and `backdrop-filter: blur(12px)`.

### Chips & Filter Tags
- **Passive**: `#181d1a` background, 1px border in `#233028`, text in `#c5cbc1`, using `label-md`.
- **Selected**: `#223028` background, 1px border in `#7c8f74`, text in `#e5b263`.

### Photo Cards & Grid Items
- Smooth edge definition with an internal `1px inset` subtle highlight (`rgba(255, 255, 255, 0.04)`).
- Hover or focused item introduces an outer glow of `rgba(229, 178, 99, 0.3)`.
- Metadata overlays use vertical gradient scrims from `transparent` to `rgba(18, 22, 20, 0.85)`.

### Inputs & Search Bars
- Background: `#15241c`.
- Border: 1px `#233028`, transitioning to `#7c8f74` on hover and `#e5b263` on focus.
- Placeholder text: `#7c8f74`. Input value: `#e9ece6`.
- Typography: `body-md` paired with a monospaced shortcut tag (`label-sm`).

### Checkboxes & Rating Stars
- Checkbox: Square with `0.25rem` radius, bordered in `#7c8f74`. Checked state fills with `#e5b263` using dark checkmark glyph (`#121614`).
- Rating Stars: Empty stars outlined in `#7c8f74` at 40% opacity; selected stars illuminated in solid `#e5b263` with subtle drop bloom.

### EXIF Telemetry Drawer
- Floating side sheet or bottom overlay panel in `#15241c` with border `#233028`.
- Renders camera parameters (f-stop, focal length, ISO, shutter) inside compact pill badges styled with `#1a251f` and `JetBrains Mono` label tokens.
