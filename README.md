# Sophi Android sample applications

Two hosts demonstrate the native chat and event SDKs with the same merchant-owned cart:

- `sample-compose`: multi-tenant Compose demo, including M&S, cart inspection, identity switching and a rejected-cart rehearsal.
- `sample-xml`: minimal Views/AppCompat M&S integration. Its distinct application ID allows both demos to be installed together.

Both support Android 6 (API 23) and compile against API 36. Use the repository Gradle wrapper and Android Studio's JDK; the current build uses AGP 9.1.1 and Kotlin 2.3.21.

For real-tenant testing, copy `local.properties.example` to the ignored `local.properties`, supply your Android SDK path and **development** `MNS_API_KEY`, and retain `SOPHI_ENV=dev`. No API key belongs in source control. The Views demo always uses dev; Compose reads `SOPHI_ENV` and defaults to dev. A missing real-tenant key keeps that tenant closed. The default local demo needs no credentials.

```sh
./gradlew :sample-compose:assembleDebug :sample-xml:assembleDebug
./gradlew :sample-compose:testDebugUnitTest
```

The sibling `../Sophi-Android` and `../Sophi-Event-Android` checkouts are substituted by `settings.gradle.kts`, because the historical published alpha does not contain the current API. This is a source-development setup, not evidence that a new SDK version has been published.

`shared/.../MerchantCart.kt` is an in-memory merchant implementation. It validates product and SKU identifiers and positive quantities, accepts a batch atomically, then pushes the accepted snapshot back to Sophi. Checkout opens a local cart screen; no order or payment is created. A real merchant must replace this store with its authenticated cart API and return success only after that API accepts the mutation.

## Combined local demo

From the management workspace:

```sh
python3 mobile/programs/mobile-event-sdk/demos/run.py android
python3 mobile/programs/mobile-event-sdk/demos/run.py android-views
```

The launcher starts one loopback HTTPS fixture, generates ignored local TLS resources, builds and opens the chosen host. Use API 24+ for this local certificate flow; the native SDK/application floor remains API 23 for services using normal trusted HTTPS. A plain checkout builds with system roots when generated fixture resources are absent. `-PsophiDemoLocalTls=false` explicitly selects that normal trust configuration. No TLS verification is bypassed.

Both hosts share `DemoTracking.kt`, `DemoEventsActivity.kt` and `MerchantCart.kt`. The Events screen includes all ten event types, explicit analytics permission, a delivery-outage switch, pending/received counts and optional shopper sign-in/sign-out. Analytics starts disabled; its host preference survives process restart. Business cart operations are atomic, a rejected write emits no event, and event failure cannot undo a successful merchant action. Baskets and purchases retain merchant variant IDs.

Send “Show shirts” in chat and add a recommended product to the demo cart. Use **Reject next cart write** to exercise a rejection. **Complete demo order** records a single synthetic purchase and clears the cart; it never charges or submits a merchant order. Guest-to-account linking keeps the guest cart, while registered account changes and logout clear it. Consent/login/logout callbacks are optional examples for integrators; anonymous collection needs only initial permission and event calls.

The fixture never forwards requests. Its received count is local acceptance, not production ingestion. For outage testing, keep the server running, create an event during the simulated outage, terminate/reopen the app, then restore delivery and wait for the retry delay. The native SDK owns the durable queue and event IDs; the host cart itself is in-memory.

The full launcher, troubleshooting and verification guide is `mobile/programs/mobile-event-sdk/DEMOS.md` in the management workspace. Nothing is published by these scripts.
