package freaktemplate.fooddelivery;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import java.io.IOException;
import java.util.ArrayList;

import freaktemplate.Getset.myOrderGetSet;
import freaktemplate.utils.DBAdapter;

import static freaktemplate.fooddelivery.MainActivity.changeStatsBarColor;
import static freaktemplate.fooddelivery.MainActivity.checkInternet;
import static freaktemplate.fooddelivery.MainActivity.showErrorDialog;
import static freaktemplate.fooddelivery.MainActivity.tf_opensense_medium;
import static freaktemplate.fooddelivery.MainActivity.tf_opensense_regular;

public class MyOrderPage extends AppCompatActivity {
    private ListView list_notification;
    private ArrayList<myOrderGetSet> data;
    private notificationAdapter adapter;
    AdRequest adRequest;
    AdView mAdView;
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myorder);
        getSupportActionBar().hide();
        changeStatsBarColor(MyOrderPage.this);

        initialization();

        if (checkInternet(MyOrderPage.this)) {
            AdShow();
            new GetDataAsyncTask().execute();
        } else showErrorDialog(MyOrderPage.this);


    }


    class GetDataAsyncTask extends AsyncTask<Void, Void, Integer> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MyOrderPage.this);
            progressDialog.setMessage(getString(R.string.loading));
            progressDialog.setCancelable(true);
            progressDialog.show();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            gettingDataFromDatabase();
            return null;
        }

        @Override
        protected void onPostExecute(Integer aVoid) {
            super.onPostExecute(aVoid);

            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
                if (data != null) {
                    adapter = new notificationAdapter(data);
                    list_notification.setAdapter(adapter);


                } else
                    Toast.makeText(MyOrderPage.this, R.string.noorder, Toast.LENGTH_SHORT).show();
            }


        }
    }


    private void gettingDataFromDatabase() {
        DBAdapter myDbHelper = new DBAdapter(MyOrderPage.this);
        myDbHelper = new DBAdapter(MyOrderPage.this);
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
        SQLiteDatabase db = myDbHelper.getReadableDatabase();
        Cursor cur;
        try {
            cur = db.rawQuery("SELECT * FROM order_detail;", null);
            data = new ArrayList<>();
            Log.e("SIZWA", "" + cur.getCount());
            if (cur.getCount() != 0) {
                if (cur.moveToFirst()) {
                    do {
                        myOrderGetSet obj = new myOrderGetSet();
                        String txt_restaurantName = cur.getString(cur.getColumnIndex("restaurantName"));
                        String txt_restaurantAddress = cur.getString(cur.getColumnIndex("restaurantAddress"));
                        String txt_orderId = cur.getString(cur.getColumnIndex("orderId"));
                        String txt_orderAmount = cur.getString(cur.getColumnIndex("orderAmount"));
                        String txt_orderTime = cur.getString(cur.getColumnIndex("orderTime"));

                        obj.setResName(txt_restaurantName);
                        obj.setResAddress(txt_restaurantAddress);
                        obj.setOrder_id(txt_orderId);
                        obj.setOrder_dateTime(txt_orderTime);
                        obj.setOrder_total(txt_orderAmount);


                        data.add(obj);
                    } while (cur.moveToNext());
                }
            }
            cur.close();
            db.close();
            myDbHelper.close();
        } catch (Exception e) {
            Log.e("Error", e.getMessage());

        }

    }

    private void initialization() {
        list_notification = findViewById(R.id.list_notification);
        ((TextView) findViewById(R.id.txt_title)).setTypeface(tf_opensense_regular);
        ImageButton ib_back = findViewById(R.id.ib_back);
        ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        list_notification.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent i = new Intent(MyOrderPage.this, CompleteOrder.class);
                i.putExtra("orderid", adapter.dat.get(position).getOrder_id());
                i.putExtra("key", "notification");
                startActivity(i);
                if (getResources().getString(R.string.show_admob_ads).equals("yes")) {
                    if (mInterstitialAd.isLoaded()) {
                        mInterstitialAd.show();

                    }
                }
            }
        });

    }


    private class notificationAdapter extends BaseAdapter {
        final ArrayList<myOrderGetSet> dat;
        private LayoutInflater inflater = null;

        notificationAdapter(ArrayList<myOrderGetSet> dat) {
            this.dat = dat;
            inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        }

        @Override
        public int getCount() {
            return dat.size();
        }

        @Override
        public Object getItem(int position) {
            return dat.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View vi = convertView;
            if (vi == null)
                vi = inflater.inflate(R.layout.cell_order, parent, false);


            TextView txt_name = vi.findViewById(R.id.txt_name);
            txt_name.setText(dat.get(position).getResName());
            txt_name.setTypeface(tf_opensense_medium);
            TextView txt_address = vi.findViewById(R.id.txt_address);
            txt_address.setText(dat.get(position).getResAddress());
            txt_address.setTypeface(tf_opensense_regular);

            TextView txt_res_name = vi.findViewById(R.id.txt_res_name);
            txt_res_name.setText(dat.get(position).getResName().substring(0, 1));

            TextView txt_order_date = vi.findViewById(R.id.txt_order_date);
            txt_order_date.setText("Order Date: " + dat.get(position).getOrder_dateTime());

            TextView txt_total_tittle = vi.findViewById(R.id.txt_total_tittle);
            TextView txt_total = vi.findViewById(R.id.txt_total);
            txt_total.setText(getString(R.string.currency) + " " + dat.get(position).getOrder_total());


            txt_res_name.setTypeface(tf_opensense_regular);
            txt_order_date.setTypeface(tf_opensense_regular);
            txt_total_tittle.setTypeface(tf_opensense_regular);
            txt_total.setTypeface(tf_opensense_regular);


            return vi;
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
