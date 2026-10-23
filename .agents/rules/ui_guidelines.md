# UI Design & Layout Guidelines

## 1. Visual Fidelity & Proportion
- Match authoritative visual mockups with high precision (concentric arcs, radial tick rings, exact indicator positions, drop shadows, and typography).
- Label positioning must be mathematically aligned to visual anchor points (e.g., minimum and maximum range labels positioned directly beneath the respective arc tips).
- Dynamic labels must accurately reflect user-selected units (e.g. Celsius vs. Fahrenheit conversion).

## 2. Single-Page Viewport Fit
- Design primary dashboards to comfortably fit within standard mobile viewports (~720dp - 820dp total height) without requiring vertical scrolling.
- Balance vertical budgets carefully:
  - Header clearance: account for `statusBarsPadding()` and the fixed header height (~80dp - 84dp spacer).
  - Main gauges and hero visuals: avoid excessive heights that force controls off-screen.
  - Secondary cards and charts: balance graph canvas height and stat rows to prevent empty blank space voids at the bottom.
- Always retain `verticalScroll(rememberScrollState())` as a safety net for smaller screens, accessibility font scaling, and landscape orientation.

## 3. Micro-Animations & Interactivity
- Provide rich tactile feedback on interactive graphs (scrubbing, drag-to-inspect, highlighted peak indicators, tooltips with downward pointers).
- Animate state transitions smoothly using `animateFloatAsState` or `Animatable` with standard easing curves (`FastOutSlowInEasing`).
