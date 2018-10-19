package freaktemplate.fooddelivery;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.TimeZone;

import freaktemplate.Adapter.restaurentadapter;
import freaktemplate.Getset.restaurentGetSet;
import freaktemplate.utils.GPSTracker;
import freaktemplate.utils.RecyclerTouchListener;

import static freaktemplate.fooddelivery.MainActivity.changeStatsBarColor;
import static freaktemplate.fooddelivery.MainActivity.checkInternet;
import static freaktemplate.fooddelivery.MainActivity.showErrorDialog;

public class MostRatedRestaurant extends Activity {
    private String CategoryTotal;
    private static final String MY_PREFS_NAME = "Fooddelivery";
    private ProgressDialog pd;
    private double latitudecur;
    private double longitudecur;
    private String Error;
    private ArrayList<restaurentGetSet> restaurentlist;
    private RecyclerView recyclerView;
    private String timezoneID;
    private String Location;
    private int radius;
    private static int pageCount;
    private static int numberOfRecord;
    private restaurentadapter adapter;
    AdRequest adRequest;
    AdView mAdView;
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggestedrestaurant);

        changeStatsBarColor(MostRatedRestaurant.this);


    }

    @Override
    protected void onResume() {
        super.onResume();
        numberOfRecord = getResources().getInteger(R.integer.numberOfRecords);
        pageCount = 1;
        recyclerView = findViewById(R.id.listview);
        restaurentlist = new ArrayList<>();
        timezoneID = TimeZone.getDefault().getID();
        gettingSharedPref();


        if (checkInternet(MostRatedRestaurant.this)) {

            AdShow();
            gettingGPSLocation();

            new GetDataAsyncTask().execute();

            clickEvents();
        } else showErrorDialog(MostRatedRestaurant.this);
    }

    private void clickEvents() {
        ImageButton btn_setting = findViewById(R.id.btn_setting);
        btn_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MostRatedRestaurant.this, SettingPage.class);
                i.putExtra("key", "rated");
                startActivity(i);
            }
        });
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Intent iv = new Intent(MostRatedRestaurant.this, DetailPage.class);
                iv.putExtra("res_id", "" + adapter.moviesList.get(position).getId());
                iv.putExtra("distance", "" + adapter.moviesList.get(position).getDistance());
                startActivity(iv);
                if (getResources().getString(R.string.show_admob_ads).equals("yes")) {

                    if (mInterstitialAd.isLoaded()) {
                        mInterstitialAd.show();

                    }
                }
            }


            @Override
            public void onLongClick(View view, int position) {

            }
        }));
        ImageButton ib_back = findViewById(R.id.ib_back);
        ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }


    private void gettingGPSLocation() {
        GPSTracker gps = new GPSTracker();
        gps.init(MostRatedRestaurant.this);        // check if GPS enabled
        if (gps.canGetLocation()) {
            try {
                latitudecur = gps.getLatitude();
                longitudecur = gps.getLongitude();
                Log.w("Current Location", "Lat: " + latitudecur + "Long: " + longitudecur);
            } catch (NullPointerException | NumberFormatException e) {
                // TODO: handle exception
                e.printStackTrace();
            }

        } else {
            gps.showSettingsAlert();
        }


    }


    private void reset() {
        CategoryTotal = "";
    }

    private void gettingSharedPref() {
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        Location = prefs.getString("CityName", null);
        int noRadius = 100000;
        radius = prefs.getInt("radius", noRadius);
    }

    class GetDataAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            reset();
            pd = new ProgressDialog(MostRatedRestaurant.this);
            pd.setMessage("Loading..");
            pd.setCancelable(true);
            pd.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            URL hp;
            try {
                String user = getString(R.string.link) + getString(R.string.servicepath) + "restaurant_byrate.php?timezone=" + timezoneID + "&lat=" + latitudecur + "&lon=" + longitudecur + "&location=" + Location + "&noofrecords=" + numberOfRecord + "&pageno=" + pageCount + "&radius=" + radius + "&short_by=ratting";
                hp = new URL(user.replace(" ", "%20"));


                Log.e("URLs", "" + hp);
                URLConnection hpCon = hp.openConnection();
                hpCon.connect();
                InputStream input = hpCon.getInputStream();
                Log.d("input", "" + input);
                BufferedReader r = new BufferedReader(new InputStreamReader(input));
                String x;
                x = r.readLine();
                StringBuilder total = new StringBuilder();
                while (x != null) {
                    total.append(x);
                    x = r.readLine();
                }
                JSONArray jObject = new JSONArray(total.toString());
                JSONObject obj = jObject.getJSONObject(0);

                switch (obj.getString("status")) {
                    case "Success":
                        JSONArray jsonArray = obj.getJSONArray("Restaurant_list");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            restaurentGetSet temp = new restaurentGetSet();
                            JSONObject Obj = jsonArray.getJSONObject(i);
                            temp.setId(Obj.getString("id"));
                            temp.setName(Obj.getString("name"));
                            temp.setLat(Obj.getString("lat"));
                            temp.setLon(Obj.getString("lon"));
                            temp.setDistance(Obj.getString("distance"));
                            temp.setOpen_time(Obj.getString("open_time"));
                            temp.setClose_time(Obj.getString("close_time"));
                            temp.setCurrency(Obj.getString("currency"));
                            temp.setDelivery_time(Obj.getString("delivery_time"));
                            temp.setImage(Obj.getString("image"));
                            temp.setRatting(Obj.getString("ratting"));
                            temp.setRes_status(Obj.getString("res_status"));
                            try {
                                JSONArray jCategory = Obj.getJSONArray("Category");
                                String[] temprory = new String[jCategory.length()];
                                for (int j = 0; j < jCategory.length(); j++) {
                                    temprory[j] = jCategory.getString(j);
                                    CategoryTotal += temprory[j];
                                    Log.e("catname12121", "" + CategoryTotal);
                                    temp.setCategory(temprory);

                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            restaurentlist.add(temp);

                        }
                        break;
                    case "Failed":
                        Error = obj.getString("error");
                        break;
                    default:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MostRatedRestaurant.this, R.string.txt_try_later, Toast.LENGTH_SHORT).show();
                            }
                        });
                        break;
                }

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Error = e.getMessage();
            } catch (NullPointerException e) {
                // TODO: handle exception
                Error = e.getMessage();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (pd.isShowing()) {
                pd.dismiss();
            }
            if (Error != null) {

                Toast.makeText(MostRatedRestaurant.this, Error, Toast.LENGTH_SHORT).show();


            } else {
                Log.e("adapter", "" + restaurentlist.size());
                LinearLayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                recyclerView.setLayoutManager(mLayoutManager);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                adapter = new restaurentadapter(recyclerView, MostRatedRestaurant.this, restaurentlist);
                adapter.setOnLoadMoreListener(new restaurentadapter.OnLoadMoreListener() {
                    @Override
                    public void onLoadMore() {
                        pageCount = pageCount + 1;
                        //Load more data for reyclerview
                        new LoadMoreData().execute();

                    }
                });
                recyclerView.setAdapter(adapter);

            }
        }
    }

    class LoadMoreData extends AsyncTask<Void, Void, Void> {
        ArrayList data;

        @Override
        protected Void doInBackground(Void... params) {
            URL hp;
            try {
                String user = getString(R.string.link) + getString(R.string.servicepath) + "restaurant_byrate.php?timezone=" + timezoneID + "&lat=" + latitudecur + "&lon=" + longitudecur + "&location=" + Location + "&noofrecords=" + numberOfRecord + "&pageno=" + pageCount + "&radius=" + radius + "&short_by=ratting";
                hp = new URL(user.replace(" ", "%20"));
                Log.e("URLs", "" + hp);
                URLConnection hpCon = hp.openConnection();
                hpCon.connect();
                InputStream input = hpCon.getInputStream();
                Log.d("input", "" + input);
                BufferedReader r = new BufferedReader(new InputStreamReader(input));
                String x;
                x = r.readLine();
                StringBuilder total = new StringBuilder();
                while (x != null) {
                    total.append(x);
                    x = r.readLine();
                }
                JSONArray jObject = new JSONArray(total.toString());
                JSONObject obj = jObject.getJSONObject(0);
                data = new ArrayList<restaurentGetSet>();


                switch (obj.getString("status")) {
                    case "Success":
                        JSONArray jsonArray = obj.getJSONArray("Restaurant_list");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            restaurentGetSet temp = new restaurentGetSet();
                            JSONObject Obj = jsonArray.getJSONObject(i);
                            temp.setId(Obj.getString("id"));
                            temp.setName(Obj.getString("name"));
                            temp.setLat(Obj.getString("lat"));
                            temp.setLon(Obj.getString("lon"));
                            temp.setDistance(Obj.getString("distance"));
                            temp.setOpen_time(Obj.getString("open_time"));
                            temp.setClose_time(Obj.getString("close_time"));
                            temp.setCurrency(Obj.getString("currency"));
                            temp.setDelivery_time(Obj.getString("delivery_time"));
                            temp.setImage(Obj.getString("image"));
                            temp.setRatting(Obj.getString("ratting"));
                            temp.setRes_status(Obj.getString("res_status"));
                            try {
                                JSONArray jCategory = Obj.getJSONArray("Category");
                                String[] temprory = new String[jCategory.length()];
                                for (int j = 0; j < jCategory.length(); j++) {
                                    temprory[j] = jCategory.getString(j);
                                    CategoryTotal += temprory[j];
                                    Log.e("catname12121", "" + CategoryTotal);
                                }
                                temp.setCategory(temprory);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            data.add(temp);
                        }
                        break;
                    case "Failed":
                        Error = obj.getString("error");


                        break;
                    default:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MostRatedRestaurant.this, "Please try later!", Toast.LENGTH_SHORT).show();
                            }
                        });
                        break;
                }

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Error = e.getMessage();
            } catch (NullPointerException e) {
                // TODO: handle exception
                Error = e.getMessage();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (Error != null) {

                Toast.makeText(MostRatedRestaurant.this, Error, Toast.LENGTH_SHORT).show();


            } else if (data.size() != 0) {
                Log.e("adapter", "" + data.size());
                adapter.setLoaded();
                adapter.addItem(data, restaurentlist.size());
            }
        }
    }

    public void AdShow() {

//        Log.e("Deviceid", Settings.Secure.getString(getActivity().getContentResolver(),Settings.Secure.ANDROID_ID));
        if (getResources().getString(R.string.show_admob_ads).equals("yes")) {
            adRequest = new AdRequest.Builder().build();
            mAdView = (AdView) findViewById(R.id.adView);
            mAdView.loadAd(adRequest);
            mAdView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    mAdView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAdFailedToLoad(int i) {

                    mAdView.loadAd(adRequest);

                }
            });
        }
        if (getResources().getString(R.string.show_admob_ads).equals("yes")) {
            mInterstitialAd = new InterstitialAd(this);
            mInterstitialAd.setAdUnitId(getResources().getString(R.string.admob_insertitial_id));
            mInterstitialAd.loadAd(new AdRequest.Builder().build());
            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    super.onAdClosed();
                    mInterstitialAd.loadAd(new AdRequest.Builder().build());
                }

                @Override
                public void onAdFailedToLoad(int i) {
                    super.onAdFailedToLoad(i);
                    mInterstitialAd.loadAd(new AdRequest.Builder().build());
                    Log.d("InterstitialAd", "onAdFailedToLoad");
                }

                @Override
                public void onAdLeftApplication() {
                    super.onAdLeftApplication();
                    Log.d("InterstitialAd", "onAdLeftApplication");
                }

                @Override
                public void onAdOpened() {
                    super.onAdOpened();
                    Log.d("InterstitialAd", "onAdOpened");
                }

                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    Log.d("InterstitialAd", "onAdLoaded");
                }
            });
        }


    }
}
