package cafe.adriel.androidaudiorecorder;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Environment;

public class AndroidAudioRecorder {
    public static final String EXTRA_FILE_PATH = "filePath";
    public static final String EXTRA_COLOR = "color";

    private Activity activity;

    private String filePath = Environment.getExternalStorageDirectory() + "/recorded_audio.wav";
    private int color = Color.parseColor("#546E7A");
    private int requestCode = 0;

    private AndroidAudioRecorder(Activity activity) {
        this.activity = activity;
    }

    public static AndroidAudioRecorder with(Activity activity) {
        return new AndroidAudioRecorder(activity);
    }

    public AndroidAudioRecorder setFilePath(String filePath) {
        this.filePath = filePath;
        return this;
    }

    public AndroidAudioRecorder setColor(int color) {
        this.color = color;
        return this;
    }

    public AndroidAudioRecorder setRequestCode(int requestCode) {
        this.requestCode = requestCode;
        return this;
    }

    public void record() {
        Intent intent = new Intent(activity, AudioRecorderActivity.class);
        intent.putExtra(EXTRA_FILE_PATH, filePath);
        intent.putExtra(EXTRA_COLOR, color);
        activity.startActivityForResult(intent, requestCode);
    }

}