package freaktemplate.fooddelivery;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.TypefaceSpan;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.login.LoginManager;
import com.gitonway.lee.niftymodaldialogeffects.lib.Effectstype;
import com.gitonway.lee.niftymodaldialogeffects.lib.NiftyDialogBuilder;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.messaging.FirebaseMessaging;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

import freaktemplate.Adapter.restaurentadapter;
import freaktemplate.Getset.CitylistGetSet;
import freaktemplate.Getset.restaurentGetSet;
import freaktemplate.utils.Config;
import freaktemplate.utils.ConnectionDetector;
import freaktemplate.utils.GPSTracker;
import freaktemplate.utils.RecyclerTouchListener;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static freaktemplate.utils.Config.SHARED_PREF;

public class MainActivity extends AppCompatActivity {

    private static final String MY_PREFS_NAME = "Fooddelivery";
    private static ArrayList<restaurentGetSet> restaurentlist;
    private String Error;
    private RecyclerView recyclerView;
    private String timezoneID;
    private String search = "";
    private String res_name;
    private ProgressDialog progressDialog;
    private ProgressDialog pd;
    private double latitudecur = 0;
    private double longitudecur = 0;
    private TextView txt_nameuser;
    private TextView txt_profile;
    private RelativeLayout rel_main;
    private String Location;
    private ImageView img_profile;
    private String CategoryTotal = "", regId;
    private SwipyRefreshLayout mSwipeRefreshLayout;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private restaurentadapter adapter;
    private static int pageCount;
    public static int numberOfRecord;
    private String subCategoryName;
    public static Typeface tf_opensense_regular;
    public static Typeface tf_opensense_medium;
    private SharedPreferences prefs;
    private int radius;
    private final int PERMISSION_REQUEST_CODE = 1001;
    private final String[] permission_location = {android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    private ArrayList<CitylistGetSet> getSet;
    AdRequest adRequest;
    String userId1, DeliveryBoyId;
    AdView mAdView;
    private InterstitialAd mInterstitialAd;

    public static boolean checkInternet(Context context) {
        // TODO Auto-generated method stub
        ConnectionDetector cd = new ConnectionDetector(context);
        return cd.isConnectingToInternet();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        setContentView(R.layout.activity_main);

        Log.e("calculate", doubleToDegree(1.4320961841646465));

        //generate key hash for facebook
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);

            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.e("MY KEY HASH:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }


        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        String currentLocation = prefs.getString("CityName", null);

        if (currentLocation == null) {
            getCityName();
        }

        if (checkPermission()) {
            gettingGPSLocation();
        } else requestPermission();

        changeStatsBarColor(MainActivity.this);

        gettingSharedPref();

        gettingIntent();

        initializations();

        settingActionBar();

        clickEvents();

    }

    private void getCityName() {


        String hp = getString(R.string.link) + getString(R.string.servicepath) + "restaurant_city.php";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, hp, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //hiding the progressbar after completion
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

                    SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                    editor.putString("CityName", getSet.get(0).getName());
                    editor.apply();

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


    private void gettingIntent() {
        Intent i = getIntent();
        if (i.getStringExtra("sub_category_name") != null) {
            subCategoryName = i.getStringExtra("sub_category_name");
        }
        if (i.getStringExtra("sub_category_id") != null) {
            String subCategoryId = i.getStringExtra("sub_category_id");
        }
    }


    private void initializations() {

        recyclerView = findViewById(R.id.listview);
        mSwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        rel_main = findViewById(R.id.rel_main);
        numberOfRecord = getResources().getInteger(R.integer.numberOfRecords);
        pageCount = 1;

        //setting fonts
        font();
        SharedPreferences prefs1 = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        userId1 = prefs1.getString("userid", null);

        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        if (pref.getString("regId", null) != null) {

            regId = pref.getString("regId", null);

            //Registering device id to server

            new RegisterMobile().execute();

        } else {
            BroadcastReceiver mRegistrationBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    // checking for type intent filter
                    if (Objects.equals(intent.getAction(), Config.REGISTRATION_COMPLETE)) {
                        // gcm successfully registered
                        // now subscribe to `global` topic to receive app wide notifications
                        FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL);
                        displayFirebaseRegId();
                    } else if (Objects.equals(intent.getAction(), Config.PUSH_NOTIFICATION)) {
                        // new push notification is received
                        String message = intent.getStringExtra("message");
                        Toast.makeText(getApplicationContext(), "notification: " + message, Toast.LENGTH_LONG).show();
                    }
                }
            };
        }


