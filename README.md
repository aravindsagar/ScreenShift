# ScreenShift
A simple app to change screen resolution, density and overscan of Android devices.

Uses the following adb commands before JellyBean 4.3:

    am display-size
    am display-density

From JellyBean 4.3 and up, this has been shifted to wm, with additional overscan option

    wm size
    wm density
    wm overscan

Needs root in JB 4.3 and higher versions.
