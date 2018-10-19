package freaktemplate.fooddelivery;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import freaktemplate.Adapter.ListViewHolderCart;
import freaktemplate.Getset.cartgetset;
import freaktemplate.utils.DBAdapter;

import static freaktemplate.fooddelivery.MainActivity.changeStatsBarColor;

public class Cart extends AppCompatActivity {
    private static ArrayList<cartgetset> cartlist;
    private ProgressDialog progressDialog;
    private String menuid;
    private String s1;
    private float total = 0;
    private TextView txt_finalans;
    private int quantity;
    private static final String MyPREFERENCES = "Fooddelivery";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        changeStatsBarColor(Cart.this);


        gettingIntent();

        getData();

    }

    private void gettingIntent() {
        Intent iv = getIntent();
        String detail_id = iv.getStringExtra("detail_id");
        String restaurent_name = iv.getStringExtra("restaurent_name");
        Log.e("detail_id", "" + detail_id);
    }

    private void getData() {

        txt_finalans = findViewById(R.id.txt_finalans);
        ((TextView) findViewById(R.id.txt_title)).setTypeface(MainActivity.tf_opensense_medium);


        cartlist = new ArrayList<>();
        new getList().execute();
    }

    private class getList extends AsyncTask<String, Integer, Integer> {
        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            progressDialog = new ProgressDialog(Cart.this);
            progressDialog.setMessage(getString(R.string.loading));
            progressDialog.setCancelable(true);
            progressDialog.show();
        }

        @Override
        protected Integer doInBackground(String... params) {
            // TODO Auto-generated method stub
            cartlist.clear();
            DBAdapter myDbHelper = new DBAdapter(Cart.this);
            myDbHelper = new DBAdapter(Cart.this);
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
            SQLiteDatabase db = myDbHelper.getReadableDatabase();
            try {
                Cursor cur = db.rawQuery("select * from cart where foodprice >=1;", null);
                Log.e("cartlisting", "" + ("select * numberOfRecords cart where foodprice <=0;"));
                Log.d("SIZWA", "" + cur.getCount());
                if (cur.getCount() != 0) {
                    if (cur.moveToFirst()) {
                        do {
                            cartgetset obj = new cartgetset();
                            String resid = cur.getString(cur.getColumnIndex("resid"));
                            String foodid = cur.getString(cur.getColumnIndex("foodname"));
                            menuid = cur.getString(cur.getColumnIndex("menuid"));
                            String foodname = cur.getString(cur.getColumnIndex("foodname"));
                            String foodprice = cur.getString(cur.getColumnIndex("foodprice"));
                            String fooddesc = cur.getString(cur.getColumnIndex("fooddesc"));
                            String restcurrency = cur.getString(cur.getColumnIndex("restcurrency"));
                            restcurrency = restcurrency.replace("$", "");
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
                                Log.e("Error", e.getMessage());
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
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            ListView list_cart = findViewById(R.id.list_cart);
            Log.d("file", "" + cartlist.size());
            if (cartlist.size() == 0) {
                Toast.makeText(getApplicationContext(), getString(R.string.norecord), Toast.LENGTH_LONG).show();
                list_cart.setVisibility(View.INVISIBLE);
            } else {
                list_cart = findViewById(R.id.list_cart);
                Button btncheckout = findViewById(R.id.btn_checkout);
                btncheckout.setTypeface(MainActivity.tf_opensense_regular);
                btncheckout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    if(checkingSignIn())
                    {
                        Intent iv = new Intent(Cart.this, PlaceOrder.class);
                        iv.putExtra("order_price", "" + s1);
                        iv.putExtra("res_id", menuid);
                        startActivity(iv);}
                        else {
                        Intent iv = new Intent(Cart.this, Login.class);
                        iv.putExtra("key", "PlaceOrder");
                        iv.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(iv);
                        Toast.makeText(Cart.this, R.string.loginmsg, Toast.LENGTH_SHORT).show();
                    }
                    }
                });
                cartadapter1 adapter = new cartadapter1(Cart.this, cartlist, menuid, quantity);
                list_cart.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                list_cart.getAdapter().getCount();
                String totalcartitem = String.valueOf(list_cart.getAdapter().getCount());
                if (totalcartitem.equals("0")) {
                    btncheckout.setEnabled(false);
                    Toast.makeText(getApplicationContext(), "your cart is empty", Toast.LENGTH_SHORT).show();
                } else {
                    btncheckout.setEnabled(true);
                }
                list_cart.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    }
                });
                double roundOff = (double) Math.round(total * 100) / 100;
                txt_finalans.setText(getString(R.string.currency)+" " + roundOff);
                Button btn_cart = findViewById(R.id.btn_cart);
                btn_cart.setText(totalcartitem);
            }
        }
    }

    private boolean checkingSignIn() {
        //getting shared preference
        SharedPreferences prefs = getSharedPreferences(MyPREFERENCES, MODE_PRIVATE);
        Log.e("user", "" + prefs.getString("userid", null));
        // check user is created or not
        // if user is already logged in
        if (prefs.getString("userid", null) != null) {
            String userid = prefs.getString("userid", null);
            return !userid.equals("delete");
        } else {
            return false;
        }
    }



