package freaktemplate.fooddelivery;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareOpenGraphAction;
import com.facebook.share.model.ShareOpenGraphContent;
import com.facebook.share.model.ShareOpenGraphObject;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;

import freaktemplate.Getset.CustomMarker;
import freaktemplate.Getset.cartgetset;
import freaktemplate.Getset.detailgetset;
import freaktemplate.Getset.favgetset;
import freaktemplate.utils.DBAdapter;
import freaktemplate.utils.GPSTracker;

import static freaktemplate.fooddelivery.MainActivity.changeStatsBarColor;
import static freaktemplate.fooddelivery.MainActivity.checkInternet;
import static freaktemplate.fooddelivery.MainActivity.showErrorDialog;
import static freaktemplate.fooddelivery.MainActivity.tf_opensense_medium;

public class DetailPage extends AppCompatActivity implements OnMapReadyCallback {
    private static ArrayList<detailgetset> detaillist;
    private static ArrayList<cartgetset> cartlist;
    private ArrayList<favgetset> favlist;
    private ProgressDialog progressDialog;
    private double latitudecur;
    private double longitudecur;
    private final int start = 0;
    private String res_id;
    private ImageButton btn_fav;
    private ImageButton btn_fav1;
    private DBAdapter myDbHelpel;
    private SQLiteDatabase db;
    private Cursor cur = null;
    View layout;
    private Button btn_share;
    String id;
    private String name;
    private String category;
    private String timing;
    private String rating;
    private String distance;
    private String image;
    private String restaurent_id;
    private String address;
    private String CategoryTotal = "";
    private CollapsingToolbarLayout collapsingToolbar;
    private String distancenew;
    private GoogleMap googleMap;
    private CallbackManager callbackManager;
    private ShareDialog shareDialog;
    private final String tag = "fb";
    private HashMap<CustomMarker, Marker> markersHashMap;

    private static double roundMyData(double Rval, int numberOfDigitsAfterDecimal) {
        double p = (float) Math.pow(10, numberOfDigitsAfterDecimal);
        Rval = Rval * p;
        double tmp = Math.floor(Rval);
        System.out.println("~~~~~~tmp~~~~~" + tmp);
        return tmp / p;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_page);

        changeStatsBarColor(DetailPage.this);


        initializations();

        getIntents();

