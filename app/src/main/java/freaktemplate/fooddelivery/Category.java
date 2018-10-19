package freaktemplate.fooddelivery;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Objects;

import freaktemplate.Getset.CategoryHeaderGetSet;
import freaktemplate.Getset.CategorySubItemGetSet;

import static freaktemplate.fooddelivery.MainActivity.changeStatsBarColor;
import static freaktemplate.fooddelivery.MainActivity.checkInternet;
import static freaktemplate.fooddelivery.MainActivity.showErrorDialog;
import static freaktemplate.fooddelivery.MainActivity.tf_opensense_regular;

public class Category extends AppCompatActivity {

    private ArrayList<CategoryHeaderGetSet> categorylist;
    private ProgressDialog progressDialog;
    private ListView listview;
    private String subCatId="",subCatName="" ;
    private LinkedHashMap<CategoryHeaderGetSet, ArrayList<CategorySubItemGetSet>> map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        changeStatsBarColor(Category.this);


        initialization();

    }

    private void initialization() {
        listview = findViewById(R.id.listview);
        categorylist = new ArrayList<>();
        ((TextView)findViewById(R.id.txt_title)).setTypeface(tf_opensense_regular);
        ImageButton ib_back = findViewById(R.id.ib_back);
        ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        if (checkInternet(Category.this))
            new GetDataAsyncTask().execute();
        else showErrorDialog(Category.this);
    }


    class GetDataAsyncTask extends AsyncTask<Void, Void, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(Category.this);
            progressDialog.setMessage(getString(R.string.loading));
            progressDialog.setCancelable(true);
            progressDialog.show();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            URL hp;
            try {
                categorylist.clear();
                map = new LinkedHashMap<>();

                hp = new URL(getString(R.string.link)+getString(R.string.servicepath) + "restaurant_category.php");
                Log.e("URL", "" + hp);
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
                final JSONObject jObject = new JSONObject(total.toString());

                if (Objects.equals(jObject.getString("status"), "Success")) {
                    JSONArray category_list = jObject.getJSONArray("Category_List");
                    for (int qw = 0; qw < category_list.length(); qw++) {
                        JSONObject cat_detail = category_list.getJSONObject(qw);
                        CategoryHeaderGetSet temp = new CategoryHeaderGetSet();
                        temp.setName(cat_detail.getString("name"));
                        temp.setId(cat_detail.getString("id"));
                        JSONArray sub_cat = cat_detail.getJSONArray("subcategory");
                        ArrayList<CategorySubItemGetSet> subData = new ArrayList<>();
                        for (int i = 0; i < sub_cat.length(); i++) {
                            CategorySubItemGetSet tempSub = new CategorySubItemGetSet();
                            tempSub.setName(sub_cat.getJSONObject(i).getString("name"));
                            tempSub.setId(sub_cat.getJSONObject(i).getString("id"));
                            subData.add(tempSub);
                        }
                        map.put(temp, subData);
                    }

                } else {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Toast.makeText(Category.this, jObject.getString("status"), Toast.LENGTH_SHORT).show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }


            } catch (JSONException | IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NullPointerException e) {
                // TODO: handle exception
            }
            return null;
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        protected void onPostExecute(Integer aVoid) {
            super.onPostExecute(aVoid);

            if (progressDialog.isShowing()) {
                progressDialog.dismiss();

                final ArrayList list = new ArrayList();
                for (CategoryHeaderGetSet getSet : map.keySet()) {
                    list.add(getSet);
                    list.addAll(map.get(getSet));
                }
                final CategoryAdapter adapter = new CategoryAdapter(Category.this, list);
                listview.setAdapter(adapter);
                listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        subCatId = ((CategorySubItemGetSet) list.get(position)).getId();
                        subCatName = ((CategorySubItemGetSet) list.get(position)).getName();
                        adapter.notifyDataSetChanged();
                    }
                });

                Button cat_search = findViewById(R.id.cat_search);
                cat_search.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent iv = new Intent(Category.this, MainActivity.class);
                        iv.putExtra("sub_category_name", "" + subCatName);
                        iv.putExtra("sub_category_id", "" + subCatId);
                        startActivity(iv);

                    }
                });

            }
        }
    }

    class CategoryAdapter extends BaseAdapter {
        private final ArrayList data1;
        private final Activity activity;
        private LayoutInflater inflater = null;
        private static final int TYPE_ITEM = 1;
        private static final int TYPE_HEADER = 2;


        CategoryAdapter(Activity a, ArrayList str) {
            activity = a;
            data1 = str;
            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return data1.size();
        }

        @Override
        public Object getItem(int position) {
            return data1.get(position);
        }

        @Override
        public int getItemViewType(int position) {
            if (getItem(position) instanceof CategoryHeaderGetSet) {
                return TYPE_HEADER;
            } else
                return TYPE_ITEM;

        }

        @Override
        public boolean isEnabled(int position) {
            return (getItemViewType(position) == TYPE_ITEM);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }


        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View vi = convertView;

            Typeface opensansregular = Typeface.createFromAsset(activity.getAssets(), "fonts/OpenSans-Regular.ttf");


            int type = getItemViewType(position);
            switch (type) {
                case TYPE_ITEM:
                    vi = inflater.inflate(R.layout.cell_maincategory, parent, false);
                    break;
                case TYPE_HEADER:
                    vi = inflater.inflate(R.layout.header_category, parent, false);
                    break;
            }

            switch (type) {
                case TYPE_ITEM:
                    TextView txt_first = vi.findViewById(R.id.txt_first);
                    TextView txt_subcategory = vi.findViewById(R.id.txt_subcategory);
                    CategorySubItemGetSet item = (CategorySubItemGetSet) data1.get(position);
                    Log.e("namecat", "" + item.getName());
                    txt_subcategory.setText(item.getName());
                    String namefirst = (item.getName());
                    String s1 = String.valueOf(namefirst).substring(0, 1).toUpperCase();
                    txt_first.setText(s1);
                    final ImageView imageView = vi.findViewById(R.id.id_click);
                    if (subCatId.equals(item.getId()))
                        imageView.setVisibility(View.VISIBLE);
                    else imageView.setVisibility(View.GONE);
                    break;
                case TYPE_HEADER:
                    TextView title = vi.findViewById(R.id.txt_name);
                    title.setTypeface(opensansregular, Typeface.BOLD);
                    CategoryHeaderGetSet head = (CategoryHeaderGetSet) data1.get(position);
                    title.setText(head.getName());
                    vi.setActivated(false);
                    vi.setFocusable(false);
                    break;
            }
            return vi;

        }
    }

}