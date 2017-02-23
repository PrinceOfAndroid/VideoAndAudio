package com.bdrk.videoandaudio;

import android.app.Application;

import cafe.adriel.androidaudioconverter.AndroidAudioConverter;
import cafe.adriel.androidaudioconverter.callback.ILoadCallback;

/**
 * Created by 5u51_5 on 2016/11/3.
 */
public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        /**
         * 配置AndroidAudioConverter
         */
        AndroidAudioConverter.load(this, new ILoadCallback() {
            @Override
            public void onSuccess() {
                // Great!
            }
            @Override
            public void onFailure(Exception error) {
                // FFmpeg is not supported by device
            }
        });
    }

}
