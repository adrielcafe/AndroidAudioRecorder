[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-AndroidAudioRecorder-green.svg?style=true)](https://android-arsenal.com/details/1/4099) [![Release](https://jitpack.io/v/adrielcafe/AndroidAudioRecorder.svg)](https://jitpack.io/#adrielcafe/AndroidAudioRecorder)

# AndroidAudioRecorder

> A fancy audio recorder for Android. It supports `WAV` format at `48kHz`.

![Screenshots](https://raw.githubusercontent.com/adrielcafe/AndroidAudioRecorder/master/demo.gif)

![Screenshots](https://raw.githubusercontent.com/adrielcafe/AndroidAudioRecorder/master/screenshots.png)

## How To Use

1 - Add these permissions into your `AndroidManifest.xml` and [request for them in Android 6.0+](https://developer.android.com/training/permissions/requesting.html)
```xml
<uses-permission android:name="android.permission.RECORD_AUDIO"/>
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.WAKE_LOCK" />
```

2 - Open the recorder activity
```java
String filePath = Environment.getExternalStorageDirectory() + "/recorded_audio.wav";
int color = getResources().getColor(R.color.colorPrimaryDark);
int requestCode = 0;
AndroidAudioRecorder.with(this)
    // Required
    .setFilePath(filePath)
    .setColor(color)
    .setRequestCode(requestCode)
    
    // Optional
    .setSource(AudioSource.MIC)
    .setChannel(AudioChannel.STEREO)
    .setSampleRate(AudioSampleRate.HZ_48000)
    .setAutoStart(true)
    .setKeepDisplayOn(true)
    
    // Start recording
    .record();
```

3 - Wait for result
```java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == 0) {
        if (resultCode == RESULT_OK) {
            // Great! User has recorded and saved the audio file
        } else if (resultCode == RESULT_CANCELED) {
            // Oops! User has canceled the recording
        }
    }
}
```

## Import to your project
Put this into your `app/build.gradle`:
```
repositories {
  maven {
    url "https://jitpack.io"
  }
}

dependencies {
  compile 'com.github.adrielcafe:AndroidAudioRecorder:0.2.0'
}
```

## FEATURES
- [X] Record audio
- [X] Tint images to black when background color is too bright (thanks to [@prakh25](https://github.com/prakh25))
- [X] Wave visualization based on this [player concept](https://dribbble.com/shots/2369760-Player-Concept)
- [X] Play recorded audio
- [X] Pause recording
- [X] Configure audio source (Mic/Camcorder), channel (Stereo/Mono) and sample rate (8kHz to 48kHz)
- [X] Auto start recording when open activity
- [X] Keep display on while recording
- [ ] Skip silence
- [ ] Animations
- [ ] Landscape screen orientation (only supports portrait at the moment)

## Dependencies
* [OmRecorder](https://github.com/kailash09dabhi/OmRecorder)
* [WaveInApp](https://github.com/Cleveroad/WaveInApp)

## Want to CONVERT AUDIO into your app?
**Take a look at [AndroidAudioConverter](https://github.com/adrielcafe/AndroidAudioConverter)! Example of usage [here](https://github.com/adrielcafe/AndroidAudioRecorder/issues/8#issuecomment-247311572).**

## License
```
The MIT License (MIT)

Copyright (c) 2016 Adriel Café

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
```
