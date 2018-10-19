package freaktemplate.fooddelivery;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import freaktemplate.Getset.cartgetset;
import freaktemplate.Getset.myOrderGetSet;
import freaktemplate.Getset.placeordergetset;
import freaktemplate.utils.DBAdapter;
import freaktemplate.utils.GPSTracker;

import static freaktemplate.fooddelivery.MainActivity.changeStatsBarColor;
import static freaktemplate.fooddelivery.MainActivity.checkInternet;
import static freaktemplate.fooddelivery.MainActivity.showErrorDialog;
import static freaktemplate.fooddelivery.MainActivity.tf_opensense_regular;

public class PlaceOrder extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerDragListener {
    private double latitudecur;
    private double longitudecur;
    private GoogleMap googleMap;
    String Error;
    private String address;
    private EditText edt_address;
    private static ArrayList<cartgetset> cartlist;
    private ArrayList<placeordergetset> placeorderlist;
    private static final String MyPREFERENCES = "Fooddelivery";
    private String status = "";
    private String allresid;
    private String description;
    private SQLiteDatabase db;
    private String userid;
    private View layout12;
    private EditText edt_note;
    private float total = 0;
    private myOrderGetSet data;
    private static final String MY_PREFS_NAME = "Fooddelivery";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_order);
        changeStatsBarColor(PlaceOrder.this);


        if (checkInternet(PlaceOrder.this)) {


            gettingLocation();

            getSharedPref();

            initialization();


        } else showErrorDialog(PlaceOrder.this);

    }

    private void getSharedPref() {
        SharedPreferences sp = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        userid = sp.getString("userid", "");

    }


    private void gettingLocation() {
        GPSTracker gps = new GPSTracker();
        gps.init(PlaceOrder.this);        // check if GPS enabled
        if (gps.canGetLocation()) {
            try {
                latitudecur = gps.getLatitude();
                longitudecur = gps.getLongitude();
            } catch (NumberFormatException e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        } else {

            gps.showSettingsAlert();
        }

    }

    private void initialization() {

        getSupportActionBar().hide();
        TextView txt_header = findViewById(R.id.txt_title);
        txt_header.setTypeface(tf_opensense_regular);
        edt_address = findViewById(R.id.edt_address);
        edt_address.setTypeface(tf_opensense_regular);
        RelativeLayout rel_main = findViewById(R.id.rel_main);

        edt_note = findViewById(R.id.edt_note);
        edt_note.setTypeface(tf_opensense_regular);

        Button btn_selectlocation = findViewById(R.id.btn_selectlocation);
        btn_selectlocation.setTypeface(tf_opensense_regular);


        Button btn_placeorder = findViewById(R.id.btn_placeorder);
        btn_placeorder.setTypeface(tf_opensense_regular);

        MapsInitializer.initialize(getApplicationContext());
        initilizeMap();

        btn_selectlocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GetDataAsyncTask().execute();


            }
        });

        btn_placeorder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validation();
            }
        });

        placeorderlist = new ArrayList<>();

        //getting Data

        cartlist = new ArrayList<>();
        new getList().execute();


    }

    private void validation() {
        if (edt_address.getText().toString().trim().isEmpty()) {
            edt_address.setError("Select Location");
        } else if (edt_note.getText().toString().trim().isEmpty()) {
            edt_note.setError("Write description!");
        } else {

            address = edt_address.getText().toString().replace(" ", "%20");
            description = edt_note.getText().toString().replace(" ", "%20");

            // new PostDataAsyncTask().execute();

            PostData();

        }

    }


    private void initilizeMap() {

        SupportMapFragment supportMapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment));
        supportMapFragment.getMapAsync(this);
        (findViewById(R.id.mapFragment)).getViewTreeObserver()
                .addOnGlobalLayoutListener(new android.view.ViewTreeObserver.OnGlobalLayoutListener() {

                    @Override
                    public void onGlobalLayout() {

                        (findViewById(R.id.mapFragment)).getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    }
                });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        try {
            // check if map is created successfully or not
            if (googleMap == null) {
                Toast.makeText(getApplicationContext(), getString(R.string.no_map), Toast.LENGTH_SHORT).show();
                return;
            }
            this.googleMap = googleMap;
            googleMap.setOnMarkerDragListener(this);


            initializeUiSettings();
            initializeMapLocationSettings();
            initializeMapTraffic();
            initializeMapType();
            initializeMapViewSettings();

        } catch (Exception e) {
            e.printStackTrace();
        }


        try {
            LatLng position = new LatLng(latitudecur, longitudecur);
            googleMap.addMarker(new MarkerOptions().position(position).draggable(true).title(address));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 17.0f));


        } catch (NullPointerException | NumberFormatException e) {
            // TODO: handle exception
        }

    }

    private void initializeUiSettings() {
        googleMap.getUiSettings().setCompassEnabled(false);
        googleMap.getUiSettings().setRotateGesturesEnabled(false);
        googleMap.getUiSettings().setTiltGesturesEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(false);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
    }

    private void initializeMapLocationSettings() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        googleMap.setMyLocationEnabled(true);
    }

    private void initializeMapTraffic() {
        googleMap.setTrafficEnabled(true);
    }

    private void initializeMapType() {
        googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
    }

    private void initializeMapViewSettings() {
        googleMap.setIndoorEnabled(true);
        googleMap.setBuildingsEnabled(false);
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {
        LatLng field = marker.getPosition();
        System.out.println("LatitudenLongitude:" + field.latitude + " " + field.longitude);
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        LatLng field = marker.getPosition();

        latitudecur = field.latitude;
        longitudecur = field.longitude;


    }

    class GetDataAsyncTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(PlaceOrder.this);
            pd.setMessage(getString(R.string.txt_load));
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected Void doInBackground(Void... params) {

            if (getAddress() != null)
                address = getAddress().get(0).getAddressLine(0);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (pd.isShowing()) {
                pd.dismiss();
            }
            LatLng position = new LatLng(latitudecur, longitudecur);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 17.0f));

            edt_address.setText(address);


        }

    }

    private List<Address> getAddress() {
        if (latitudecur != 0 && longitudecur != 0) {
            try {
                Geocoder geocoder = new Geocoder(PlaceOrder.this);
                List<Address> addresses = geocoder.getFromLocation(latitudecur, longitudecur, 1);
                String address = addresses.get(0).getAddressLine(0);
                String city = addresses.get(0).getAddressLine(1);
                String country = addresses.get(0).getAddressLine(2);
                Log.d("TAG", "address = " + address + ", city = " + city + ", country = " + country);
                return addresses;

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(PlaceOrder.this, R.string.error_lat_long, Toast.LENGTH_LONG).show();
        }
        return null;
    }


    private class getList extends AsyncTask<String, Integer, Integer> {
        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();

        }

        @Override
        protected Integer doInBackground(String... params) {
            // TODO Auto-generated method stub
            DBAdapter myDbHelper;
            myDbHelper = new DBAdapter(PlaceOrder.this);
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
                Cursor cur = db.rawQuery("select * from cart where foodprice >=1;", null);
                Log.e("cartlistingplaceorder", "" + ("select * numberOfRecords cart where foodprice >=1;"));
                Log.d("SIZWA", "" + cur.getCount());
                if (cur.getCount() != 0) {
                    if (cur.moveToFirst()) {
                        do {
                            cartgetset obj = new cartgetset();
                            String resid = cur.getString(cur.getColumnIndex("resid"));
                            String foodid = cur.getString(cur.getColumnIndex("foodname"));
                            String menuid = cur.getString(cur.getColumnIndex("menuid"));
                            String foodname = cur.getString(cur.getColumnIndex("foodname"));
                            String foodprice = cur.getString(cur.getColumnIndex("foodprice"));
                            String fooddesc = cur.getString(cur.getColumnIndex("fooddesc"));
                            String restcurrency = cur.getString(cur.getColumnIndex("restcurrency"));
                            obj.setResid(resid);
                            obj.setFoodid(foodid);
                            obj.setMenuid(menuid);
                            obj.setFoodname(foodname);
                            obj.setFoodprice(foodprice);
                            obj.setFooddesc(fooddesc);
                            obj.setRestcurrency(restcurrency);
                            cartlist.add(obj);
                            try {
                                float quant = Float.parseFloat(foodprice);
                                float single = Float.parseFloat(restcurrency);
                                Log.e("12345", "" + quant + single);
                                float totalsum = quant * single;
                                total = totalsum + total;
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
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

        }
    }


    private void PostData() {
        placeorderlist.clear();
        JSONObject jo_itemId = new JSONObject();
        JSONObject jo_item;
        JSONObject jo_itemQuantity = new JSONObject();
        JSONObject jo_itemPrice = new JSONObject();
        JSONArray jo_detail = null;
        jo_detail = new JSONArray();


        StringBuilder totalOrderInfo = new StringBuilder();
        StringBuilder totalvalue = new StringBuilder();
        StringBuilder totalDetailOrder = new StringBuilder();

        for (int i = 0; i < cartlist.size(); i++) {
            jo_item = new JSONObject();

            allresid = cartlist.get(i).getResid();

            String tempItemName = cartlist.get(i).getFoodname();
            totalvalue.append(tempItemName).append(",");

            String menu_id = "ItemId=" + cartlist.get(i).getMenuid();

            String qty = "/ItemQty=" + cartlist.get(i).getFoodprice();


            float ans1 = Float.parseFloat(cartlist.get(i).getFoodprice());
            float ans2 = Float.valueOf(cartlist.get(i).getRestcurrency().replace("$", ""));
            float ans3 = ans1 * ans2;

            String sprice = "/ItemAmt=" + String.valueOf(ans3);


            String SingleOrderInfo = menu_id + qty + sprice;
            totalDetailOrder.append(" Item Name: ").append(tempItemName).append(" Item Price: ").append(ans1).append(" Item Quantity: ").append(ans2).append(" Total: ").append(ans3).append(";");


            totalOrderInfo.append(SingleOrderInfo).append(",");


            try {
                jo_itemId.put("ItemId", cartlist.get(i).getMenuid());
                jo_itemQuantity.put("ItemQty", cartlist.get(i).getFoodprice());
                jo_itemPrice.put("ItemAmt", String.valueOf(ans3));
                jo_item.put("ItemId", cartlist.get(i).getMenuid());
                jo_item.put("ItemQty", cartlist.get(i).getFoodprice());
                jo_item.put("ItemAmt", String.valueOf(ans3));
                jo_detail.put(jo_item);


            } catch (JSONException e) {
                e.printStackTrace();
            }


        }

        final JSONObject sendDetail = new JSONObject();
        try {
            sendDetail.put("Order", jo_detail);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.e("checking", sendDetail.toString());


        String hp;
        hp = getString(R.string.link) + getString(R.string.servicepath) + "bookorder.php?";

        StringRequest postRequest = new StringRequest(Request.Method.POST, hp, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // response
                Log.d("Response", response);

                try {

                    data = new myOrderGetSet();
                    JSONObject responsedat = new JSONObject(response);
                    String txt_success = responsedat.getString("success");
                    if (txt_success.equals("Order Book Successfully")) {
                        status = txt_success;
                        JSONArray jA_category = responsedat.getJSONArray("order_details");
                        JSONObject cat_detail = jA_category.getJSONObject(0);
                        data.setResName(cat_detail.getString("restaurant_name"));
                        data.setResAddress(cat_detail.getString("restaurant_address"));
                        data.setOrder_total(cat_detail.getString("order_amount"));
                        data.setOrder_id(cat_detail.getString("order_id"));
                        data.setOrder_dateTime(cat_detail.getString("order_date"));

                    } else {
                        status = txt_success;
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }

                updateUI();


            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.Response", error.toString());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", userid);
                params.put("res_id", allresid);
                params.put("address", address);
                params.put("lat", String.valueOf(latitudecur));
                params.put("long", String.valueOf(longitudecur));
                params.put("food_desc", sendDetail.toString());
                params.put("notes", description);
                double roundOff = (double) Math.round(total * 100) / 100;
                params.put("total_price", String.valueOf(roundOff));

                return params;
            }

            @Override
            public String getBodyContentType() {
                return super.getBodyContentType();

            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(postRequest);

    }

    private void updateUI() {
        if (status.equals("Order Book Successfully")) {
            Intent iv = new Intent(PlaceOrder.this, CompleteOrder.class);
            iv.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            iv.putExtra("orderid", data.getOrder_id());
            iv.putExtra("restName", data.getResName());
            iv.putExtra("resAddress", data.getResAddress());
            iv.putExtra("ordertime", data.getOrder_dateTime());
            iv.putExtra("order_amount", data.getOrder_total());
            iv.putExtra("key", "orderplace");

            saveOrderToDatabase();
            startActivity(iv);
            finish();


        } else {
            RelativeLayout rl_back = findViewById(R.id.rl_back);
            if (rl_back == null) {
                Log.d("second", "second");
                RelativeLayout rl_dialoguser = findViewById(R.id.rl_infodialog);
                try {
                    layout12 = getLayoutInflater().inflate(R.layout.addcart, rl_dialoguser, false);
                } catch (Exception e) {
                    // TODO: handle exception
                }

                rl_dialoguser.addView(layout12);
                ImageView img = layout12.findViewById(R.id.imageView);
                img.setImageResource(R.drawable.cancel_icon);
                TextView txt_dia = layout12.findViewById(R.id.txt_dia);
                txt_dia.setText("" + getString(R.string.notbooked));
                Button btn_yes = layout12.findViewById(R.id.btn_yes);
                btn_yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        Intent iv = new Intent(PlaceOrder.this, MainActivity.class);
                        startActivity(iv);
                    }
                });
            }
        }

    }


    private void saveOrderToDatabase() {
        DBAdapter myDbHelper = new DBAdapter(PlaceOrder.this);
        try {
            myDbHelper.createDataBase();
        } catch (IOException io) {
            throw new Error("Unable TO Create DataBase");
        }
        try {
            myDbHelper.openDataBase();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        db = myDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("restaurantAddress", data.getResAddress());
        values.put("restaurantName", data.getResName());
        values.put("orderAmount", data.getOrder_total());
        values.put("orderId", data.getOrder_id());
        values.put("orderTime", data.getOrder_dateTime());


        db.insert("order_detail", null, values);

        myDbHelper.close();

    }

}
