# Sophi Android Sample

Two small apps that show how to integrate the Sophi Android SDK (`com.usesophi:sophi-android`):

| Module | What it shows |
|---|---|
| `sample-compose` | A feature catalog: one screen for each part of the public API, plus a live event log |
| `sample-xml` | The View/XML path: opening Sophi full screen and embedding `SophiChatFragment` |

Both apps use the `Dev` environment, and their screen text is in Turkish.

## Requirements

- Android Studio with Android SDK 36
- JDK 21. The Gradle wrapper (9.3.1) downloads it automatically when it is missing.
- A device or emulator: API 23 or newer for `sample-compose`, API 24 or newer for `sample-xml`
- A Sophi API key for the Dev environment

## Setup

Add your API key to `local.properties` in the project root. The file is ignored by git:

```properties
SOPHI_API_KEY=your-dev-api-key
```

Both modules read the key into `BuildConfig.SOPHI_API_KEY`. If it is missing, the apps still build, but Sophi cannot open a session.

## Run

```bash
./gradlew :sample-compose:installDebug
```

```bash
./gradlew :sample-xml:installDebug
```

The two apps have different application ids, so they can be installed side by side.

The SDK version is set by `sophiAndroid` in `gradle/libs.versions.toml`.

## sample-compose: feature catalog

The home screen lists one entry per feature. Every screen opens with a short description, and its code is meant to be copied.

| Screen | API | File |
|---|---|---|
| Full-screen launch | `Sophi.start`: plain, with `initialMessage`, with `externalProductId`, or with both | `feature/LaunchScreen.kt` |
| Embed in Compose | `SophiChat` under the app's own toolbar: plain, with a message, or with a product | `feature/EmbeddedChatScreen.kt` |
| Appearance | `SophiConfig` presets (default, custom colors and texts, no avatar and no close button) and `Sophi.setLocale` | `feature/AppearanceScreen.kt` |
| Identity | `setExternalUserId`, `reset`, `setUserName`, `setIdentityPersistenceEnabled`, `endSession`, `currentSessionInfo` | `feature/IdentityScreen.kt` |
| Commerce | `updateCart`, `updateFavorite`, a failing cart write, and the app opening the product page | `feature/CommerceScreen.kt` |
| Host commands | `sendTextMessage`, `sendProductReply`, `openProduct`, `openHistory` and `toggleHistory` next to an embedded chat | `feature/HostCommandsScreen.kt` |
| Events | Every `SophiListener` callback, newest first | `feature/EventsScreen.kt` |

These files show how the sample is wired together:

- **`SampleApplication.kt`** calls `SampleSophi.configure` once, at startup.
- **`SampleSophi.kt`** holds the single `Sophi.configure(...)` call, with every parameter written out, and the `SophiListener` that sends each callback to the event log.
- **`SampleState.kt`** holds the settings the screens change, the event log, and the signed-in customer id.

### Integration notes the sample shows

- **Configure with the signed-in customer id every time.**
  - Call `configure` in `Application.onCreate` and pass the id of the customer who is signed in.
  - If the stored identity belongs to a different id, the SDK clears it. A configure without the id therefore signs the shopper out.
  - The sample keeps the id in its own `SharedPreferences` (`SignedInUser`), as a real app keeps its login.
- **Calling `configure` again starts a new runtime.**
  - A chat that is open closes, and `onClose` is called.
  - The `updateCart` snapshot and favorite overrides are dropped. Send them again after configuring.
  - The Appearance screen does this when you change the preset.
- **`onAddToCart` must write to your own cart before it returns.**
  - Return `Result.success` only after the write succeeded.
  - A failure reaches `onError` and blocks checkout until a later add succeeds.
  - Turn on "Sepete yazma başarısız olsun" on the Commerce screen to see this.
- **`onOpenProductPage` returns `true` only when your app opened the page.**
  - When it returns `false`, the SDK opens the page itself.
- **Host commands wait until the chat is ready.**
  - A command sent while an answer is streaming fails with `SophiError.Busy`.
  - The Host commands screen shows the result of each command in a snackbar.
- **`sendOutfitReply` is shown only as a code sample.**
  - It needs a `SophiOutfit` that came from a recommendation, so its button is disabled.

## sample-xml: Views and fragments

`MainActivity` has four buttons:

- **Full screen:** `Sophi.start(context)`
- **With a message:** `Sophi.start(context, initialMessage = ...)`
- **With a product:** `Sophi.start(context, externalProductId = ...)`
- **Embedded:** opens `EmbeddedActivity`

`EmbeddedActivity` places `SophiChatFragment` under a `MaterialToolbar`:

- **Close.** Closing arrives as a fragment result on `SophiChatFragment.REQUEST_KEY_CLOSE`, not as a callback property. It therefore still works after rotation and process death.
- **Add once.** The fragment is added only when `savedInstanceState == null`.
- **Keyboard.**
  - The activity is edge-to-edge and uses `windowSoftInputMode="adjustResize"`.
  - The chat moves itself above the keyboard.
  - The root view pads the bottom by the navigation bar height while the keyboard is closed, and by nothing while it is open.

The Sophi configuration is in `SampleApplication.kt`.
