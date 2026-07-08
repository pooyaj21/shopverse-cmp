# ShopVerse — Compose Multiplatform

The Compose Multiplatform client of [ShopVerse](../). Shares one `commonMain` code
base across **Android** and **iOS**, talking to the same Supabase backend
([`../shopverse-supabase/docs/API.md`](../shopverse-supabase/docs/API.md)) as the
native Android reference app.

> Status: **scaffold + architecture in place.** Splash → Home (paged catalog) runs
> end-to-end against live Supabase on both platforms. Cart, orders/QR receipt, auth
> screens, and profile are the next milestones (see `../shopverse-idea/week-4-cmp-core.md`
> and `week-5-cmp-multiplatform.md`).

## Stack

Based on the in-house **ProvinCompose** architecture:

| Concern            | Choice                                                        |
| ------------------ | ------------------------------------------------------------- |
| UI                 | Compose Multiplatform 1.8, Material 3                         |
| Navigation         | JetBrains `navigation-compose` (type-safe routes)            |
| State              | `androidx.lifecycle` ViewModel + `MutableStateFlow`          |
| DI                 | Koin (`koin-compose`)                                         |
| HTTP               | **raw Ktor 3** client (mirrors the Android OkHttp repos)     |
| Local DB (cart)    | Room KMP + bundled SQLite                                     |
| Prefs / tokens     | DataStore Preferences                                         |
| Images             | Coil 3                                                        |

## Architecture (layers)

Mirrors the Android app's `:core:*` module split, expressed here as packages inside
the single `:composeApp` module:

```
com.shopverse.cmp
├── model/            domain models (Product, UserProfile, Order, LocalCartItem…)
├── network/
│   ├── service/util/ AppResult, safeApiCall, defaultConfig (Supabase headers),
│   │                 authConfig (refresh-token rotation), Session/Env config
│   ├── service/      Ktor services (AuthService, ProductService)
│   ├── model/        request + response DTOs, {data,meta} envelope, error shapes
│   ├── converter/    DTO → domain mappers
│   ├── repository/   repositories (return AppResult<domain>)
│   └── useCase/      use cases (return AppResult<domain>)
├── database/         Room: AppDatabase (expect/actual builder), CartItemDAO, entity
├── core/
│   ├── architecture/ BaseViewModel(State), ViewState, Route, Render, navigation/
│   ├── provider/     DataStoreProvider, DatabaseProvider (Composition-local singletons)
│   ├── dataStore/    Keys + blocking DataStore accessors + expect path
│   └── theme/        Material 3 light/dark
├── screen/           Screen/Dialog routes + feature screens (splash, home, product…)
├── App.kt            root composable: env init → providers → Koin → nav
├── DI.kt             Koin module
├── NavigationStack.kt
└── Platform.kt       expect: engine, versionName, isDebug, openLink (+ .android/.native)
```

The dependency direction matches Android: `screen → useCase → repository → service`,
with `model` shared by all.

### Supabase specifics

- **Two response shapes**: PostgREST returns naked JSON arrays; edge functions wrap in
  `{ data, meta }`. `safeApiCall` + `ErrorResponse` handle PostgREST / Auth / edge errors.
- **Headers**: every request carries `apikey` + `Authorization: Bearer <jwt-or-anon>`
  (`defaultConfig`). Public catalog + public order detail use the anon key as bearer.
- **Refresh-token rotation**: `authConfig` catches 401, refreshes behind a `Mutex`
  (concurrent refreshes would spend the rotating token), persists the new pair, and
  retries via `TokenRefreshedException`.
- **Pagination**: `?limit&offset` + `Prefer: count=exact` → `Content-Range` → `PagedResult`.

## Running

Prereqs: JDK 17, Android SDK, Xcode (for iOS).

Secrets live in `local.properties` (git-ignored) and are injected as generated
`SupabaseSecrets` constants at build time:

```properties
sdk.dir=/path/to/Android/sdk
supabase.url=https://cxfllbnxuvmeykjvtyeb.supabase.co
supabase.anonKey=<anon key from the Android project's local.properties>
```

- **Android:** `./gradlew :composeApp:assembleDebug` (or run the `composeApp` config from Android Studio).
- **iOS:** open `iosApp/iosApp.xcodeproj` in Xcode, set your Team in
  `iosApp/Configuration/Config.xcconfig`, target an iOS 15+ simulator, and Run.

## Notes / TODO

- **Desktop target** is not wired yet (the ProvinCompose base ships Android + iOS only).
  Adding it is a `jvm("desktop")` target + a `desktopMain` entrypoint + JVM Ktor/Coil
  engines — planned for the week-5 milestone.
- Launcher icons (`androidMain/res/mipmap-*`) and the iOS `AppIcon` are placeholders
  copied from the base project; swap them for `../shopverse-icons/` assets.
- The `shopverse://orders/<id>` deep link is registered on both platforms
  (Android manifest + iOS `Info.plist`) but not yet routed — wired with the orders feature.
```