        //getting intents numberOfRecords Location.class
        timezoneID = TimeZone.getDefault().getID();

        Intent iv = getIntent();
        String res_id = iv.getStringExtra("res_id");
        res_name = iv.getStringExtra("res_name");
        String manualadd = iv.getStringExtra("manualadd");

        Log.e("res_id", res_id + res_name);
        Log.e("manualadd", "" + manualadd);
        AdShow();

        if (checkInternet(MainActivity.this))
        //Getting Data
        {
            restaurentlist = new ArrayList<>();
            new GetDataAsyncTask().execute();
        } else showErrorDialog(MainActivity.this);

    }

    private void gettingSharedPref() {
        prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        Location = prefs.getString("CityName", null);
        int noRadius = 100000;
        radius = prefs.getInt("radius", noRadius);

        String regId = getSharedPreferences(SHARED_PREF, MODE_PRIVATE).getString("regId", null);
    }

    private void displayFirebaseRegId() {

        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        String regId = pref.getString("regId", null);
        Log.e("fireBaseRid", "Firebase Reg id: " + regId);

        new RegisterMobile().execute();
    }

    private void settingActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            setupDrawer();
            drawer();
            SpannableString s;
            if (prefs.getString("CityName", null) == null) {
                s = new SpannableString(getString(R.string.txt_home_header));
                s.setSpan(new TypefaceSpan("OpenSans-Regular.ttf"), 0, s.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                s = new SpannableString(prefs.getString("CityName", null));
                s.setSpan(new TypefaceSpan("OpenSans-Regular.ttf"), 0, s.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            actionBar.setTitle(s);

        }

    }

    public static void showErrorDialog(Context c) {
        final NiftyDialogBuilder material;
        material = NiftyDialogBuilder.getInstance(c);
        material.withTitle(c.getString(R.string.text_warning))
                .withMessage(c.getString(R.string.internet_check_error))
                .withDialogColor(c.getString(R.string.colorErrorDialog))
                .withButton1Text("OK").setButton1Click(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                material.cancel();
            }
        })
                .withDuration(1000)
                .withEffect(Effectstype.Fadein)
                .show();
    }

    private void gettingGPSLocation() {
        GPSTracker gps = new GPSTracker();
        gps.init(MainActivity.this);
        // check if GPS enabled
        if (gps.canGetLocation()) {
            try {
                latitudecur = gps.getLatitude();
                longitudecur = gps.getLongitude();
                Log.w("Current Location", "Lat: " + latitudecur + "Long: " + longitudecur);
            } catch (NullPointerException | NumberFormatException e) {
                // TODO: handle exception
            }

        } else {
            gps.showSettingsAlert();
        }


    }


    private void clickEvents() {

        EditText edit_search = findViewById(R.id.edit_search);
        edit_search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH && checkInternet(MainActivity.this)) {
                    Error = null;
                    reset();
                    restaurentlist.clear();
                    pageCount = 1;
                    new GetDataAsyncTasksearch().execute();
                    return true;

                }
                return false;
            }
        });

        // search on home page method
        edit_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) { // TODO
                search = s.toString();
                if (search.length() == 0) {
                    if (checkInternet(MainActivity.this)) {
                        Error = null;
                        reset();
                        pageCount = 1;
                        restaurentlist.clear();
                        new GetDataAsyncTask().execute();
                    }
                }
            }
        });


        mSwipeRefreshLayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                if (search.length() == 0) {
                    Error = null;
                    restaurentlist.clear();
                    reset();
                    pageCount = 1;
                    new GetDataAsyncTask().execute();
                    mSwipeRefreshLayout.setRefreshing(false);
                    recyclerView.scrollToPosition(restaurentlist.size() - 1);
                } else {
                    Error = null;
                    restaurentlist.clear();
                    reset();
                    pageCount = 1;
                    new GetDataAsyncTasksearch().execute();
                    mSwipeRefreshLayout.setRefreshing(false);
                }

            }
        });
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Intent iv = new Intent(MainActivity.this, DetailPage.class);
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
    }

    private void reset() {
        CategoryTotal = "";
    }

    private void font() {

        Typeface tf_worksans = Typeface.createFromAsset(MainActivity.this.getAssets(), "fonts/WorkSans-Regular.otf");
        tf_opensense_regular = Typeface.createFromAsset(MainActivity.this.getAssets(), "fonts/OpenSans-Regular.ttf");
        tf_opensense_medium = Typeface.createFromAsset(MainActivity.this.getAssets(), "fonts/OpenSans-Semibold.ttf");
        TextView txt_search = findViewById(R.id.txt_search);
        txt_search.setTypeface(tf_worksans);
        TextView txt_rated = findViewById(R.id.txt_rated);
        txt_rated.setTypeface(tf_worksans);
        TextView txt_suggested = findViewById(R.id.txt_suggested);
        txt_suggested.setTypeface(tf_worksans);
//        TextView txt_cusine = findViewById(R.id.txt_cusine);
//        txt_cusine.setTypeface(tf_worksans);
        TextView txt_fav = findViewById(R.id.txt_fav);
        txt_fav.setTypeface(tf_worksans);
        TextView txt_share = findViewById(R.id.txt_share);
        txt_share.setTypeface(tf_worksans);
        TextView txt_terms = findViewById(R.id.txt_terms);
        txt_terms.setTypeface(tf_worksans);
        TextView txt_aboutus = findViewById(R.id.txt_aboutus);
        txt_aboutus.setTypeface(tf_worksans);
        TextView txt_logout = findViewById(R.id.txt_logout);
        txt_logout.setTypeface(tf_worksans);
        txt_nameuser = findViewById(R.id.txt_nameuser);
        txt_nameuser.setTypeface(tf_opensense_regular);
        txt_profile = findViewById(R.id.txt_profile);
        txt_profile.setTypeface(tf_worksans);
        img_profile = findViewById(R.id.img_profile);


    }

    private void drawer() {
        LinearLayout ll_fav = findViewById(R.id.ll_fav);
        LinearLayout ll_share = findViewById(R.id.ll_share);
        final LinearLayout ll_aboutus = findViewById(R.id.ll_aboutus);
        final LinearLayout ll_terms = findViewById(R.id.ll_terms);
//        LinearLayout ll_cusine = findViewById(R.id.ll_cusine);
        LinearLayout ll_search = findViewById(R.id.ll_search);
        LinearLayout ll_rated = findViewById(R.id.ll_rated);
        LinearLayout ll_suggested = findViewById(R.id.ll_suggested);
        LinearLayout ll_signout = findViewById(R.id.ll_signout);
        LinearLayout ll_notification = findViewById(R.id.ll_notification);

        ll_fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iv = new Intent(MainActivity.this, Favourite.class);
                startActivity(iv);
            }
        });
        ll_notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iv = new Intent(MainActivity.this, MyOrderPage.class);
                startActivity(iv);
            }
        });

        ll_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url1 = "https://play.google.com/store/apps/details?id=" + MainActivity.this.getPackageName();
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, "Food Delivery");
                intent.putExtra(android.content.Intent.EXTRA_TEXT, url1);
                startActivity(Intent.createChooser(intent, "Share Food Delivery with"));
            }
        });


        ll_aboutus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iv = new Intent(MainActivity.this, Aboutus.class);
                startActivity(iv);

            }
        });

        ll_terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent iv = new Intent(MainActivity.this, Termcondition.class);
                startActivity(iv);
            }
        });


