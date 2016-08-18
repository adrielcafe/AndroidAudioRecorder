package cafe.adriel.androidaudiorecorder;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import omrecorder.AudioSource;

public class Util {
    private static final Handler HANDLER = new Handler();
    private static final int AUDIO_FREQUENCY = 44100;

    private Util() {
    }

    public static void requestPermission(Activity activity, String permission) {
        if (ContextCompat.checkSelfPermission(activity, permission)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{permission}, 0);
        }
    }

    public static void wait(int millis, Runnable callback){
        HANDLER.postDelayed(callback, millis);
    }

    public static AudioSource getMic() {
        return new AudioSource.Smart(
                MediaRecorder.AudioSource.MIC,
                AudioFormat.ENCODING_PCM_16BIT,
                AudioFormat.CHANNEL_IN_STEREO,
                AUDIO_FREQUENCY);
    }

    /**
     * Function to check brightness of background color
     * @param color
     * @return true if color is bright
     */
    public static boolean isBrightColor(int color) {
        if(android.R.color.transparent == color) {
            return true;
        }
        int [] rgb = {Color.red(color), Color.green(color), Color.blue(color)};
        int brightness = (int) Math.sqrt(
                rgb[0] * rgb[0] * 0.241 +
                rgb[1] * rgb[1] * 0.691 +
                rgb[2] * rgb[2] * 0.068);
        //color is bright
        return brightness >= 200;
    }

    public static int getDarkerColor(int color) {
        float factor = 0.8f;
        int a = Color.alpha(color);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        return Color.argb(a,
                Math.max((int) (r * factor), 0),
                Math.max((int) (g * factor), 0),
                Math.max((int) (b * factor), 0));
    }

    public static String formatSeconds(int seconds) {
        return getTwoDecimalsValue(seconds / 3600) + ":"
                + getTwoDecimalsValue(seconds / 60) + ":"
                + getTwoDecimalsValue(seconds % 60);
    }

    private static String getTwoDecimalsValue(int value) {
        if (value >= 0 && value <= 9) {
            return "0" + value;
        } else {
            return value + "";
        }
    }

}