package cafe.adriel.androidaudiorecorder;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class PlayerManager {

    public interface PlayerManagerPlayCallBack{
        void onPlayFinished();
    }

    private  boolean isPlaying = false;
    private AudioTrack at = null;

    private static final PlayerManager ourInstance = new PlayerManager();

    public static PlayerManager getInstance() {
        return ourInstance;
    }

    private PlayerManager() {
    }

    public void play(final String filePath, final PlayerManagerPlayCallBack playerManagerPlayCallBack){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    isPlaying = true;
                    // just put here your wav file
                    File yourWavFile = new File(filePath);
                    FileInputStream fis = new FileInputStream(yourWavFile);
                    int minBufferSize = AudioTrack.getMinBufferSize(48000,
                            AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);
                    at = new AudioTrack(AudioManager.STREAM_MUSIC, 48000,
                            AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT,
                            minBufferSize, AudioTrack.MODE_STREAM);

                    int i = 0;
                    byte[] music = null;
                    try {

                        music = new byte[512];
                        at.play();

                        while ((i = fis.read(music)) != -1 && isPlaying){
                            at.write(music, 0, i);
                        }


                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    at.stop();
                    at.release();
                    isPlaying = false;
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            playerManagerPlayCallBack.onPlayFinished();
                        }
                    });

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }).start();
    }

    public void stop(){
        if (at == null){
            return;
        }
        isPlaying = false;

    }

    public boolean isPlaying() {
        return isPlaying;
    }
}
