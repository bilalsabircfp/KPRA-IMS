package com.dzone.kpra_app;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;



/**
 * Created by Bilal on 24-Jan-19.
 */
public class Splash extends Activity {

    Intent myintent;
    ImageView imageView;
    SharedPreferences sharedPreferences;
    TextView textView,t2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        imageView = (ImageView) findViewById(R.id.donut_progress);
        textView  = (TextView) findViewById(R.id.text);

        PropertyValuesHolder donutAlphaProperty    = PropertyValuesHolder.ofFloat("alpha", 0f, 1f);
        PropertyValuesHolder donutProgressProperty = PropertyValuesHolder.ofInt("donut_progress", 0, 100);
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(imageView, donutAlphaProperty, donutProgressProperty);
        animator.setDuration(3000);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.start();

        animator = ObjectAnimator.ofPropertyValuesHolder(textView, donutAlphaProperty, donutProgressProperty);
        animator.setDuration(3000);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.start();

    }


    @Override
    protected void onResume() {
        super.onResume();

        //In onresume fetching value from sharedpreference
        sharedPreferences = getSharedPreferences("DataStore", Context.MODE_PRIVATE);

        if (sharedPreferences.getString("name", "").equals("")) {


            myintent = new Intent(this, Login.class);

            new Handler().postDelayed(new Runnable(){
                @Override
                public void run() {
                    startActivity(myintent);
                    finish();
                }
            }, 4000);


        } else {

//            Toast.makeText(this, "Data Found", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            this.finish();
        }
    }
}
