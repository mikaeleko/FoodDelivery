package freaktemplate.fooddelivery;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import freaktemplate.Adapter.DeliveryAdapter;
import freaktemplate.Adapter.OrderDetailAdapter;
import freaktemplate.Getset.DeliveryGetSet;

import static freaktemplate.fooddelivery.MainActivity.tf_opensense_regular;

public class DeliveryOrderHistory extends AppCompatActivity {


    private ArrayList<DeliveryGetSet> data;
    private OrderDetailAdapter adapter;
    private ListView lv_order_history;
    private static final String MY_PREFS_NAME = "Fooddelivery";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_order_history);

        getSupportActionBar().hide();


    }

    private void initView() {
        TextView txt_title = findViewById(R.id.txt_title);
        txt_title.setTypeface(tf_opensense_regular);
        lv_order_history = findViewById(R.id.lv_order_history);

        lv_order_history.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(DeliveryOrderHistory.this, DeliveryOrderDetail.class);
                i.putExtra("OrderNo", data.get(position).getOrderNo());
                i.putExtra("OrderAmount", data.get(position).getOrderAmount());
                i.putExtra("isComplete", data.get(position).getComplete());
                i.putExtra("OrderQuantity", data.get(position).getOrderQuantity());
                i.putExtra("OrderTimeDate", data.get(position).getOrderTimeDate());
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
        settingData();
    }

    private void settingData() {


        //creating a string request to send request to the url
        String hp = getString(R.string.link) + getString(R.string.servicepath) + "order_history.php?deliverboy_id=" + getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).getString("DeliveryUserId", "");

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
                                    data.add(getSet);
                                }

                                DeliveryAdapter adapter = new DeliveryAdapter(data, DeliveryOrderHistory.this);
                                lv_order_history.setAdapter(adapter);


                            } else {
                                Toast.makeText(DeliveryOrderHistory.this, obj.getString("order"), Toast.LENGTH_SHORT).show();
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
    protected void onResume() {
        super.onResume();

        initView();
    }
}
