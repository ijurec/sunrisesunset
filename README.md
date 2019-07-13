## Sunrise Sunset

This android application provides sunrise and sunset information for the current location. You can also change the date and do a search for any city of your choice.

## How to

To start you must go to https://developers.google.com/places/android-sdk/get-api-key to obtain an API key, then go to SunriseSunset/app/src/main/cpp/native-lib.cpp and copy the key to NewStringUTF("") of the return statement. The NDK was used to protect against reverse engineering.

Here are some screenshots:

![screenshot_current_location](https://user-images.githubusercontent.com/6454932/61153948-70ec7c00-a4f5-11e9-95c4-6fa0b0ca84f7.png)

![screenshot_lviv_location](https://user-images.githubusercontent.com/6454932/61153950-734ed600-a4f5-11e9-98a7-7c2b01c50a38.png)