        if (checkInternet(DetailPage.this)) {
            gettingGPSLocation();
            getData();

        } else
            showErrorDialog(DetailPage.this);


    }

    private void gettingGPSLocation() {
        GPSTracker gps = new GPSTracker();
        gps.init(DetailPage.this);        // check if GPS enabled
        if (gps.canGetLocation()) {
            try {
                latitudecur = gps.getLatitude();
                longitudecur = gps.getLongitude();
                Log.w("Current Location", "Lat: " + latitudecur + "Long: " + longitudecur);
            } catch (NullPointerException | NumberFormatException e) {
                // TODO: handle exception
                Log.e("Error", e.getMessage());
            }

        } else {
            gps.showSettingsAlert();
        }


    }

    private void initializations() {

        //toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //initialization
        collapsingToolbar = findViewById(R.id.collapsing_toolbar);
        btn_share = findViewById(R.id.btn_share);
        btn_fav = findViewById(R.id.btn_fav);
        btn_fav1 = findViewById(R.id.btn_fav1);


    }

    private void getIntents() {
        //getting intents
        Intent iv = getIntent();
        res_id = iv.getStringExtra("res_id");
        distancenew = iv.getStringExtra("distance");
    }

    private void getData() {
        //getting data
        detaillist = new ArrayList<>();
        cartlist = new ArrayList<>();
        favlist = new ArrayList<>();

        if (checkInternet(DetailPage.this))
            new GetDataAsyncTask().execute();
        else showErrorDialog(DetailPage.this);


        //fav button onclick
        onClickFavourite();
        //facebook share callbacks
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(DetailPage.this);
        shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
                Toast.makeText(DetailPage.this, "You shared this post", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel() {
                Log.e(tag, "cancelled");
            }

            @Override
            public void onError(FacebookException error) {
                Log.e(tag, error.toString());
            }
        });


    }

    private void onClickFavourite() {
        btn_fav.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                final detailgetset temp_Obj3 = detaillist.get(start);
                btn_fav1.setVisibility(View.VISIBLE);
                btn_fav.setVisibility(View.INVISIBLE);
                // data store in database of favorite store
                myDbHelpel = new DBAdapter(DetailPage.this);
                try {
                    myDbHelpel.createDataBase();
                } catch (IOException io) {
                    throw new Error("Unable TO Create DataBase");
                }
                try {
                    myDbHelpel.openDataBase();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                db = myDbHelpel.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put("restaurent_id", temp_Obj3.getId());
                values.put("name", temp_Obj3.getName());
                values.put("category", temp_Obj3.getCategory());
                values.put("timing", temp_Obj3.getTime());
                values.put("rating", temp_Obj3.getRatting());
                values.put("distance", distancenew);
                values.put("image", temp_Obj3.getPhoto());
                values.put("address", temp_Obj3.getAddress());
                db.insert("Favourite", null, values);
                Log.e("inserted values", values.toString());
                myDbHelpel.close();
            }
        });
        // on click of this Favourite button store will be unfavourite
        btn_fav1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                btn_fav.setVisibility(View.VISIBLE);
                btn_fav1.setVisibility(View.INVISIBLE);
                final detailgetset temp_Obj3 = detaillist.get(start);
                // remove record of store numberOfRecords database to unfavourite
                DBAdapter myDbHelper;
                myDbHelper = new DBAdapter(DetailPage.this);
                try {
                    myDbHelper.createDataBase();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    myDbHelper.openDataBase();

                } catch (SQLException sqle) {
                    sqle.printStackTrace();
                }
                db = myDbHelper.getWritableDatabase();
                cur = db.rawQuery("Delete from favourite where restaurent_id =" + temp_Obj3.getId() + ";", null);
                Log.e("deletedvalues", "" + ("Delete numberOfRecords Favourite where id =" + temp_Obj3.getId() + ";"));
                if (cur.getCount() != 0) {
                    if (cur.moveToFirst()) {
                        do {
                            favgetset obj = new favgetset();
                            restaurent_id = cur.getString(cur.getColumnIndex("restaurent_id"));
                            name = cur.getString(cur.getColumnIndex("name"));
                            category = cur.getString(cur.getColumnIndex("category"));
                            timing = cur.getString(cur.getColumnIndex("timing"));
                            rating = cur.getString(cur.getColumnIndex("rating"));
                            distance = cur.getString(cur.getColumnIndex("distance"));
                            image = cur.getString(cur.getColumnIndex("image"));
                            address = cur.getString(cur.getColumnIndex("address"));
                            obj.setName(name);
                            obj.setCategory((category));
                            obj.setTiming(timing);
                            obj.setRating(rating);
                            obj.setDistance(distance);
                            obj.setImage(image);
                            obj.setId(restaurent_id);
                            obj.setAddress(address);
                            favlist.add(obj);
                        } while (cur.moveToNext());
                    }
                }
                cur.close();
                db.close();
                myDbHelper.close();
            }
        });
    }

    private Uri getLocalBitmapUri(ImageView imageView) {
        // Extract Bitmap numberOfRecords ImageView drawable
        Drawable drawable = imageView.getDrawable();
        Bitmap bmp;
        if (drawable instanceof BitmapDrawable) {
            bmp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        } else {
            return null;
        }
        // Store image to default external storage directory
        Uri bmpUri = null;
        try {
            File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    "share_image_" + System.currentTimeMillis() + ".png");
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            bmpUri = FileProvider.getUriForFile(DetailPage.this, BuildConfig.APPLICATION_ID + ".provider", file);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }

    private void getlist() {
        DBAdapter myDbHelper = new DBAdapter(getApplicationContext());
        try {
            myDbHelper.createDataBase();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            myDbHelper.openDataBase();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
        db = myDbHelper.getReadableDatabase();
        try {
            cur = db.rawQuery("delete  from cart ;", null);
            Log.e("deletdetail_pagedata", "delete numberOfRecords cart ;");
            if (cur.getCount() != 0) {
                if (cur.moveToFirst()) {
                    do {
                        cartgetset obj = new cartgetset();
                        String resid = cur.getString(cur.getColumnIndex("resid"));
                        String menuid321 = cur.getString(cur.getColumnIndex("menuid"));
                        String foodid = cur.getString(cur.getColumnIndex("foodid"));
                        String foodname = cur.getString(cur.getColumnIndex("foodname"));
                        String foodprice321 = cur.getString(cur.getColumnIndex("foodprice"));
                        String fooddesc = cur.getString(cur.getColumnIndex("fooddesc"));
                        obj.setResid(resid);
                        obj.setFoodid(foodid);
                        obj.setMenuid(menuid321);
                        obj.setFoodname(foodname);
                        obj.setFooddesc(fooddesc);
                        Log.e("menuid321", menuid321);
                        Log.e("foodp321", "" + foodprice321);
                        cartlist.add(obj);
                    } while (cur.moveToNext());
                }
            }
            cur.close();
            db.close();
            myDbHelper.close();
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        double latitude = 0, longitude = 0;
        try {
            String lat = detaillist.get(0).getLat();
            String lon = detaillist.get(0).getLon();
            latitude = Double.parseDouble(lat);
            longitude = Double.parseDouble(lon);
        } catch (NumberFormatException e) {
            Log.e("Error", e.getMessage());
            Toast.makeText(DetailPage.this, getString(R.string.later_txt), Toast.LENGTH_SHORT).show();
            // TODO: handle exception
        }
        afterMapReady(latitude, longitude);

    }

    private void afterMapReady(double latitude, double longitude) {
        LatLng position = new LatLng(latitude, longitude);
        CustomMarker customMarkerOne = new CustomMarker("markerOne", latitude, longitude);
        try {
            MarkerOptions markerOption = new MarkerOptions().position(

                    new LatLng(customMarkerOne.getCustomMarkerLatitude(), customMarkerOne.getCustomMarkerLongitude()))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
                    .title(detaillist.get(0).getName());

            Marker newMark = googleMap.addMarker(markerOption);

            addMarkerToHashMap(customMarkerOne, newMark);

            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 17));
        } catch (Exception e1) {
            Toast.makeText(DetailPage.this, getString(R.string.later_txt), Toast.LENGTH_SHORT).show();

        }
    }

    private void addMarkerToHashMap(CustomMarker customMarker, Marker marker) {
        setUpMarkersHashMap();
        markersHashMap.put(customMarker, marker);
    }

    private void setUpMarkersHashMap() {
        if (markersHashMap == null) {
            markersHashMap = new HashMap<>();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

    }

    class GetDataAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(DetailPage.this);
            progressDialog.setMessage(getString(R.string.loading));
            progressDialog.setCancelable(true);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            URL hp;
            String error1;
            try {
                detaillist.clear();
                hp = new URL(getString(R.string.link) + getString(R.string.servicepath) + "getrestaurantdetail.php?res_id=" + res_id + "&lat=" + latitudecur + "&" + "lon=" + longitudecur);
                Log.e("URLdetail", "" + hp);
                URLConnection hpCon = hp.openConnection();
                hpCon.connect();
                InputStream input = hpCon.getInputStream();
                BufferedReader r = new BufferedReader(new InputStreamReader(input));
                String x;
                x = r.readLine();
                StringBuilder total = new StringBuilder();
                while (x != null) {
                    total.append(x);
                    x = r.readLine();
                }
                Log.e("URL", "" + total);

                JSONArray jObject = new JSONArray(total.toString());
                Log.d("URL12", "" + jObject);
                JSONObject Obj1;
                Obj1 = jObject.getJSONObject(0);
                switch (Obj1.getString("status")) {
                    case "Success":
                        JSONObject jsonO = Obj1.getJSONObject("Restaurant_Detail");
                        detailgetset temp = new detailgetset();
                        temp.setId(jsonO.getString("id"));
                        temp.setName(jsonO.getString("name"));
                        temp.setAddress(jsonO.getString("address"));
                        temp.setTime(jsonO.getString("time"));
                        temp.setDelivery_time(jsonO.getString("delivery_time"));
                        temp.setCurrency(jsonO.getString("currency"));
                        temp.setPhoto(jsonO.getString("photo"));
                        temp.setPhone(jsonO.getString("phone"));
                        temp.setLat(jsonO.getString("lat"));
                        temp.setLon(jsonO.getString("lon"));
                        temp.setDesc(jsonO.getString("desc"));
                        temp.setEmail(jsonO.getString("email"));
                        temp.setLocation(jsonO.getString("address"));
                        temp.setRatting(jsonO.getString("ratting"));
                        temp.setRes_status(jsonO.getString("res_status"));
                        temp.setDelivery_charg(jsonO.getString("delivery_charg"));
                        temp.setDistance(jsonO.getString("distance"));
                        temp.setCategory(jsonO.getString("Category"));
                        String catname = jsonO.getString("Category");
                        Log.e("catname", "" + catname);
                        CategoryTotal = CategoryTotal + catname;
                        detaillist.add(temp);
                        Log.e("detaillist", detaillist.get(0).getName());
                        break;
                    case "Failed":
                        final String error = Obj1.getString("Error");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(DetailPage.this, error, Toast.LENGTH_SHORT).show();
                            }
                        });
                        break;
                    default:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(DetailPage.this, "Please try again later!", Toast.LENGTH_SHORT).show();
                            }
                        });
                        break;
                }

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                error1 = e.getMessage();
            } catch (NullPointerException e) {
                // TODO: handle exception
                error1 = e.getMessage();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
            collapsingToolbar.setTitle("");
            collapsingToolbar.setExpandedTitleTextAppearance(R.style.ExpandedAppBar);
            collapsingToolbar.setNestedScrollingEnabled(false);

            Typeface tf_opensense_regular = Typeface.createFromAsset(DetailPage.this.getAssets(), "fonts/OpenSans-Regular.ttf");

            //initialize
            AppBarLayout appBarLayout = findViewById(R.id.appbar);
            TextView txt_addresstitle = findViewById(R.id.txt_addresstitle);
            TextView txt_phonenumber = findViewById(R.id.txt_phonenumber);
            TextView txt_timingtitle = findViewById(R.id.txt_timingtitle);
            TextView txt_foodtitle = findViewById(R.id.txt_foodtitle);
            TextView txt_deliverytitle = findViewById(R.id.txt_deliverytitle);
            TextView txt_deliverytypetitle = findViewById(R.id.txt_deliverytypetitle);
            final TextView txt_title = findViewById(R.id.txt_title);
            TextView txt_addressdesc = findViewById(R.id.txt_addressdesc);
            TextView txt_descnumber = findViewById(R.id.txt_descnumber);
            TextView txt_timingdesc = findViewById(R.id.txt_timingdesc);
            TextView txt_fooddesc = findViewById(R.id.txt_fooddesc);
            TextView txt_deliverydesc = findViewById(R.id.txt_deliverydesc);
            TextView txt_deliverytypedesc = findViewById(R.id.txt_deliverytypedesc);
            final RatingBar rb = findViewById(R.id.rate);
            final TextView txt_ratenumber = findViewById(R.id.txt_ratenumber);
            TextView txt_distance = findViewById(R.id.txt_distance);
            final ImageView imageview = findViewById(R.id.img_detail);
            TextView txt_description = findViewById(R.id.txt_description);


            //setting typeface

            txt_addresstitle.setTypeface(tf_opensense_medium);
            txt_phonenumber.setTypeface(tf_opensense_medium);
            txt_timingtitle.setTypeface(tf_opensense_medium);
            txt_foodtitle.setTypeface(tf_opensense_medium);
            txt_deliverytitle.setTypeface(tf_opensense_medium);
            txt_deliverytypetitle.setTypeface(tf_opensense_medium);
            txt_title.setTypeface(tf_opensense_regular);
            txt_addressdesc.setTypeface(tf_opensense_regular);
            txt_descnumber.setTypeface(tf_opensense_regular);
            txt_addressdesc.setTypeface(tf_opensense_regular);
            txt_fooddesc.setTypeface(tf_opensense_regular);
            txt_deliverydesc.setTypeface(tf_opensense_regular);
            txt_deliverytypedesc.setTypeface(tf_opensense_regular);
            txt_description.setTypeface(tf_opensense_regular);

            //setting data
            if (detaillist.size() > 0) {
                txt_title.setText((detaillist.get(0).getName()));
                txt_addressdesc.setText((detaillist.get(0).getAddress()));
                txt_descnumber.setText((detaillist.get(0).getPhone()));
                txt_timingdesc.setText((detaillist.get(0).getTime()));
                if (CategoryTotal != null) {
                    String category = CategoryTotal.replace("[", "").replace("]", "").replace("\"", "").replace(",",", ");
                    txt_fooddesc.setText(category);
                }
                txt_deliverydesc.setText((detaillist.get(0).getDelivery_time()));
                txt_deliverytypedesc.setText((detaillist.get(0).getDelivery_charg()));
                rb.setRating(Float.parseFloat(detaillist.get(0).getRatting()));
                rb.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                    @Override
                    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                        // Auto-generated
                        Log.d("rate", "" + rating);
                    }
                });
                txt_ratenumber.setText("" + Float.parseFloat(detaillist.get(0).getRatting()));
                txt_ratenumber.setTypeface(tf_opensense_regular);
                final String image = detaillist.get(0).getPhoto().replace(" ", "%20");
                Picasso.with(DetailPage.this)
                        .load(getString(R.string.link) + getString(R.string.imagepath) + image)
                        .into(imageview);
                Log.e("Image", getString(R.string.link) + getString(R.string.imagepath) + image);


                appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
                    @Override
                    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                        if (collapsingToolbar.getHeight() + verticalOffset < 2 * ViewCompat.getMinimumHeight(collapsingToolbar)) {
                            //      btn_share.animate().alpha(1).setDuration(600);
                            txt_ratenumber.animate().alpha(0).setDuration(600);
                            rb.animate().alpha(0).setDuration(600);
                            //       txt_name.animate().alpha(0).setDuration(600);
                        } else {
                            txt_ratenumber.animate().alpha(1).setDuration(600);
                            rb.animate().alpha(1).setDuration(600);
                            //      txt_name.animate().alpha(1).setDuration(600);
                            //   btn_share.animate().alpha(0).setDuration(600);
                            txt_title.animate().alpha(1).setDuration(600);
                            collapsingToolbar.setTitle("");
                        }
                    }
                });
                txt_distance.setTypeface(tf_opensense_regular);
                try {
                    double numbar = roundMyData(Double.parseDouble(distancenew), 1);
                    txt_distance.setText("" + numbar + " "+getResources().getString(R.string.km));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }

                txt_description.setText(detaillist.get(0).getDesc());

            }
            //adding map support

            SupportMapFragment mapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment));
            mapFragment.getMapAsync(DetailPage.this);

            //listeners after getting detail

            Button btn_call = findViewById(R.id.btn_call);
            btn_call.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String call = detaillist.get(0).getPhone();
                    String uri = "tel:" + call;
                    Intent i = new Intent(Intent.ACTION_DIAL);
                    i.setData(Uri.parse(uri));
                    startActivity(i);
                }
            });

            Button btn_map = findViewById(R.id.btn_map);
            btn_map.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final detailgetset temp_Obj3 = detaillist.get(start);

                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://maps.google.com/maps?saddr=" + latitudecur + "," + longitudecur + "&daddr=" + temp_Obj3.getLat() + "," + temp_Obj3.getLon()));
                    intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                    startActivity(intent);
                }
            });

            Button btn_menu = findViewById(R.id.btn_menu);
            btn_menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    getlist();