class cartadapter1 extends BaseAdapter {
    final ArrayList<Integer> quantity = new ArrayList<>();
    SQLiteDatabase db;
    Cursor cur = null;
    final String menuid1;
    String menuid321, foodprice321, restcurrency321;
    final int quen;
    String foodid, foodname, fooddesc;
    int val1;
    float add, sub;
    private final ArrayList<cartgetset> data1;
    private final Activity activity;
    private LayoutInflater inflater = null;

    cartadapter1(Activity a, ArrayList<cartgetset> str, String menuid, int quantity) {
        activity = a;
        data1 = str;
        menuid1 = menuid;
        quen = quantity;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (int i = 0; i < data1.size(); i++) {
            this.quantity.add(i);
        }
    }

    @Override
    public int getCount() {
        return data1.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View row;
        final ListViewHolderCart listViewHolder;
        Typeface opensansregular = Typeface.createFromAsset(activity.getAssets(), "fonts/OpenSans-Regular.ttf");
        if (convertView == null) {
            row = inflater.inflate(R.layout.cell_cart, parent,false);
            listViewHolder = new ListViewHolderCart();
            listViewHolder.txt_name1 = row.findViewById(R.id.txt_name1);
            listViewHolder.txt_name1.setTypeface(opensansregular);
            listViewHolder.txt_desc = row.findViewById(R.id.txt_desc);
            listViewHolder.txt_desc.setTypeface(opensansregular);
            listViewHolder.txt_totalprice = row.findViewById(R.id.txt_totalprice);
            listViewHolder.txt_basic_price = row.findViewById(R.id.txt_basic_price);
            listViewHolder.txt_totalprice.setTypeface(MainActivity.tf_opensense_medium);
            listViewHolder.txt_basic_price.setTypeface(opensansregular);
            listViewHolder.txt_quantity = row.findViewById(R.id.txt_quantity);
            listViewHolder.txt_quantity.setTypeface(opensansregular);

            listViewHolder.btn_plus = row.findViewById(R.id.btn_plus);
            listViewHolder.btn_minus1 = row.findViewById(R.id.btn_minus1);
            listViewHolder.edTextQuantity = row.findViewById(R.id.edTextQuantity);
            listViewHolder.edTextQuantity.setTypeface(opensansregular);
            listViewHolder.btn_plus.setTag(listViewHolder);
            listViewHolder.btn_minus1.setTag(listViewHolder);
            row.setTag(listViewHolder);
        } else {
            row = convertView;
            listViewHolder = (ListViewHolderCart) row.getTag();
        }
        listViewHolder.txt_name1.setText((data1.get(position).getFoodname()));
        listViewHolder.txt_desc.setText(data1.get(position).getFooddesc());
        listViewHolder.txt_totalprice.setText(getString(R.string.currency) +" "+ (data1.get(position).getRestcurrency()));
        listViewHolder.edTextQuantity.setText(data1.get(position).getFoodprice());
        val1 = Integer.parseInt(data1.get(position).getFoodprice());
        listViewHolder.btn_plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View p = (View) v.getParent();
                ListViewHolderCart holder1 = (ListViewHolderCart) (v.getTag());
                val1 = Integer.valueOf(holder1.edTextQuantity.getText().toString());

                String add_price = holder1.txt_totalprice.getText().toString().trim().replace("$", "");
                add = Float.valueOf(add_price);
                total = total + add;
                Log.e("totalansadd ", "" + total);
                double roundOff = (double) Math.round(total * 100) / 100;
                txt_finalans.setText(getString(R.string.currency) +" "+ roundOff);
                val1++;
                holder1.edTextQuantity.setText(String.valueOf(val1));
                DBAdapter myDbHelper;
                myDbHelper = new DBAdapter(activity);
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
                    cur = db.rawQuery("UPDATE cart SET foodprice ='" + val1 + "' Where menuid ='" + data1.get(position).getMenuid() + "';", null);
                    Log.e("updatequeryplus", "" + "UPDATE cart SET foodprice ='" + val1 + "' Where menuid ='" + data1.get(position).getMenuid() + "';");
                    Log.e("SIZWA", "" + cur.getCount());
                    if (cur.getCount() != 0) {
                        if (cur.moveToFirst()) {
                            do {
                                cartgetset obj = new cartgetset();
                                menuid321 = cur.getString(cur.getColumnIndex("menuid"));
                                foodid = cur.getString(cur.getColumnIndex("foodid"));
                                foodname = cur.getString(cur.getColumnIndex("foodname"));
                                foodprice321 = cur.getString(cur.getColumnIndex("foodprice"));
                                fooddesc = cur.getString(cur.getColumnIndex("fooddesc"));
                                restcurrency321 = cur.getString(cur.getColumnIndex("restcurrency"));
                                obj.setFoodid(foodid);
                                obj.setMenuid(menuid321);
                                obj.setFoodname(foodname);
                                obj.setFoodprice(foodprice321);
                                obj.setFooddesc(fooddesc);
                                obj.setRestcurrency(restcurrency321);
                                Log.e("menuid321updatedcart", "" + menuid321);
                                Log.e("foodp321updatedcart", "" + foodprice321);
                                data1.add(obj);
                                new getList().execute();
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
        });
        listViewHolder.btn_minus1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListViewHolderCart holder1 = (ListViewHolderCart) (v.getTag());
                String sub_price = holder1.txt_totalprice.getText().toString().trim().replace("$", "");

                sub = Float.valueOf(sub_price);
                if (val1 <= 0) {
                } else {
                    val1 = Integer.valueOf(holder1.edTextQuantity.getText().toString());
                    val1--;
                    total = total - sub;
                    Log.e("totalanssub ", "" + total);
                    double roundOff = (double) Math.round(total * 100) / 100;
                    txt_finalans.setText(getString(R.string.currency)+" " + roundOff);
                    holder1.edTextQuantity.setText(String.valueOf(val1));
                    DBAdapter myDbHelper = new DBAdapter(activity);
                    myDbHelper = new DBAdapter(activity);
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
                        cur = db.rawQuery("UPDATE cart SET foodprice ='" + val1 + "' Where menuid ='" + data1.get(position).getMenuid() + "';", null);
                        Log.e("updatequeryminus", "" + "UPDATE cart SET foodprice ='" + val1 + "' Where menuid ='" + data1.get(position).getMenuid() + "';");
                        Log.e("SIZWA", "" + cur.getCount());
                        if (cur.getCount() != 0) {
                            if (cur.moveToFirst()) {
                                do {
                                    cartgetset obj = new cartgetset();
                                    menuid321 = cur.getString(cur.getColumnIndex("menuid"));
                                    foodid = cur.getString(cur.getColumnIndex("foodid"));
                                    foodname = cur.getString(cur.getColumnIndex("foodname"));
                                    foodprice321 = cur.getString(cur.getColumnIndex("foodprice"));
                                    fooddesc = cur.getString(cur.getColumnIndex("fooddesc"));
                                    restcurrency321 = cur.getString(cur.getColumnIndex("restcurrency"));
                                    obj.setFoodid(foodid);
                                    obj.setMenuid(menuid321);
                                    obj.setFoodname(foodname);
                                    obj.setFoodprice(foodprice321);
                                    obj.setFooddesc(fooddesc);
                                    obj.setRestcurrency(restcurrency321);
                                    Log.e("menuid321updatedcart", "" + menuid321);
                                    Log.e("foodp321updatedcart", "" + foodprice321);
                                    data1.add(obj);
                                    new getList().execute();
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
            }
        });
        return row;
    }
}
}





