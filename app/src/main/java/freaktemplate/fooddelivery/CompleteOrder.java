package freaktemplate.fooddelivery;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import freaktemplate.Getset.orderTimelineGetSet;
import freaktemplate.timeline.OrderStatus;
import freaktemplate.timeline.TimeLineAdapter;
import freaktemplate.timeline.TimeLineModel;

import static freaktemplate.fooddelivery.MainActivity.changeStatsBarColor;
import static freaktemplate.fooddelivery.MainActivity.tf_opensense_regular;

public class CompleteOrder extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private final List<TimeLineModel> mDataList = new ArrayList<>();
    private String orderId;
    private String[] ordertxt;
    private TextView txt_name;
    private TextView txt_contact;
    private TextView txt_address;
    private TextView txt_order_amount;
    private TextView txt_order_EstimatedTime;
    private TextView txt_order_time;
    private ImageView rest_image;
    private ArrayList<orderTimelineGetSet> orderGetSet;
    private orderTimelineGetSet oData;
    private String key = "";
    String rejectedOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_order);

        changeStatsBarColor(CompleteOrder.this);


        gettingIntents();
        ordertxt = new String[]{getString(R.string.timelineo1), getString(R.string.timelineo2), getString(R.string.timelineo3), getString(R.string.timelineo4), getString(R.string.timelineo5)};
        rejectedOrder = getString(R.string.timeline6);
        initView();
        getData();


    }

    private void gettingIntents() {
        Intent i = getIntent();
        orderId = i.getStringExtra("orderid");
        key = i.getStringExtra("key");


    }

    private void initView() {

        //settine time line adapter for order status
        mRecyclerView = findViewById(R.id.time_line);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setHasFixedSize(true);


        getSupportActionBar().hide();

        //initialization
        TextView txt_title = findViewById(R.id.txt_title);
        txt_title.setTypeface(tf_opensense_regular);

        TextView txt_orderno = findViewById(R.id.txt_orderno);
        txt_orderno.setTypeface(tf_opensense_regular);
        txt_orderno.setText("Order No : " + orderId);

        rest_image = findViewById(R.id.image);

        txt_name = findViewById(R.id.txt_name);
        txt_name.setTypeface(tf_opensense_regular);

        txt_address = findViewById(R.id.txt_address);
        txt_address.setTypeface(tf_opensense_regular);

        txt_contact = findViewById(R.id.txt_contact);
        txt_contact.setTypeface(tf_opensense_regular);

        TextView txt_order_title = findViewById(R.id.txt_order_title);
        txt_order_title.setTypeface(tf_opensense_regular);

        TextView txt_order = findViewById(R.id.txt_order);
        txt_order.setTypeface(tf_opensense_regular);
        txt_order.setText(orderId);

        TextView txt_order_amount_title = findViewById(R.id.txt_order_amount_title);
        txt_order_amount_title.setTypeface(tf_opensense_regular);

        txt_order_amount = findViewById(R.id.txt_order_amount);
        txt_order_amount.setTypeface(tf_opensense_regular);

        TextView txt_order_estimatedtime_title = findViewById(R.id.txt_order_estimatedtime_title);
        txt_order_estimatedtime_title.setTypeface(tf_opensense_regular);

        txt_order_EstimatedTime = findViewById(R.id.txt_order_estimatedtime);
        txt_order_EstimatedTime.setTypeface(tf_opensense_regular);

        TextView txt_order_time_title = findViewById(R.id.txt_order_time_title);
        txt_order_time_title.setTypeface(tf_opensense_regular);

        txt_order_time = findViewById(R.id.txt_order_time);
        txt_order_time.setTypeface(tf_opensense_regular);

        //on click listeners

        Button btn_placeorder = findViewById(R.id.btn_placeorder);
        btn_placeorder.setTypeface(tf_opensense_regular);

        ImageButton ib_back = findViewById(R.id.ib_back);
        ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (key.equals("orderplace")) {
                    Intent i = new Intent(CompleteOrder.this, MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
                    startActivity(i);
                } else onBackPressed();
            }
        });

        RelativeLayout rel_order = findViewById(R.id.rel_order);
        rel_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(CompleteOrder.this, OrderSpecification.class);
                i.putExtra("OrderId", orderId);
                startActivity(i);
            }
        });


    }


    private void updateTimeline() {
        if (Objects.equals(orderGetSet.get(0).getStatus(), "Activate")) {
            mDataList.add(new TimeLineModel(ordertxt[1], orderGetSet.get(0).getOrder_date_time(), OrderStatus.ACTIVE));
            mDataList.add(new TimeLineModel(ordertxt[2], orderGetSet.get(0).getOrder_date_time(), OrderStatus.ACTIVE));

            if (Objects.equals(orderGetSet.get(1).getStatus(), "Activate")) {
                mDataList.add(new TimeLineModel(ordertxt[3], orderGetSet.get(1).getOrder_date_time(), OrderStatus.ACTIVE));

                if (Objects.equals(orderGetSet.get(2).getStatus(), "Activate")) {
                    mDataList.add(new TimeLineModel(ordertxt[4], orderGetSet.get(2).getOrder_date_time(), OrderStatus.ACTIVE));
                } else {
                    mDataList.add(new TimeLineModel(ordertxt[4], "", OrderStatus.INACTIVE));
                }
            } else {
                mDataList.add(new TimeLineModel(ordertxt[3], "", OrderStatus.INACTIVE));
                mDataList.add(new TimeLineModel(ordertxt[4], "", OrderStatus.INACTIVE));
            }
        } else {
            mDataList.add(new TimeLineModel(ordertxt[1], "", OrderStatus.INACTIVE));
            mDataList.add(new TimeLineModel(ordertxt[2], "", OrderStatus.INACTIVE));
            mDataList.add(new TimeLineModel(ordertxt[3], "", OrderStatus.INACTIVE));
            mDataList.add(new TimeLineModel(ordertxt[4], "", OrderStatus.INACTIVE));
        }
        boolean mWithLinePadding = false;
        TimeLineAdapter mTimeLineAdapter = new TimeLineAdapter(mDataList, mWithLinePadding);
        mRecyclerView.setAdapter(mTimeLineAdapter);

    }

    private void updateRejectTimeLine() {
        if (Objects.equals(orderGetSet.get(0).getStatus(), "Activate")) {
            mDataList.add(new TimeLineModel(ordertxt[1], "", OrderStatus.INACTIVE));
            mDataList.add(new TimeLineModel(ordertxt[2], "", OrderStatus.INACTIVE));
            mDataList.add(new TimeLineModel(ordertxt[3], "", OrderStatus.INACTIVE));
            mDataList.add(new TimeLineModel(rejectedOrder, orderGetSet.get(0).getOrder_date_time(), OrderStatus.COMPLETED));
        }
        boolean mWithLinePadding = false;
        TimeLineAdapter mTimeLineAdapter = new TimeLineAdapter(mDataList, mWithLinePadding);
        mRecyclerView.setAdapter(mTimeLineAdapter);
    }


    private void getData() {
        //getting the progressbar
        final ProgressBar progressBar = findViewById(R.id.progressBar);

        //making the progressbar visible
        progressBar.setVisibility(View.VISIBLE);

        //creating a string request to send request to the url
        String hp = getString(R.string.link) + getString(R.string.servicepath) + "order_details.php?order_id=" + orderId;
        Log.d("CheckUrl", "" + getString(R.string.link) + getString(R.string.servicepath) + "order_details.php?order_id=" + orderId);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, hp,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //hiding the progressbar after completion
                        progressBar.setVisibility(View.INVISIBLE);
                        Log.e("Response", response);

                        try {

                            //getting the whole json object from the response
                            JSONObject obj = new JSONObject(response);
                            String status = obj.getString("success");
                            if (Objects.equals(status, "1")) {
                                orderGetSet = new ArrayList<>();

                                JSONArray ja_orderDetail = obj.getJSONArray("order_details");
                                JSONObject jo_data = ja_orderDetail.getJSONObject(0);
                                String txt_restName = jo_data.getString("restaurant_name");
                                String txt_restaurant_address = jo_data.getString("restaurant_address");
                                String txt_restaurant_contact = jo_data.getString("restaurant_contact");
                                String txt_order_amount = jo_data.getString("order_amount");
                                String txt_order_time = jo_data.getString("order_time");
                                String txt_delivery_time = jo_data.getString("delivery_time");
                                String txt_order_id = jo_data.getString("order_id");
                                String image = jo_data.getString("restaurant_image");




                                updateUI(txt_restName, txt_restaurant_address, txt_restaurant_contact, txt_order_amount, txt_order_time, txt_delivery_time, txt_order_id, image);
                                if (jo_data.getString("reject_date_time").equals("null")) {
                                    oData = new orderTimelineGetSet();
                                    oData.setOrder_date_time(jo_data.getString("order_verified_date"));
                                    oData.setStatus(jo_data.getString("order_verified"));
                                    orderGetSet.add(oData);
                                    oData = new orderTimelineGetSet();
                                    oData.setOrder_date_time(jo_data.getString("delivery_date_time"));
                                    oData.setStatus(jo_data.getString("delivery_status"));
                                    orderGetSet.add(oData);
                                    oData = new orderTimelineGetSet();
                                    oData.setOrder_date_time(jo_data.getString("delivered_date_time"));
                                    oData.setStatus(jo_data.getString("delivered_status"));
                                    orderGetSet.add(oData);
                                    updateTimeline();
                                } else {
                                    oData = new orderTimelineGetSet();
                                    oData.setOrder_date_time(jo_data.getString("reject_date_time"));
                                    oData.setStatus(jo_data.getString("reject_status"));
                                    orderGetSet.add(oData);
                                    updateRejectTimeLine();
                                }


                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //displaying the error in toast if occurrs
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        //creating a request queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        //adding the string request to request queue
        requestQueue.add(stringRequest);
    }


    private void updateUI(String txt_restName, String txt_restaurant_address, String txt_restaurant_contact, String txt_order_amo, String txt_order_tim, String txt_delivery_time, String txt_order_id, String image) {
        txt_name.setText(txt_restName);
        txt_address.setText(txt_restaurant_address);
        txt_contact.setText(txt_restaurant_contact);
        txt_order_EstimatedTime.setText(txt_delivery_time);
        txt_order_amount.setText(getString(R.string.currency) + " " + txt_order_amo);
        txt_order_time.setText(txt_order_tim);
        Picasso.with(CompleteOrder.this).load(getResources().getString(R.string.link) + getString(R.string.imagepath) + image).into(rest_image);
        mDataList.add(new TimeLineModel(ordertxt[0], txt_order_tim, OrderStatus.ACTIVE));
    }


    @Override
    public void onBackPressed() {
        if (key.equals("orderplace")) {
            Intent i = new Intent(CompleteOrder.this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
            startActivity(i);
        } else super.onBackPressed();
    }
}

