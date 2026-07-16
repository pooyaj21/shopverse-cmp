# ShopVerse — Compose Multiplatform

The Compose Multiplatform client of [ShopVerse](../). Shares one `commonMain` code
base across **Android** and **iOS**, talking to the same Supabase backend
([`../shopverse-supabase/docs/API.md`](../shopverse-supabase/docs/API.md)) as the
native Android reference app.

> Status: ✅ **done — full canonical feature set on Android + iOS**: splash,
> onboarding, auth (sign-up / login / logout / delete account), paged catalog,
> product detail, local cart, order submit with QR receipt, order history +
> detail, profile + theme toggle, and `shopverse://orders/<id>` deep linking.
> Android releases ship via fastlane + GitHub Actions + Firebase App
> Distribution (see below); iOS builds are verified in CI.

## Get the app

Want to try ShopVerse CMP on Android? Join the tester group on Firebase App
Distribution and you'll get every release straight to your device:

**[appdistribution.firebase.dev/i/204fa6628f6b5b63](https://appdistribution.firebase.dev/i/204fa6628f6b5b63)**

Alternatively, grab the signed APK attached to the latest
[GitHub Release](../../releases/latest).

There is **no iOS release** — installable iOS builds require an Apple
Developer account, which this project doesn't have. The iOS app builds and
runs from source (see [Running](#running)), and CI verifies every commit
still compiles for iOS.

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

## Release pipeline (fastlane + GitHub Actions)

`.github/workflows/release.yml` runs on every `v*` tag push (or manually
via *Actions → Release → Run workflow*). It builds the signed release APK
with fastlane, publishes a GitHub Release with the APK attached, and
uploads the same APK to Firebase App Distribution.

Fastlane lanes (`fastlane/Fastfile`):

```bash
bundle exec fastlane android build       # clean + :composeApp:assembleRelease (signed APK)
bundle exec fastlane android release     # build + GitHub release + Firebase distribution
bundle exec fastlane android distribute  # upload an APK to Firebase App Distribution only
```

Required repository **Actions secrets**:

| Secret                          | Value                                                    |
| ------------------------------- | -------------------------------------------------------- |
| `KEYSTORE_BASE64`               | `base64 -i key.jks`                                      |
| `STORE_PASSWORD`                | keystore store password                                  |
| `KEY_ALIAS`                     | key alias                                                |
| `KEY_PASSWORD`                  | key password                                             |
| `SUPABASE_URL`                  | Supabase project URL                                     |
| `SUPABASE_ANON_KEY`             | Supabase publishable anon key                            |
| `FIREBASE_APP_ID`               | Firebase Android app ID for `com.shopverse.cmp`          |
| `FIREBASE_SERVICE_ACCOUNT_JSON` | service account key JSON (Firebase App Distribution Admin role) |

(`GITHUB_TOKEN` is provided automatically by Actions.)

Cutting a release:

```bash
git tag v1.0.0
git push origin v1.0.0
```

Note: the workflow builds the commit the tag points to — after pushing a
fix, re-point the tag (`git tag -d`, push the deletion, re-tag) rather than
re-running the failed workflow, which would reuse the old commit.

iOS is not part of the distribution pipeline — installable iOS builds
require an Apple Developer account (TestFlight or Ad Hoc signing), which
this project doesn't use. Instead, `.github/workflows/ios-build.yml` runs
on every push/PR to `main` and verifies the iOS target builds: it compiles
the Kotlin framework for `iosSimulatorArm64` and builds the Xcode app for
the simulator (no code signing needed).

## Notes / TODO

- **Desktop target** is not wired yet (the ProvinCompose base ships Android + iOS only).
  Adding it is a `jvm("desktop")` target + a `desktopMain` entrypoint + JVM Ktor/Coil
  engines — planned for the week-5 milestone.
- Launcher icons (`androidMain/res/mipmap-*`) and the iOS `AppIcon` are placeholders
  copied from the base project; swap them for `../shopverse-icons/` assets.
