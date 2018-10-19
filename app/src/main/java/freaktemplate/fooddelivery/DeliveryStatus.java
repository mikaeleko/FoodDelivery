package freaktemplate.fooddelivery;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ImageButton;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import freaktemplate.Adapter.DeliveryAdapter;
import freaktemplate.Getset.DeliveryGetSet;
import freaktemplate.utils.Config;

import static freaktemplate.fooddelivery.MainActivity.tf_opensense_regular;

public class DeliveryStatus extends AppCompatActivity {

    private ArrayList<DeliveryGetSet> data;
    private ListView listView_delivery;
    private static final String MY_PREFS_NAME = "Fooddelivery";
    private String status,DeliveryBoyId,regId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_status);
        getSupportActionBar().hide();

        SharedPreferences prefsDeliveryBoyId = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        DeliveryBoyId = prefsDeliveryBoyId.getString("DeliveryUserId",null);
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        regId = pref.getString("regId", null);
        Log.d("checksfds",""+regId+":::"+DeliveryBoyId);
        new RegisterMobile().execute();

    }

    private void initViews() {

        TextView txt_header = findViewById(R.id.txt_header);
        TextView txt_name = findViewById(R.id.txt_name);
        TextView btn_deilverb = findViewById(R.id.btn_deilverb);
        TextView btn_signout = findViewById(R.id.btn_signout);
        TextView txt_presenceOn = findViewById(R.id.txt_presenceOn);
        TextView list_order_none = findViewById(R.id.list_order_none);
        TextView btn_order_history = findViewById(R.id.btn_order_history);
        TextView btn_my_profile = findViewById(R.id.btn_my_profile);
        SwitchCompat sw_radius_onoff = findViewById(R.id.Sw_radius_onoff);

        listView_delivery = findViewById(R.id.list_order_info);
        txt_header.setTypeface(tf_opensense_regular);
        txt_name.setTypeface(tf_opensense_regular);
        btn_deilverb.setTypeface(tf_opensense_regular);
        btn_signout.setTypeface(tf_opensense_regular);
        txt_presenceOn.setTypeface(tf_opensense_regular);
        list_order_none.setTypeface(tf_opensense_regular);
        btn_order_history.setTypeface(tf_opensense_regular);
        btn_my_profile.setTypeface(tf_opensense_regular);

        //getting shared pref and setting data

        txt_name.setText(getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).getString("DeliveryUserName", ""));

        sw_radius_onoff.setChecked(getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).getBoolean("isPresent", false));

        sw_radius_onoff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    status = "yes";
                } else {
                    status = "no";
                }

                sendPresence(status);

            }
        });

        btn_signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences.Editor edit = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                edit.putBoolean("isDeliverAccountActive", false);
                edit.putString("DeliveryUserId", "");
                edit.putString("DeliveryUserName", "");
                edit.putString("DeliveryUserPhone", "");
                edit.putString("DeliveryUserEmail", "");
                edit.putString("DeliveryUserVNo", "");
                edit.putString("DeliveryUserVType", "");
                edit.putBoolean("isPresent", false);
                edit.apply();
                Intent i = new Intent(DeliveryStatus.this, MainActivity.class);
                startActivity(i);
                finish();

            }
        });

        btn_my_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(DeliveryStatus.this, DeliveryUserProfile.class);
                startActivity(i);
            }
        });

        btn_order_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(DeliveryStatus.this, DeliveryOrderHistory.class);
                startActivity(i);
            }
        });

        ImageButton ib_back = findViewById(R.id.ib_back);
        ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        listView_delivery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(DeliveryStatus.this, DeliveryOrderDetail.class);
                i.putExtra("OrderNo", data.get(position).getOrderNo());
                i.putExtra("OrderAmount", data.get(position).getOrderAmount());
                i.putExtra("isComplete", data.get(position).getComplete());
                i.putExtra("OrderQuantity", data.get(position).getOrderQuantity());
                i.putExtra("OrderTimeDate", data.get(position).getOrderTimeDate());
                startActivity(i);
            }
        });

        settingData();
    }

    private void sendPresence(final String status) {
        //creating a string request to send request to the url
        String hp = getString(R.string.link) + getString(R.string.servicepath) + "deliveryboy_presence.php";
        Log.w(getClass().getName(), hp);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, hp,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //hiding the progressbar after completion
                        Log.e("Response123", response);
                        //    {"data":{"success":"1","presence":"false"}}

                        try {
                            JSONObject jo_main = new JSONObject(response);
                            JSONObject jo_data = jo_main.getJSONObject("data");
                            if (jo_data.getString("success").equals("1")) {
                                String isPresent = jo_data.getString("presence");
                                getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit().putBoolean("isPresent", returnBool(isPresent)).apply();
                            } else {
                                Toast.makeText(DeliveryStatus.this, jo_data.getString("presence"), Toast.LENGTH_SHORT).show();
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
                }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("status", status); //Add the data you'd like to send to the server.
                MyData.put("deliverboy_id",getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).getString("DeliveryUserId", ""));
                return MyData;
            }
        };
        //creating a request queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        //adding the string request to request queue
        requestQueue.add(stringRequest);
    }

    private void settingData() {


        //creating a string request to send request to the url
        String hp = getString(R.string.link) + getString(R.string.servicepath) + "deliveryboy_order.php?deliverboy_id=" + getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).getString("DeliveryUserId", "");
        Log.w(getClass().getName(), hp);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, hp,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //hiding the progressbar after completion
                        Log.e("Response123", response);

                        try {

                            //getting the whole json object from the response
                            JSONObject obj = new JSONObject(response);
                            if (obj.getString("success").equals("1")) {
                                JSONArray ja_order = obj.getJSONArray("order");
                                DeliveryGetSet getSet;
                                data = new ArrayList<>();

                                for (int i = 0; i < ja_order.length(); i++) {
                                    JSONObject jo_orderDetail = ja_order.getJSONObject(i);
                                    getSet = new DeliveryGetSet();
                                    getSet.setOrderNo(jo_orderDetail.getString("order_no"));
                                    getSet.setOrderTimeDate(jo_orderDetail.getString("date"));
                                    getSet.setOrderQuantity(jo_orderDetail.getString("items"));
                                    getSet.setOrderAmount(jo_orderDetail.getString("total_amount"));
                                    getSet.setComplete(jo_orderDetail.getString("status"));
                                    Log.d("Chehdfsf",""+jo_orderDetail.getString("status"));

                                    data.add(getSet);
                                }

                                DeliveryAdapter adapter = new DeliveryAdapter(data, DeliveryStatus.this);
                                listView_delivery.setAdapter(adapter);

                            } else {
                                Toast.makeText(DeliveryStatus.this, obj.getString("order"), Toast.LENGTH_SHORT).show();
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

    @Override
    public void onBackPressed() {
        showExitDialog();

    }

    @Override
    protected void onResume() {
        super.onResume();
        initViews();
    }

    private void showExitDialog() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(DeliveryStatus.this, R.style.MyDialogTheme);
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

    private boolean returnBool(String status) {
        return Objects.equals(status, "yes");
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

                http://192.168.1.116/freak/FoodDeliverySystem/api/token.php?token=1253aaa&type=ios&user_id=3
                hp = new URL(getString(R.string.link) + getString(R.string.servicepath) + "token.php?token="+regId+"&type=android&user_id=null&delivery_boyid="+DeliveryBoyId);
                Log.d("URL4564", "" + hp);

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

}