//                    Intent iv = new Intent(DetailPage.this, MenuList.class);
//                    iv.putExtra("detail_id", "" + res_id);
//                    iv.putExtra("restaurent_name", "" + detaillist.get(0).getName());
//                    startActivity(iv);
                    String call = detaillist.get(0).getPhone();
                    String uri = "tel:" + call;
                    Intent i = new Intent(Intent.ACTION_DIAL);
                    i.setData(Uri.parse(uri));
                    startActivity(i);
                }

            });

            Button btn_review = findViewById(R.id.btn_review);
            btn_review.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Intent iv = new Intent(DetailPage.this, Review.class);
                    iv.putExtra("detail_id", "" + detaillist.get(0).getId());
                    iv.putExtra("name", "" + detaillist.get(0).getName());
                    iv.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(iv);
                }
            });

            btn_share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    AlertDialog alertDialog = new AlertDialog.Builder(DetailPage.this, R.style.MyDialogTheme).create();
                    alertDialog.setTitle(getString(R.string.share));
                    alertDialog.setMessage(getString(R.string.sharetitle));
                    // share on gmail,hike etc
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.more),
                            new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int id) {
                                    try {
                                        String urlToShare = "Name:" + detaillist.get(0).getName() + "\n";
                                        Uri bmpUri = getLocalBitmapUri(imageview);
                                        Intent share = new Intent(Intent.ACTION_SEND);
                                        share.setType("text/plain");
                                        share.setType("image/*");
                                        share.setType("image/jpeg");
                                        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                        share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                                        share.putExtra(Intent.EXTRA_SUBJECT, "Restaurant");
                                        share.putExtra("android.intent.extra.TEXT", urlToShare);
                                        share.putExtra(Intent.EXTRA_STREAM, bmpUri);
                                        startActivity(Intent.createChooser(share, "Share link!"));
                                    } catch (NullPointerException e) {
                                        // TODO: handle exception
                                    }
                                }
                            });

                    // share on whatsapp

                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.whatsapp),
                            new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int id) {

                                    String urlToShare = "Name:" + detaillist.get(0).getName() + "\n";
                                    Uri bmpUri = getLocalBitmapUri(imageview);
                                    Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
                                    whatsappIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    whatsappIntent.putExtra(Intent.EXTRA_TEXT, urlToShare);
                                    whatsappIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
                                    whatsappIntent.setType("text/plain");
                                    whatsappIntent.setType("image/*");
                                    whatsappIntent.setPackage("com.whatsapp");
                                    //startActivity(whatsappIntent);
                                    try {
                                        startActivity(whatsappIntent);
                                        // Detailpage.this.startActivity(whatsappIntent);

                                    } catch (android.content.ActivityNotFoundException ex) {
                                        Toast.makeText(DetailPage.this, "Whatsapp have not been installed.", Toast.LENGTH_LONG)
                                                .show();
                                    }

                                }
                            });

                    // share on facebook

                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.facebook),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                    //fb share integrate
                                    String imageurl = getString(R.string.link) + getString(R.string.imagepath) + image;
                                    String url1 = "https://www.google.com";
                                    String urlToShare = detaillist.get(0).getDesc();

                                    ShareOpenGraphObject object = new ShareOpenGraphObject.Builder()
                                            .putString("fb:app_id", getString(R.string.facebook_app_id))
                                            .putString("og:type", "article")
                                            .putString("og:url", url1)
                                            .putString("og:title", detaillist.get(0).getName())
                                            .putString("og:image", imageurl)
                                            .putString("og:description", urlToShare).build();
                                    // Create an action
                                    ShareOpenGraphAction action = new ShareOpenGraphAction.Builder()
                                            .setActionType("news.publishes")
                                            .putObject("article", object)
                                            .build();

                                    // Create the content
                                    ShareOpenGraphContent contentn = new ShareOpenGraphContent.Builder()
                                            .setPreviewPropertyName("article").setAction(action)
                                            .build();

                                    Log.e("check", object.getBundle().toString() + " " + action.getBundle().toString());
                                    shareDialog.show(contentn, ShareDialog.Mode.AUTOMATIC);

                                }
                            });
                    alertDialog.show();
                }
            });


            //checking Favourite
            new getfavlist().execute();
        }


    }

    private class getfavlist extends AsyncTask<String, Integer, Integer> {

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();

        }

        @Override
        protected Integer doInBackground(String... params) {
            // TODO Auto-generated method stub
            favlist.clear();
            DBAdapter myDbHelper;
            myDbHelper = new DBAdapter(DetailPage.this);
            try {
                myDbHelper.createDataBase();
            } catch (IOException e) {

                e.printStackTrace();
            }
            try {
                myDbHelper.openDataBase();
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            }
            int i = 1;
            db = myDbHelper.getReadableDatabase();
            try {
                cur = db.rawQuery("select * from Favourite where restaurent_id=" + detaillist.get(0).getId() + ";", null);


                Log.e("alreadyfav", "select * numberOfRecords Favourite where restaurent_id=" + detaillist.get(0).getId() + ";");
                if (cur.getCount() != 0) {
                    if (cur.moveToFirst()) {
                        do {
                            favgetset obj = new favgetset();
                            restaurent_id = cur.getString(cur.getColumnIndex("restaurent_id"));
                            name = cur.getString(cur.getColumnIndex("name"));
                            category = cur.getString(cur.getColumnIndex("category"));
                            timing = cur.getString(cur.getColumnIndex("timing"));
                            rating = cur.getString(cur.getColumnIndex("rating"));
                            distance = cur.getString(cur.getColumnIndex("distance"));
                            image = cur.getString(cur.getColumnIndex("image"));
                            address = cur.getString(cur.getColumnIndex("address"));
                            obj.setName(name);
                            obj.setCategory((category));
                            obj.setTiming(timing);
                            obj.setRating(rating);
                            obj.setDistance(distance);
                            obj.setImage(image);
                            obj.setId(restaurent_id);
                            obj.setAddress(address);
                            favlist.add(obj);
                        } while (cur.moveToNext());
                    }
                }
                cur.close();
                db.close();
                myDbHelper.close();
            } catch (Exception e) {
                // TODO: handle exception
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (favlist.size() == 0) {
                btn_fav.setVisibility(View.VISIBLE);
                btn_fav1.setVisibility(View.INVISIBLE);
            } else {

                btn_fav1.setVisibility(View.VISIBLE);
                btn_fav.setVisibility(View.INVISIBLE);
            }

        }
    }
}
