# Sophi Android sample applications

Two hosts demonstrate the same native SDK and merchant-owned cart:

- `sample-compose`: multi-tenant Compose demo, including M&S, cart inspection, identity switching and a rejected-cart rehearsal.
- `sample-xml`: minimal Views/AppCompat M&S integration. Its distinct application ID allows both demos to be installed together.

Both support Android 6 (API 23) and compile against API 36. Use the repository Gradle wrapper and Android Studio's JDK; the current build uses AGP 9.1.1 and Kotlin 2.3.21.

Copy `local.properties.example` to the ignored `local.properties`, supply your Android SDK path and **development** `MNS_API_KEY`, and retain `SOPHI_ENV=dev`. No API key belongs in source control. The Views demo always uses dev; Compose reads `SOPHI_ENV` and defaults to dev. A missing key keeps the assistant closed and displays setup instructions.

```sh
./gradlew :sample-compose:assembleDebug :sample-xml:assembleDebug
./gradlew :sample-compose:testDebugUnitTest
```

The sibling `../Sophi-Android` checkout is substituted by `settings.gradle.kts`, because the historical published alpha does not contain the current API. This is a source-development setup, not evidence that a new SDK version has been published.

`shared/.../MerchantCart.kt` is an in-memory merchant implementation. It validates product and SKU identifiers and positive quantities, accepts a batch atomically, then pushes the accepted snapshot back to Sophi. Checkout opens a local cart screen; no order or payment is created. A real merchant must replace this store with its authenticated cart API and return success only after that API accepts the mutation.
