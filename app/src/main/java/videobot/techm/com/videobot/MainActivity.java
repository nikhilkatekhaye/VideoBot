package videobot.techm.com.videobot;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import com.ibm.watson.developer_cloud.visual_recognition.v3.VisualRecognition;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifyImagesOptions;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.VisualClassification;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener{

    private static final String TAG = MainActivity.class.getName();
    private MediaPlayer mMediaPlayer;
    private TextureView videoView;
    private ImageView imageCapture;
    private Button btnCapture;
    private Button btnGetInfo;
    private Bitmap selectedBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        playVideo();
        imageCapture = (ImageView) findViewById(R.id.img_video_captured);
        initButtonCapture();
        initButtonGetInfo();
//        processImage();
    }

    private void initButtonGetInfo() {
        btnGetInfo = (Button) findViewById(R.id.btn_image_info);
        btnGetInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                selectedBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                processImage(byteArray);
                selectedBitmap.recycle();
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initButtonCapture() {
        btnCapture = (Button) findViewById(R.id.btn_capture);
        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedBitmap = videoView.getBitmap();
                imageCapture.setImageBitmap(selectedBitmap);
//                mMediaPlayer.pause();
            }
        });
    }

    public void playVideo(){
        videoView = (TextureView) findViewById(R.id.videoView);
        videoView.setSurfaceTextureListener(this);
        String path = "android.resource://" + getPackageName() + "/" + R.raw.horlicks;

    }

    public void processImage(final byte[] data){
        new Thread(new Runnable() {
            @Override
            public void run() {
                VisualRecognition service = new VisualRecognition(VisualRecognition
                        .VERSION_DATE_2016_05_20);
                service.setApiKey("397baf4487e79254d68d3e45036de6de0bc38b04");

                try {
                    byte[] image = getImageFromAsset("fruitbowl.jpg");
                    ClassifyImagesOptions options = new ClassifyImagesOptions.Builder()
                            .images(data,"fruitbowl.jpg")
                            .build();
                    final VisualClassification result = service.classify(options).execute();
                    Log.i(TAG,result.toString());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),result.toString(),Toast.LENGTH_LONG).show();
                        }
                    });

                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }).start();

    }

    private byte[] getImageFromAsset(String fileName) throws IOException {

        InputStream inputStream = getAssets().open(fileName);
        byte[] buffer = new byte[8192];
        int bytesRead;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
        return output.toByteArray();
    }


    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        Surface surface = new Surface(surfaceTexture);

        try {
            String path = "android.resource://" + getPackageName() + "/" + R.raw.horlicks;
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer
                    .setDataSource(this, Uri.parse(path));
            mMediaPlayer.setSurface(surface);
            mMediaPlayer.setLooping(true);

            // don't forget to call MediaPlayer.prepareAsync() method when you use constructor for
            // creating MediaPlayer
            mMediaPlayer.prepareAsync();
            // Play video when the media source is ready for playback.
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.start();
                }
            });

        } catch (IllegalArgumentException e) {
            Log.d(TAG, e.getMessage());
        } catch (SecurityException e) {
            Log.d(TAG, e.getMessage());
        } catch (IllegalStateException e) {
            Log.d(TAG, e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        super.onDestroy();
        if (mMediaPlayer != null) {
            // Make sure we stop video and release resources when activity is destroyed.
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }
}
