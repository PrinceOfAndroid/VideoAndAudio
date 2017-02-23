package com.bdrk.videoandaudio;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by 5u51_5 on 2017/2/7.
 */

public class MyToast {
    private static Toast toast;

    public static void showToast(Context context, String content) {
        if (toast == null) {
            toast = Toast.makeText(context, content, Toast.LENGTH_SHORT);
        } else {
            toast.setText(content);
        }
        toast.show();
    }

}
