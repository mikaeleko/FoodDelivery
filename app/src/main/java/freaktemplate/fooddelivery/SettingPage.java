package freaktemplate.fooddelivery;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import freaktemplate.Adapter.CityListAdapter;
import freaktemplate.Getset.CitylistGetSet;

import static freaktemplate.fooddelivery.MainActivity.changeStatsBarColor;
import static freaktemplate.fooddelivery.MainActivity.tf_opensense_regular;

public class SettingPage extends AppCompatActivity {
    private TextView txt_radius;
    private TextView txt_city;
    private ListView listCity;
    private SeekBar radius_seekBar;
    private int radius;
    private SharedPreferences sp;
    private CityListAdapter adapter;
    private ArrayList<CitylistGetSet> getSet;
    private RelativeLayout rl_distance;
    private final int defaultRadius = 100;
    private final int noRadius = 100000;
    private String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_page);
        getSupportActionBar().hide();

        changeStatsBarColor(SettingPage.this);


        getSharedPref();

        getIntents();

        initView();


    }

    private void getIntents() {
        key = getIntent().getStringExtra("key");

    }

    private void getSharedPref() {
        String MY_PREFS_NAME = "Fooddelivery";
        sp = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        radius = sp.getInt("radius", noRadius);
    }

    private void initView() {


        rl_distance = findViewById(R.id.rl_distance);
        ((TextView) findViewById(R.id.txt_title)).setTypeface(tf_opensense_regular);


        TextView txt_distancetitle = findViewById(R.id.txt_distancetitle);
        txt_distancetitle.setTypeface(tf_opensense_regular);

        txt_radius = findViewById(R.id.txt_radius);
        txt_radius.setTypeface(tf_opensense_regular);

        TextView txt_currentlocationtitle = findViewById(R.id.txt_currentlocationtitle);
        txt_currentlocationtitle.setTypeface(tf_opensense_regular);

        TextView txt_distanceontitle = findViewById(R.id.txt_presenceOn);
        txt_distanceontitle.setTypeface(tf_opensense_regular);

        txt_city = findViewById(R.id.txt_city);
        txt_city.setTypeface(tf_opensense_regular);

        listCity = findViewById(R.id.listCity);

        getCityList();

        SwitchCompat sw_radius_onoff = findViewById(R.id.Sw_radius_onoff);

        ImageButton ib_back = findViewById(R.id.ib_back);
        ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        radius_seekBar = findViewById(R.id.radius_seekBar);
        if (radius == noRadius) {
            radius_seekBar.setProgress(defaultRadius);
            txt_radius.setText(defaultRadius + " KM");
            disableRadiusLayout();

        } else {
            radius_seekBar.setProgress(radius);
            txt_radius.setText(radius + " KM");
            sw_radius_onoff.setChecked(true);

        }

        radius_seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                txt_radius.setText(progress + " KM");
                radius = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                sp.edit().putInt("radius", radius).apply();
            }
        });

        sw_radius_onoff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                if (isChecked) {
                    radius_seekBar.setEnabled(true);
                    AlphaAnimation alpha = new AlphaAnimation(1.0F, 1.0F);
                    alpha.setDuration(0); // Make animation instant
                    alpha.setFillAfter(true); // Tell it to persist after the animation ends
                    rl_distance.startAnimation(alpha);
                    sp.edit().putInt("radius", defaultRadius).apply();

                } else {
                    disableRadiusLayout();
                }

            }
        });

    }

    private void getCityList() {

        //getting the progressbar
        final ProgressBar progressBar = findViewById(R.id.progressBar);

        //making the progressbar visible
        progressBar.setVisibility(View.VISIBLE);

        String hp = getString(R.string.link) + getString(R.string.servicepath) + "/restaurant_city.php";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, hp, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //hiding the progressbar after completion
                progressBar.setVisibility(View.INVISIBLE);
                Log.e("Response", response);
                try {
                    CitylistGetSet temp;
                    JSONObject jo_response = new JSONObject(response);
                    getSet = new ArrayList<>();
                    JSONArray ja_city = jo_response.getJSONArray("city");
                    for (int i = 0; i < ja_city.length(); i++) {
                        temp = new CitylistGetSet();
                        JSONObject cityname = ja_city.getJSONObject(i);
                        String city = cityname.getString("city_name");
                        temp.setName(city);
                        temp.setId(String.valueOf(i));
                        getSet.add(temp);
                    }
                    updateUI();

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
        //creating a request queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        //adding the string request to request queue
        requestQueue.add(stringRequest);
    }

    private void updateUI() {
        adapter = new CityListAdapter(SettingPage.this, getSet, SettingPage.this);
        listCity.setAdapter(adapter);
        txt_city.setText(sp.getString("CityName", getSet.get(0).getName()));
        listCity.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                sp.edit().putString("CityName", getSet.get(position).getName()).apply();
                adapter.notifyDataSetChanged();
                txt_city.setText(sp.getString("CityName", getSet.get(0).getName()));

            }
        });
    }

    private void disableRadiusLayout() {
        AlphaAnimation alpha = new AlphaAnimation(0.3F, 0.3F);
        alpha.setDuration(0); // Make animation instant
        alpha.setFillAfter(true); // Tell it to persist after the animation ends
        rl_distance.startAnimation(alpha);

        int radius1;
        SharedPreferences.Editor editor = sp.edit();
        radius1 = defaultRadius;
        radius_seekBar.setProgress(radius1);
        radius_seekBar.setEnabled(false);
        editor.putInt("radius", noRadius);
        txt_radius.setText(radius1 + " " + "KM");
        editor.apply();

    }


    @Override
    public void onBackPressed() {
        switch (key) {
            case "map": {
                Intent i = new Intent(SettingPage.this, Mapactivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
                startActivity(i);
                break;
            }
            case "rated":
             super.onBackPressed();
                break;
            default: {
                Intent i = new Intent(SettingPage.this, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
                startActivity(i);
                break;
            }
        }
    }
}
