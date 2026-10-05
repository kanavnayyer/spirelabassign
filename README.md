# Product Catalog & Offline Cart

Small Android app for the Spire Lab assignment. It loads products from DummyJSON, lets you search, open details, and keep a cart that still works offline.

## Setup

1. Open the project in Android Studio.
2. In `local.properties` add your SDK path and base URL:

```
sdk.dir=<your-sdk-path>
BASE_URL=https://dummyjson.com/
```

## Libraries
| Library | Version | Why |
| --- | --- | --- |
| Kotlin | 2.2.10 | App language |
| Android Gradle Plugin | 9.4.1 | Build |
| Jetpack Compose BOM | 2026.02.01 | UI toolkit versions |
| Compose Material3 | via BOM | Screens / components |
| Compose Material Icons Extended | via BOM | Cart / delete / quantity icons |
| Activity Compose | 1.8.0 | Compose Activity host |
| Lifecycle Runtime / ViewModel Compose | 2.8.7 | ViewModels + lifecycle |
| Navigation Compose | 2.8.9 | Product list → detail → cart |
| Hilt | 2.60.1 | Dependency injection |
| Hilt Navigation Compose | 1.2.0 | `hiltViewModel()` in screens |
| KSP | 2.2.10-2.0.2 | Hilt / Room code gen |
| Retrofit | 2.11.0 | DummyJSON API calls |
| Retrofit Gson converter | 2.11.0 | JSON → Kotlin models |
| OkHttp Logging Interceptor | 4.12.0 | HTTP client / logging |
| Room (runtime + ktx) | 2.7.1 | Offline cart database |
| Coil Compose | 2.7.0 | Product / cart images |
| Kotlinx Coroutines Android | 1.10.2 | Async work + Flow |
| AndroidX Core KTX | 1.10.1 | Kotlin Android helpers |
Gradle catalog: `gradle/libs.versions.toml`  
App deps: `app/build.gradle.kts`


3. Sync Gradle and run the app.

No API key needed. Min SDK is 27.

## What I used

- Kotlin + Jetpack Compose
- MVVM
- Hilt for DI
- Retrofit + OkHttp for API calls
- Room for cart storage
- Coil for images
- Coroutines / Flow
- Navigation Compose

## Architecture

Pretty straightforward MVVM:

`Screen → ViewModel → Repository → API / Room`

- Products come from the network (DummyJSON).
- Cart is saved in Room, so it works without internet and stays after app restart.
- Base URL is read from `local.properties` into BuildConfig (not hardcoded).

## Local storage

Cart uses Room table `cart_items` with product id, title, price, thumbnail, and quantity. Quantity +/- and remove all update Room. Totals are calculated from the saved items.

## Design choices

- Kept the code simple on purpose. No extra layers.
- Loading / empty / error + retry handled on product screens.
- Search waits a bit after typing before calling the API.
- Offline support is for the cart (as asked). Product list/search need network.

## Known limitations

- Product list, search, and details need internet.
- No checkout or login.
- API returns a limited product list (around 30 items).

## API

DummyJSON Products API: https://dummyjson.com/docs/products
