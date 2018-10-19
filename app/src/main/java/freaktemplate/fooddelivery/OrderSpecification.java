package freaktemplate.fooddelivery;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import freaktemplate.Adapter.OrderDetailAdapter;
import freaktemplate.Getset.orderDetailGetSet;

import static freaktemplate.fooddelivery.MainActivity.changeStatsBarColor;

public class OrderSpecification extends AppCompatActivity {
    private ListView list_order;
    private ArrayList<orderDetailGetSet> data;
    private TextView txt_FinalAns;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_specification);
        if(getSupportActionBar()!=null)
        getSupportActionBar().hide();

        initViews();

        Intent i = getIntent();

        String tempOrderID = i.getStringExtra("OrderId");

        settingData(tempOrderID);
    }

    private void initViews() {
        ImageButton ib_back = findViewById(R.id.ib_back);
        ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        list_order = findViewById(R.id.list_order);
        TextView txt_total_tittle = findViewById(R.id.txt_total_tittle);
        txt_total_tittle.setTypeface(MainActivity.tf_opensense_regular);

        TextView txt_title = findViewById(R.id.txt_title);
        txt_title.setTypeface(MainActivity.tf_opensense_regular);

        txt_FinalAns = findViewById(R.id.txt_finalans);
        txt_FinalAns.setTypeface(MainActivity.tf_opensense_regular);

        changeStatsBarColor(OrderSpecification.this);

    }


    private void settingData(String orderID) {


        //creating a string request to send request to the url
        String hp = getString(R.string.link) + getString(R.string.servicepath) + "/order_item_details.php?order_id=" + orderID;
        Log.w(getClass().getName(), hp);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, hp,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //hiding the progressbar after completion
                        Log.e("Response", response);
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        try {
                            //getting the whole json object from the response
                            JSONObject obj = new JSONObject(response);
                            if (obj.getString("success").equals("1")) {
                                data = new ArrayList<>();
                                orderDetailGetSet getSet;
                                JSONObject jo_order = obj.getJSONObject("Order");
                                String total_amount = jo_order.getString("order_amount");

                                JSONArray ja_item = jo_order.getJSONArray("item_name");
                                for (int i = 0; i < ja_item.length(); i++) {
                                    getSet = new orderDetailGetSet();
                                    JSONObject jo_item = ja_item.getJSONObject(i);
                                    getSet.setItemName(jo_item.getString("name"));
                                    getSet.setItemPrice(jo_item.getString("amount"));
                                    getSet.setItemQuantity(jo_item.getString("qty"));
                                    data.add(getSet);
                                }

                                updateUI(total_amount);
                            } else {
                                Toast.makeText(OrderSpecification.this, obj.getString("order"), Toast.LENGTH_SHORT).show();
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

    private void updateUI(String total) {
        OrderDetailAdapter adapter = new OrderDetailAdapter(data, OrderSpecification.this);
        list_order.setAdapter(adapter);
        String temp ="$ " + total;
        txt_FinalAns.setText(temp);
    }
}
