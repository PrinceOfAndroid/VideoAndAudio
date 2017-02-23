package com.bdrk.videoandaudio;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * demo的targetSdkVersion 为22 跳过了6.0权限设置(。・_・)/~~~
 */
public class MainActivity extends AppCompatActivity {
    private Button btnVideo;
    private Button btnAudio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initListener();
    }


    private void initView() {
        btnAudio = (Button) findViewById(R.id.btn_audio);
        btnVideo = (Button) findViewById(R.id.btn_video);

    }

    private void initListener() {
        btnVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, RecordVideoActivity.class);
                startActivity(intent);
            }
        });

        btnAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, RecordAudioActivity.class);
                startActivity(intent);
            }
        });
    }
}
