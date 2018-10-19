package freaktemplate.fooddelivery;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import static freaktemplate.fooddelivery.MainActivity.changeStatsBarColor;

public class Splash extends AppCompatActivity {
    private static final String MY_PREFS_NAME = "Fooddelivery";
    private boolean isDeliveryAccountActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen
        );
        changeStatsBarColor(Splash.this);


        int SPLASH_TIME_OUT = getResources().getInteger(R.integer.splash_time_out);
        new Handler().postDelayed(new Runnable() {

                                      /*
                                       * Showing splash screen with a timer. This will be useful when you
                                       * want to show case your app logo / company
                                       */

                                      @Override
                                      public void run() {
                                          SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
                                          isDeliveryAccountActive = prefs.getBoolean("isDeliverAccountActive", false);
                                          if (isDeliveryAccountActive) {
                                              Intent iv = new Intent(Splash.this, DeliveryStatus.class);
                                              startActivity(iv);
                                              finish();
                                          } else {
                                                  Intent iv = new Intent(Splash.this, MainActivity.class);
                                                  startActivity(iv);
                                                  finish();

                                          }
                                      }
                                  },
                SPLASH_TIME_OUT);
    }

}