//        ll_cusine.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent iv = new Intent(MainActivity.this, Category.class);
//                startActivity(iv);
//            }
//        });

        ll_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iv = new Intent(MainActivity.this, MainActivity.class);
                startActivity(iv);
            }
        });


        ll_rated.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iv = new Intent(MainActivity.this, Mapactivity.class);
                startActivity(iv);
            }
        });


        ll_suggested.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iv = new Intent(MainActivity.this, MostRatedRestaurant.class);
                startActivity(iv);
            }
        });


        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        if (prefs.getString("userid", null) != null)


        {
            String userId = prefs.getString("userid", null);
            String image = prefs.getString("imagepath", null);
            String profileimage = prefs.getString("imageprofile", null);

            Log.e("image121", "" + profileimage);
            if (Objects.equals(userId, "delete")) {
                ll_signout.setVisibility(View.GONE);
                txt_nameuser.setText(R.string.txt_signin);
                txt_profile.setText(R.string.txt_profile);
                txt_nameuser.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent iv = new Intent(MainActivity.this, Login.class);
                        startActivity(iv);
                    }
                });
            } else


            {
                String uname = prefs.getString("username", null);
                try {
                    Picasso.with(getApplicationContext())
                            .load(profileimage)
                            .into(img_profile);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }

                ll_signout.setVisibility(View.VISIBLE);
                ll_signout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create(); // Read
                        // Update
                        alertDialog.setTitle("Log Out?");
                        alertDialog.setMessage(getString(R.string.error_logout));
                        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Continue..", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // here you can add functions
                                if (LoginManager.getInstance() != null) {
                                    LoginManager.getInstance().logOut();
                                }
                                String prodel = "delete";
                                String userid = "delete";
                                SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                                editor.putString("delete", "" + prodel);
                                editor.putString("userid", "" + userid);
                                editor.apply();
                                Intent iv = new Intent(MainActivity.this, MainActivity.class);
                                startActivity(iv);
                            }
                        });

                        alertDialog.show();
                        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.black));

                    }


                });
                txt_nameuser.setText(uname);
                txt_profile.setText(R.string.txt_profile);
                txt_profile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent iv = new Intent(MainActivity.this, Profile.class);
                        startActivity(iv);
                    }
                });
            }
        } else {
            ll_signout.setVisibility(View.GONE);
            txt_profile.setText(R.string.txt_profile);
            txt_nameuser.setText(R.string.txt_signin);
            txt_nameuser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent iv = new Intent(MainActivity.this, Login.class);
                    startActivity(iv);
                }
            });
        }

    }

    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                drawerView.setEnabled(true);
                recyclerView.setClickable(false);
                recyclerView.setFocusable(false);

            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                view.setEnabled(false);
                rel_main.setEnabled(true);
                rel_main.setVisibility(View.VISIBLE);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        if (mDrawerToggle != null)
            mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mDrawerToggle != null)
            mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_setting) {
            Intent iv = new Intent(MainActivity.this, SettingPage.class);
            iv.putExtra("key", "main");
            startActivity(iv);
            return true;
        }

        // Activate the navigation drawer toggle
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);

    }

    @Override
    public void onBackPressed() {

        if (mDrawerLayout.isDrawerOpen(Gravity.START))
            mDrawerLayout.closeDrawer(Gravity.START);
        else {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this, R.style.MyDialogTheme);
            builder1.setTitle(getString(R.string.Quit));
            builder1.setMessage(getString(R.string.statementquit));
            builder1.setCancelable(true);
            builder1.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    finishAffinity();
                }
            });
            builder1.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
            AlertDialog alert11 = builder1.create();
            alert11.show();
        }
    }


    class GetDataAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            reset();
            pd = new ProgressDialog(MainActivity.this);
            pd.setMessage("Loading..");
            pd.setCancelable(true);
            pd.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            URL hp;
            try {
                if (subCategoryName == null)

                {
                    if (res_name == null) {
                        String user = getString(R.string.link) + getString(R.string.servicepath) + "restaurantlist.php?timezone=" + timezoneID + "&" + "lat=" + latitudecur + "&" + "lon=" + longitudecur + "&location=" + Location + "&noofrecords=" + numberOfRecord + "&pageno=" + pageCount + "&radius=" + radius;
                        hp = new URL(user.replace(" ", "%20"));

                    } else {
                        hp = new URL(getString(R.string.link) + getString(R.string.servicepath) + "restaurantlist.php?timezone=" + timezoneID + "&" + "lat=" + latitudecur + "&lon=" + longitudecur + "&search=" + res_name + "&noofrecords=" + numberOfRecord + "&pageno=" + pageCount + "&radius=" + radius);
                    }
                } else {
                    String user = getString(R.string.link) + getString(R.string.servicepath) + "restaurantlist.php?timezone=" + timezoneID + "&" + "lat=" + latitudecur + "&" + "lon=" + longitudecur + "&search=" + subCategoryName + "&noofrecords=" + numberOfRecord + "&pageno=" + pageCount + "&radius=" + radius;
                    hp = new URL(user.replace(" ", "%20"));
                }


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
                                Toast.makeText(MainActivity.this, R.string.txt_try_later, Toast.LENGTH_SHORT).show();
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

                Toast.makeText(MainActivity.this, Error, Toast.LENGTH_SHORT).show();


            } else {
                Log.e("adapter", "" + restaurentlist.size());
                LinearLayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                recyclerView.setLayoutManager(mLayoutManager);
                recyclerView.setItemAnimator(new DefaultItemAnimator());


                adapter = new restaurentadapter(recyclerView, MainActivity.this, restaurentlist);

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

                if (search.length() == 0) {
                    if (subCategoryName == null)

                    {
                        if (res_name == null) {
                            String user = getString(R.string.link) + getString(R.string.servicepath) + "restaurantlist.php?timezone=" + timezoneID + "&" + "lat=" + latitudecur + "&" + "lon=" + longitudecur + "&location=" + Location + "&noofrecords=" + numberOfRecord + "&pageno=" + pageCount + "&radius=" + radius;
                            hp = new URL(user.replace(" ", "%20"));

                        } else {
                            hp = new URL(getString(R.string.link) + getString(R.string.servicepath) + "restaurantlist.php?timezone=" + timezoneID + "&" + "lat=" + latitudecur + "&lon=" + longitudecur + "&search=" + res_name + "&noofrecords=" + numberOfRecord + "&pageno=" + pageCount + "&radius=" + radius);
                        }
                    } else {
                        String user = getString(R.string.link) + getString(R.string.servicepath) + "restaurantlist.php?timezone=" + timezoneID + "&" + "lat=" + latitudecur + "&" + "lon=" + longitudecur + "&search=" + subCategoryName + "&noofrecords=" + numberOfRecord + "&pageno=" + pageCount + "&radius=" + radius;
                        hp = new URL(user.replace(" ", "%20"));
                    }
                } else {
                    hp = new URL(getString(R.string.link) + getString(R.string.servicepath) + "restaurantlist.php?timezone=" + timezoneID + "&" + "lat=" + latitudecur + "&" + "lon=" + longitudecur + "&" + "search=" + search + "&noofrecords=" + numberOfRecord + "&pageno=" + pageCount + "&radius=" + radius);
                }


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
                                Toast.makeText(MainActivity.this, "Please try later!", Toast.LENGTH_SHORT).show();
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

                Toast.makeText(MainActivity.this, Error, Toast.LENGTH_SHORT).show();


            } else if (data.size() != 0) {
                Log.e("adapter", "" + data.size());
                adapter.setLoaded();
                adapter.addItem(data, restaurentlist.size());
            }
        }
    }

    class GetDataAsyncTasksearch extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage(getString(R.string.loading));
            progressDialog.setCancelable(true);
            progressDialog.show();
            reset();
        }

        @Override
        protected Void doInBackground(Void... params) {
            URL hp;
            try {


                String Usersearch = getString(R.string.link) + getString(R.string.servicepath) + "restaurantlist.php?timezone=" + timezoneID + "&" + "lat=" + latitudecur + "&" + "lon=" + longitudecur + "&" + "search=" + search + "&noofrecords=" + numberOfRecord + "&pageno=" + pageCount + "&radius=" + radius;
                hp = new URL(Usersearch.replace(" ", "%20"));
                Log.e("URLsearch", "" + hp);
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
                Log.d("URL", "" + total);
                JSONArray jObject = new JSONArray(total.toString());
                JSONObject obj = jObject.getJSONObject(0);

                switch (obj.getString("status")) {
                    case "Success":
                        restaurentlist.clear();
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
                            JSONArray jCategory = Obj.getJSONArray("Category");
                            String[] temprory = new String[jCategory.length()];
                            for (int j = 0; j < jCategory.length(); j++) {
                                temprory[j] = jCategory.getString(j);
                                CategoryTotal = CategoryTotal + temprory[j];
                                Log.e("catname12121", "" + CategoryTotal);
                            }
                            temp.setCategory(temprory);
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
                                Toast.makeText(MainActivity.this, "Please try later!", Toast.LENGTH_SHORT).show();
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

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if (Error != null) {
                Toast.makeText(MainActivity.this, Error, Toast.LENGTH_SHORT).show();
            } else {
                adapter = new restaurentadapter(recyclerView, MainActivity.this, restaurentlist);
                adapter.setOnLoadMoreListener(new restaurentadapter.OnLoadMoreListener() {
                    @Override
                    public void onLoadMore() {
                        pageCount = pageCount + 1;
                        new LoadMoreData().execute();
                    }
                });
                recyclerView.setAdapter(adapter);
            }


        }

    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, permission_location, PERMISSION_REQUEST_CODE);
    }

    public static void changeStatsBarColor(Activity activity) {
        Window window = activity.getWindow();

        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        // finally change the color
        window.setStatusBarColor(ContextCompat.getColor(activity, R.color.my_statusbar_color));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent i = new Intent(MainActivity.this, MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(i);
                    finish();
                } else requestPermission();
            }
        }
    }

    public static String doubleToDegree(double value) {
        int degree = (int) value;
        double rawMinute = Math.abs((value % 1.0d) * 60.0d);
        Log.e("Raw min", " " + rawMinute);
        String s1 = String.format("%d\u00b0 %d\u2032 %d\u2033", new Object[]{Integer.valueOf(degree), Integer.valueOf((int) rawMinute), Integer.valueOf((int) Math.round((rawMinute % 1.0d) * 60.0d))});
        return "\u03b1 = " + s1 + "\n" + "2\u03b1 = " + findSum(Integer.valueOf((int) Math.round((rawMinute % 1.0d) * 60.0d)), Integer.valueOf((int) rawMinute), degree);
    }

    public static String findSum(int second, int minute, int degree) {
        int s = (second + second) % 60;
        int m = ((((second + second) / 60) + minute) + minute) % 60;
        int d = (((minute + minute) / 60) + degree) + degree;
        return String.format(Locale.ENGLISH, "%d\u00b0 %d\u2032 %d\u2033", new Object[]{Integer.valueOf(d), Integer.valueOf(m), Integer.valueOf(s)});
    }

    public class RegisterMobile extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... params) {
            URL hp;
            try {

                http:
//192.168.1.116/freak/FoodDeliverySystem/api/token.php?token=1253aaa&type=ios&user_id=3
                hp = new URL(getString(R.string.link) + getString(R.string.servicepath) + "token.php?token=" + regId + "&type=android&user_id=" + userId1 + "&delivery_boyid=null");
                Log.d("URL", "" + hp);

                // URL Connection

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
                Log.d("URL", "" + total);

                // Json Parsing

                JSONObject jObject = new JSONObject(total.toString());
                Log.d("URL12", "" + jObject);


            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();

            } catch (NullPointerException e) {
                // TODO: handle exception
                e.printStackTrace();

            } catch (MalformedURLException e) {
                e.printStackTrace();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);


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
