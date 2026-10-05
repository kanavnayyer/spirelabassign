# Product Catalog & Offline Cart

Small Android app for the Spire Lab assignment. It loads products from DummyJSON, lets you search, open details, and keep a cart that still works offline.

## Setup

1. Open the project in Android Studio.
2. In `local.properties` add your SDK path and base URL:

```
sdk.dir=<your-sdk-path>
BASE_URL=https://dummyjson.com/
```

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
