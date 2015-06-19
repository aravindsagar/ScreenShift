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

#License

Copyright 2014 Aravind Sagar

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use the files in this repository except in compliance with the License.
   You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
