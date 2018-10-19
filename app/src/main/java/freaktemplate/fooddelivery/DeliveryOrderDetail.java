package freaktemplate.fooddelivery;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MIME;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import freaktemplate.Adapter.DeliveryAdapter;
import freaktemplate.Adapter.OrderDetailAdapter;
import freaktemplate.Getset.DeliveryGetSet;
import freaktemplate.Getset.deliveryorderdetailgetset;
import freaktemplate.Getset.orderDetailGetSet;
import freaktemplate.utils.GPSTracker;

import static freaktemplate.fooddelivery.MainActivity.tf_opensense_medium;
import static freaktemplate.fooddelivery.MainActivity.tf_opensense_regular;

public class DeliveryOrderDetail extends AppCompatActivity {
    private ListView list_order;
    private static final String MY_PREFS_NAME = "Fooddelivery";
    private String orderNo ,orderAmount,orderItem,orderDate,orderStatus,orderPayment,orderName,orderAddress,orderContact,orderLat,orderLon,responseStr,postSuccess,isPick;
    private ProgressDialog progressDialog;
    Button btn_picked;
    private ArrayList<orderDetailGetSet> getsetDeliveryorderdetail;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_order_detail);
        Objects.requireNonNull(getSupportActionBar()).hide();

        gettingIntents();
        getData();
    }

    private void gettingIntents() {
        Intent i = getIntent();
        orderNo = i.getStringExtra("OrderNo");
        orderStatus = i.getStringExtra("isComplete");

    }


    private void initViews() {


        TextView txt_header = findViewById(R.id.txt_header);
        txt_header.setTypeface(tf_opensense_regular);
        txt_header.setText("Order No " + orderNo);

        ImageView img_user = findViewById(R.id.img_user);
        if (orderStatus.equals("Order is processing")) {
            img_user.setImageDrawable(getDrawable(R.drawable.img_orderprocess));
        }
        else if (orderStatus.equals("Order is out for delivery"))
        {
            img_user.setImageDrawable(getDrawable(R.drawable.img_orderprocess));

        }
        else if(orderStatus.equals("Order is Delivered"))
        {
            img_user.setImageDrawable(getDrawable(R.drawable.img_ordercomplete));

        }

        TextView txt_orderAmount = findViewById(R.id.txt_orderAmount);
        txt_orderAmount.setTypeface(tf_opensense_medium);
        txt_orderAmount.setText("Order Amount $ " + orderAmount);

        TextView txt_orderQuantity = findViewById(R.id.txt_orderQuantity);
        txt_orderQuantity.setTypeface(tf_opensense_regular);
        txt_orderQuantity.setText(orderItem + " Items");

        TextView txt_orderPaymentStyle = findViewById(R.id.txt_orderPaymentStyle);
        txt_orderPaymentStyle.setTypeface(tf_opensense_regular);
        String pay_text = "Payment : ";
        // this is the text we'll be operating on
        SpannableString text = new SpannableString(pay_text + orderPayment);

        // make "Lorem" (characters 0 to 5) red
        int temp = getResources().getColor(R.color.res_green);
        ForegroundColorSpan fcs = new ForegroundColorSpan(temp);
        text.setSpan(fcs, pay_text.length(), text.length(), 0);
        txt_orderPaymentStyle.setText(text);

        TextView txt_orderDateTime = findViewById(R.id.txt_orderDateTime);
        txt_orderDateTime.setTypeface(tf_opensense_regular);
        txt_orderDateTime.setText(orderDate);

        TextView txt_name = findViewById(R.id.txt_name);
        txt_name.setTypeface(tf_opensense_regular);
        txt_name.setText(orderName);

        TextView txt_address = findViewById(R.id.txt_address);
        txt_address.setTypeface(tf_opensense_regular);
        txt_address.setText(orderAddress);

        TextView txt_contact = findViewById(R.id.txt_contact);
        txt_contact.setTypeface(tf_opensense_regular);
        txt_contact.setText(orderContact);

        Button btn_call = findViewById(R.id.btn_call);
        btn_call.setTypeface(tf_opensense_regular);
        btn_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uri = "tel:" + orderContact;
                Intent i = new Intent(Intent.ACTION_DIAL);
                i.setData(Uri.parse(uri));
                startActivity(i);
            }
        });

        Button btn_map = findViewById(R.id.btn_map);
        btn_map.setTypeface(tf_opensense_regular);
        btn_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gettingGPSLocation();

            }
        });

         btn_picked = findViewById(R.id.btn_picked);
        btn_picked.setTypeface(tf_opensense_medium);
        Log.d("checkIddsd",""+orderStatus);
        if (orderStatus.equals("Order is processing")) {
            btn_picked.setText("PICKED");
            btn_picked.setBackgroundColor(getResources().getColor(R.color.res_orange));
        }
        else if (orderStatus.equals("Order is out for delivery"))
        {
            btn_picked.setText("COMPLETE");
            btn_picked.setBackgroundColor(Color.BLACK);

        }
        else if(orderStatus.equals("Order is Delivered"))
        {
            btn_picked.setVisibility(View.GONE);
        }
        btn_picked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (orderStatus.equals("Order is processing")) {
                    isPick = "pick";
                    btn_picked.setText("PICKED");
                    btn_picked.setBackgroundColor(getResources().getColor(R.color.res_orange));
                    new postingData().execute();
                }
                else if (orderStatus.equals("Order is out for delivery"))
                {
                    isPick ="complete";
                    btn_picked.setText("COMPLETE");
                    btn_picked.setBackgroundColor(Color.BLACK);
                    new postingData().execute();
                }
                else if(orderStatus.equals("Order is Delivered"))
                {
                    btn_picked.setVisibility(View.GONE);
                }

            }
        });

        list_order = findViewById(R.id.list_order);

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
        gps.init(DeliveryOrderDetail.this);
        // check if GPS enabled
        if (gps.canGetLocation()) {
            try {
                Double sLat = gps.getLatitude();
                Double sLong = gps.getLongitude();
                Log.w("Current Location", "Lat: " + sLat + "Long: " + sLong +"URL:::"+orderLat+"::::"+orderLon);
                Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse("http://maps.google.com/maps?saddr=" + sLat + "," + sLong + "&daddr=" + orderLat + "," +orderLon));
                intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                startActivity(intent);
            } catch (NullPointerException | NumberFormatException e) {
                // TODO: handle exception
                e.printStackTrace();
            }

        } else {
            gps.showSettingsAlert();
        }


    }


    private void getData() {


        //creating a string request to send request to the url
        String hp = getString(R.string.link) + getString(R.string.servicepath) + "deliveryboy_order_details.php?order_id="+orderNo;
        Log.w(getClass().getName(), hp);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, hp,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //hiding the progressbar after completion
                        Log.e("Response", response);

                        try {

                            //getting the whole json object from the response
                            JSONObject obj = new JSONObject(response);
                            if (obj.getString("success").equals("1")) {
                                JSONObject ja_order = obj.getJSONObject("Order");
                                orderDetailGetSet getSet;
                                getsetDeliveryorderdetail = new ArrayList<>();

                                orderAmount = ja_order.getString("order_amount");
                                orderItem = ja_order.getString("items");
                                orderDate = ja_order.getString("date");
                                orderPayment = ja_order.getString("payment");
                                orderName  = ja_order.getString("customer_name");
                                orderAddress = ja_order.getString("address");
                                orderContact = ja_order.getString("phone");
                                orderLat = ja_order.getString("lat");
                                orderLon = ja_order.getString("long");


                                initViews();
                                JSONArray jsonArray = ja_order.getJSONArray("item_name");

                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jo_orderDetail = jsonArray.getJSONObject(i);

                                    getSet = new orderDetailGetSet();
                                    getSet.setItemName(jo_orderDetail.getString("name"));
                                    getSet.setItemQuantity(jo_orderDetail.getString("qty"));
                                    getSet.setItemPrice(jo_orderDetail.getString("amount"));

                                    getsetDeliveryorderdetail.add(getSet);
                                }

                                OrderDetailAdapter adapter = new OrderDetailAdapter(getsetDeliveryorderdetail, DeliveryOrderDetail.this);
                                list_order.setAdapter(adapter);

                            } else {
                                Toast.makeText(DeliveryOrderDetail.this, obj.getString("order"), Toast.LENGTH_SHORT).show();
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //displaying the error in toast if occurs

                        NetworkResponse networkResponse = error.networkResponse;
                        if (networkResponse != null) {
                            Log.e("Status code", String.valueOf(networkResponse.statusCode));
                            Toast.makeText(getApplicationContext(), String.valueOf(networkResponse.statusCode), Toast.LENGTH_SHORT).show();

                        }
                    }
                });

        //creating a request queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        //adding the string request to request queue
        requestQueue.add(stringRequest);
    }

    class postingData extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(DeliveryOrderDetail.this);
            progressDialog.setMessage("Loading..");
            progressDialog.setCancelable(true);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub
            HttpClient httpClient = new DefaultHttpClient();
            HttpEntity httpEntity;

            Log.e("sourceFile", "" + orderNo );
            httpEntity = MultipartEntityBuilder.create().addTextBody("order_id", "" + orderNo, ContentType.create("text/plain", MIME.UTF8_CHARSET)).build();
             HttpPost httpPost = null ;
            if (isPick.equals("pick"))
            {
                httpPost = new HttpPost(getString(R.string.link)+getString(R.string.servicepath)  + "order_pick.php");
            }
            else if(isPick.equals("complete"))
            {
                httpPost = new HttpPost(getString(R.string.link)+getString(R.string.servicepath)  + "order_complete.php");
            }

            Log.e("httpPost", "" + httpPost);
            httpPost.setEntity(httpEntity);
            HttpResponse response = null;
            try {
                response = httpClient.execute(httpPost);
            } catch (IOException e) {
                e.printStackTrace();
            }
            HttpEntity result = null;
            if (response != null) {
                result = response.getEntity();
            }
            Log.e("result", "" + result);
            if (result != null) {
                try {
                    responseStr = EntityUtils.toString(result).trim();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.e("Response12322", "" + responseStr);
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (progressDialog.isShowing())
                progressDialog.dismiss();
            new showResponse().execute();

        }
    }

    class showResponse extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub
            try {
              final   JSONObject jsonObject = new JSONObject(responseStr);

                Log.e("Obj", jsonObject.toString());
                postSuccess = jsonObject.getString("success");
                if (jsonObject.getString("success").equals("1")) {


                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Toast.makeText(DeliveryOrderDetail.this, jsonObject.getString("error"), Toast.LENGTH_SHORT).show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                }

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NullPointerException e) {
                // TODO: handle exception
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (postSuccess.equals("1")) {
                if (orderStatus.equals("Order is processing")) {

                    AlertDialog.Builder builder1 = new AlertDialog.Builder(DeliveryOrderDetail.this, R.style.MyDialogTheme);

                    builder1.setMessage("Your assigned order is Picked for delivery.");
                    builder1.setCancelable(true);
                    builder1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            btn_picked.setText("COMPLETE");
                            btn_picked.setBackgroundColor(Color.BLACK);
                            onBackPressed();
                        }
                    });

                    AlertDialog alert11 = builder1.create();
                    alert11.show();


                } else if (orderStatus.equals("Order is out for delivery")) {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(DeliveryOrderDetail.this, R.style.MyDialogTheme);

                    builder1.setMessage("Your assigned order is completed.");
                    builder1.setCancelable(true);
                    builder1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            btn_picked.setVisibility(View.GONE);
                            onBackPressed();
                        }
                    });

                    AlertDialog alert11 = builder1.create();
                    alert11.show();
                } else if (orderStatus.equals("Order is Delivered")) {
                    btn_picked.setVisibility(View.GONE);
                }
            }
        }
    }

}
