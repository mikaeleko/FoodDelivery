package freaktemplate.fooddelivery;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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
import java.util.List;

import freaktemplate.Getset.menugetset;

import static freaktemplate.fooddelivery.MainActivity.changeStatsBarColor;
import static freaktemplate.fooddelivery.MainActivity.checkInternet;
import static freaktemplate.fooddelivery.MainActivity.showErrorDialog;

public class MenuList extends AppCompatActivity {
    private static ArrayList<menugetset> menulist;
    private String detail_id;
    private ProgressDialog progressDialog;
    private String restaurent_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_list);
        gettingIntents();
        changeStatsBarColor(MenuList.this);

        initializations();
        getData();
    }


    private void initializations() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(restaurent_name);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent iv = new Intent(MenuList.this, Cart.class);
                iv.putExtra("detail_id", detail_id);
                iv.putExtra("restaurent_name", restaurent_name);
                startActivity(iv);
            }
        });
    }


    private void gettingIntents() {
        Intent iv = getIntent();
        detail_id = iv.getStringExtra("detail_id");
        Log.d("Checkdetail_id",""+detail_id);
        restaurent_name = iv.getStringExtra("restaurent_name");
        Log.e("detail_idmenulist", "" + detail_id);
    }


    private void getData() {
        menulist = new ArrayList<>();
        if(checkInternet(MenuList.this))
        new GetDataAsyncTask().execute();
        else showErrorDialog(MenuList.this);
    }


    class GetDataAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MenuList.this);
            progressDialog.setMessage(getString(R.string.loading));
            progressDialog.setCancelable(true);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            menulist.clear();
            URL hp;
            try {
                menulist.clear();
                hp = new URL(getString(R.string.link)+getString(R.string.servicepath) + "/restaurant_menu.php?res_id=" + detail_id);
                Log.e("URLmenu", "" + hp);
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
                Log.e("URL", "" + total);
                JSONArray jObject = new JSONArray(total.toString());
                for (int i = 0; i < jObject.length(); i++) {
                    final JSONObject Obj1;
                    Obj1 = jObject.getJSONObject(i);

                    if (Obj1.getString("status").equals("Success")) {
                        JSONArray data = Obj1.getJSONArray("Menu_Category");
                        for (int iq = 0; iq < data.length(); iq++) {
                            JSONObject jdat = data.getJSONObject(iq);
                            menugetset temp = new menugetset();
                            temp.setId(jdat.getString("id"));
                            temp.setName(jdat.getString("name"));
                            temp.setCreated_at(jdat.getString("created_at"));
                            menulist.add(temp);
                        }

                        Log.e("menulist", menulist.get(0).getId());
                    } else {
                        Log.e("success", "Failed:No data available ");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Toast.makeText(MenuList.this, Obj1.getString("error"), Toast.LENGTH_SHORT).show();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }

                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Log.e("Error",e.getMessage());
            } catch (NullPointerException e) {
                // TODO: handle exception
                Log.e("Error",e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            {
                ViewPager viewPager = findViewById(R.id.viewpager);
                ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
                int i;
                Menufragment menufragment;
                for (i = 0; i < menulist.size(); i++) {
                    int pos = Integer.parseInt(menulist.get(i).getId());
                    menufragment = new Menufragment();
                    menufragment.init(menulist.get(i).getId(),detail_id);
                    adapter.addFragment(menufragment, menulist.get(i).getId(), menulist.get(i).getName(), detail_id, 0);
                    Log.e("data123", menulist.get(i).getName());
                    Log.e("tabedid", menulist.get(i).getId());
                    adapter.getCount();
                }
                viewPager.setAdapter(adapter);

                TabLayout tabLayout = findViewById(R.id.tabs);
                tabLayout.setupWithViewPager(viewPager);

                View root = tabLayout.getChildAt(0);

                if (root instanceof LinearLayout) {
                    ((LinearLayout) root).setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
                    GradientDrawable drawable = new GradientDrawable();
                    drawable.setColor(Color.WHITE);
                    drawable.setSize(1,1);
                    ((LinearLayout) root).setDividerPadding(10);
                    root.setPadding(0,25,0,25);
                    ((LinearLayout) root).setDividerDrawable(drawable);
                }

                ViewGroup vg = (ViewGroup) tabLayout.getChildAt(0);
                int tabsCount = vg.getChildCount();
                for (int j = 0; j < tabsCount; j++) {
                    ViewGroup vgTab = (ViewGroup) vg.getChildAt(j);
                    int tabChildsCount = vgTab.getChildCount();
                    for (int it = 0; it < tabChildsCount; it++) {
                        View tabViewChild = vgTab.getChildAt(it);
                        if (tabViewChild instanceof TextView) {
                            ((TextView) tabViewChild).setTypeface(MainActivity.tf_opensense_regular, Typeface.NORMAL);
                        }
                    }
                }

            }
        }
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        private ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return menulist.size();
        }

        private void addFragment(Fragment fragment, String id, String s, String detail_id, int i) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(s);
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            int index = menulist.indexOf(object);
            if (index == 1) {
                return POSITION_NONE;
            } else {
                return index;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}

