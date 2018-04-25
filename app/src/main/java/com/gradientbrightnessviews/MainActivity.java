package com.gradientbrightnessviews;

import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.gradientbrightnessviews.views.GradientBar;
import com.gradientbrightnessviews.views.GradientSeekBar;

public class MainActivity extends AppCompatActivity {

    GradientBar gradientBar;
    GradientSeekBar gradientSeekBar;
    int progress = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gradientBar = findViewById(R.id.gradientBar);
        gradientSeekBar = findViewById(R.id.arcGradient);

//        new CountDownTimer(30000, 500){
//            @Override
//            public void onTick(long millisUntilFinished) {
//                gradientBar.setProgress(progress);
//                gradientSeekBar.setProgress(progress);
//                progress++;
//                if(progress > gradientBar.getMaxProgress()){
//                    progress = 0;
//                }
//            }
//
//            @Override
//            public void onFinish() {
//
//            }
//        }.start();
    }
}
