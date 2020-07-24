package cafe.adriel.androidaudiorecorder.model;

import android.media.MediaRecorder;

public enum AudioSource {
    MIC,
    CAMCORDER;

    public int getSource(){
        if (this == AudioSource.CAMCORDER) {
            return MediaRecorder.AudioSource.CAMCORDER;
        }
        return MediaRecorder.AudioSource.MIC;
    }
}