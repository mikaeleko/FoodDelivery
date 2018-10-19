package freaktemplate.fooddelivery;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.TextView;

import static freaktemplate.fooddelivery.MainActivity.changeStatsBarColor;
import static freaktemplate.fooddelivery.MainActivity.tf_opensense_regular;

public class Aboutus extends Activity {

    private ImageButton ib_back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aboutus);
        setAboutUscontent();
        changeStatsBarColor(Aboutus.this);
        ib_back = findViewById(R.id.ib_back);
         ib_back.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 onBackPressed();
             }
         });
        ((TextView)findViewById(R.id.txt_header)).setTypeface(tf_opensense_regular);


    }

    private void setAboutUscontent() {
        WebView web = findViewById(R.id.web);
        web.loadUrl("file:///android_asset/"+getString(R.string.aboutus_filename));

    }

}
