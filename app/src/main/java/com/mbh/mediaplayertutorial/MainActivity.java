package com.mbh.mediaplayertutorial;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    ImageButton ib_play, ib_stop;
    Button btn_next;

    MBMediaPlayer mediaPlayer;

    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        initUI();


        mediaPlayer = new MBMediaPlayer(this);
        mediaPlayer.addAssetFile("dua1.mp3"); // test for assets file
        mediaPlayer.addRawFile(R.raw.dua2); // test for raw file

        mediaPlayer.setOnComplestionListener(onCompletionListener);
    }

    private MBMediaPlayer.OnCompletionListener onCompletionListener = new MBMediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion() {
            ib_play.setImageResource(R.drawable.ic_play);
        }
    };

    private void initUI() {
        ib_play = (ImageButton) findViewById(R.id.btn_play);
        ib_stop = (ImageButton) findViewById(R.id.btn_stop);
        btn_next = (Button) findViewById(R.id.btn_next);

        ib_play.setOnClickListener(this);
        ib_stop.setOnClickListener(this);
        btn_next.setOnClickListener(this);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mediaPlayer != null)
            mediaPlayer.destroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_play:
                if(!mediaPlayer.isPlaying()) {
                    if(mediaPlayer.isPaused())
                        mediaPlayer.playOrPause(false);
                    else mediaPlayer.playAll();
                    ib_play.setImageResource(R.drawable.ic_pause);
                } else {
                    mediaPlayer.pause();
                    ib_play.setImageResource(R.drawable.ic_play);
                }
                break;
            case R.id.btn_stop:
                mediaPlayer.stop();
                ib_play.setImageResource(R.drawable.ic_play);
                break;
            case R.id.btn_next:
                mediaPlayer.next();
                ib_play.setImageResource(R.drawable.ic_pause);
                break;
            default:
                break;
        }
    }
}
