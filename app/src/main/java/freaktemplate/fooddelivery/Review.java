package freaktemplate.fooddelivery;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

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

import freaktemplate.Adapter.reviewadapter;
import freaktemplate.Getset.reviewgetset;
import freaktemplate.utils.RecyclerTouchListener;

import static freaktemplate.fooddelivery.MainActivity.changeStatsBarColor;
import static freaktemplate.fooddelivery.MainActivity.checkInternet;
import static freaktemplate.fooddelivery.MainActivity.showErrorDialog;
import static freaktemplate.fooddelivery.MainActivity.tf_opensense_regular;

public class Review extends AppCompatActivity {
    private static final String MyPREFERENCES = "Fooddelivery";
    private ProgressDialog progressDialog;
    private ArrayList<reviewgetset> reviewlist;
    private String Error;
    private String detail_id;
    private Button btn_add_review;
    private View layout12;
    private String uservalue;
    private EditText edt_review;
    private RatingBar rate;
    private String usercomment;
    private String userrate;
    private TextView txt_ratenumber;
    private RelativeLayout rel_title;
    private String revmsg;
    private String useremail;
    private RelativeLayout rl_dialoguser;
    private FloatingActionButton fab;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        changeStatsBarColor(Review.this);


        getIntents();


        initalization();

        onClickListners();

    }

    private void initalization() {
        rl_dialoguser = findViewById(R.id.rl_infodialog);
        rel_title = findViewById(R.id.rel_title);
        fab = findViewById(R.id.fab);

        ((TextView) findViewById(R.id.txt_title)).setTypeface(tf_opensense_regular);

        reviewlist = new ArrayList<>();
        ImageButton ib_back = findViewById(R.id.ib_back);
        ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //getting review detail
        if (checkInternet(Review.this))
            new getreviewdetail().execute();
        else showErrorDialog(Review.this);
    }

    private void getIntents() {
        Intent iv = getIntent();
        detail_id = iv.getStringExtra("detail_id");
        String name = iv.getStringExtra("name");
    }


    private void onClickListners() {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.2F);
                fab.startAnimation(buttonClick);
                rel_title.setEnabled(false);
                SharedPreferences prefs = getSharedPreferences(MyPREFERENCES, MODE_PRIVATE);
                Log.e("user", "" + prefs.getString("userid", null));
                // check user is created or not
                // if user is already logged in
                if (prefs.getString("userid", null) != null) {
                    uservalue = prefs.getString("userid", null);
                    useremail = prefs.getString("usermailid", null);
                    if (uservalue.equals("delete")) {
                        showSnack();
                    } else {
                        showReviewDialog();
                    }
                } else {
                    showSnack();
                }
            }
        });


    }

    private void showReviewDialog() {
        final Dialog dialog = new Dialog(Review.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.givereview);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        edt_review = dialog.findViewById(R.id.txt_description);
        txt_ratenumber = dialog.findViewById(R.id.txt_ratenumber);
        rate = dialog.findViewById(R.id.rate1234);
        String rb = String.valueOf(rate);
        rate.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                // Auto-generated
                Log.e("rate", "" + rating);
                userrate = String.valueOf(rate.getRating());
                txt_ratenumber.setText(userrate);
            }
        });
        Button btn_submit = dialog.findViewById(R.id.btn_submit);
        btn_submit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                try {
                    usercomment = edt_review.getText().toString().replace(" ", "%20");
                    userrate = String.valueOf(rate.getRating());
                    if (usercomment.equals(null)) {
                        usercomment = "";
                    }
                } catch (NullPointerException e) {
                    // TODO: handle exception
                }
                Log.e("comment", "" + usercomment);
                Log.e("rate", "" + userrate);
                if (usercomment.equals("")) {
                    edt_review.setError("Review Please");
                } else {
                    if (checkInternet(Review.this)) {
                        dialog.dismiss();
                        new getRateDetail().execute();
                    } else showErrorDialog(Review.this);

                }
            }
        });
        Button btn_cancel = dialog.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void showSnack() {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.coordinator), "You have to login for give review", Snackbar.LENGTH_LONG);

        //Adding action to snackbar
        snackbar.setAction("Log In", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iv = new Intent(Review.this, Login.class);
                iv.putExtra("key", "review");
                startActivity(iv);
            }
        });

        //Customizing colors
        snackbar.setActionTextColor(Color.WHITE);
        View view1 = snackbar.getView();
        TextView textView = view1.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.GREEN);

        //Displaying snackbar
        snackbar.show();
    }

    private void getdetailforNearMe() {
        // TODO Auto-generated method stub
        URL hp = null;
        try {
            reviewlist.clear();

            hp = new URL(getString(R.string.link) + getString(R.string.servicepath) + "getrestaurant_review.php?res_id=" + detail_id);

            Log.d("URLrev", "" + hp);
            URLConnection hpCon = hp.openConnection();
            hpCon.connect();
            InputStream input = hpCon.getInputStream();
            Log.d("input", "" + input);
            BufferedReader r = new BufferedReader(new InputStreamReader(input));
            String x = "";
            x = r.readLine();
            StringBuilder total = new StringBuilder();

            while (x != null) {
                total.append(x);
                x = r.readLine();
            }


            Log.e("URL", "" + total);
            JSONArray jsonArray = new JSONArray(total.toString());
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            if (jsonObject.getString("status").equals("Success")) {
                JSONArray data = jsonObject.getJSONArray("Reviews");
                for (int i = 0; i < data.length(); i++) {
                    JSONObject rev_detail = data.getJSONObject(i);
                    reviewgetset temp = new reviewgetset();
                    temp.setId(rev_detail.getString("id"));
                    temp.setUsername(rev_detail.getString("username"));
                    temp.setImage(rev_detail.getString("image"));
                    temp.setReview_text(rev_detail.getString("review_text"));
                    temp.setRatting(rev_detail.getString("ratting"));
                    reviewlist.add(temp);
                }
            }

        } catch (JSONException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Error = e.getMessage();
        } catch (NullPointerException e) {
            // TODO: handle exception
            Error = e.getMessage();
        }
    }

    class getreviewdetail extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(Review.this);
            progressDialog.setMessage("Loading");
            progressDialog.setCancelable(true);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            getdetailforNearMe();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
                if (Error != null) {
                    Snackbar.make(findViewById(R.id.coordinator), "No review Available", Snackbar.LENGTH_LONG).show();
                } else {
                    RecyclerView listview = findViewById(R.id.listview);
                    reviewadapter adapter = new reviewadapter(Review.this, reviewlist);
                    RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getApplicationContext(), 1);
                    listview.setLayoutManager(mLayoutManager);
                    listview.setItemAnimator(new DefaultItemAnimator());
                    listview.setAdapter(adapter);
                    listview.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), listview, new RecyclerTouchListener.ClickListener() {

                        @Override
                        public void onClick(View view, int position) {

                            openReviewDialog(reviewlist, position);

                        }

                        @Override
                        public void onLongClick(View view, int position) {

                        }
                    }));

                }
            }
        }
    }

    private void openReviewDialog(ArrayList<reviewgetset> reviewlist, int position) {
        final Dialog dialog = new Dialog(Review.this);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.custom_review_dialog);
        TextView txt_name = dialog.findViewById(R.id.txt_nameuser);
        Button btn_ok = dialog.findViewById(R.id.btn_ok);
        txt_name.setText(reviewlist.get(position).getUsername());
        txt_name.setTypeface(tf_opensense_regular);
        try {
            RatingBar rb = dialog.findViewById(R.id.rate1234);
            rb.setRating(Float.parseFloat(reviewlist.get(position).getRatting()));
            rb.setClickable(false);
            rb.setIsIndicator(true);
            rb.setFocusable(false);
        } catch (NumberFormatException e) {
            // TODO: handle exception
        }

        TextView txt_comment_desc = dialog.findViewById(R.id.txt_desc);
        txt_comment_desc.setText(reviewlist.get(position).getReview_text());
        txt_comment_desc.setTypeface(tf_opensense_regular);
        ImageView img_user = dialog.findViewById(R.id.img_my);
        String image = reviewlist.get(position).getImage().replace(" ", "%20");
        String imagePath = getString(R.string.link)+getString(R.string.imagepath)+image;
        try {

            Picasso.with(Review.this)
                    .load(imagePath)
                    .into(img_user);
            Log.e("image_click_review", "" + image);
        }catch (IllegalArgumentException e)
        {
            e.printStackTrace();
        }
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();

    }

    class getRateDetail extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... params) {
            URL hp = null;
            try {
                hp = new URL(getString(R.string.link) + getString(R.string.servicepath) + "postrestaurant_review.php?" + "res_id=" + detail_id + "&ratting=" + userrate.replace(".0", "") + "&user_id=" + uservalue + "&review_text=" + usercomment);
                Log.e("userurl", "" + hp);
                URLConnection hpCon = hp.openConnection();
                hpCon.connect();
                InputStream input = hpCon.getInputStream();
                Log.d("input", "" + input);
                BufferedReader r = new BufferedReader(new InputStreamReader(input));
                String x = "";
                // x = r.readLine();
                StringBuilder total = new StringBuilder();
                while (x != null) {
                    total.append(x);
                    x = r.readLine();
                }

                Log.d("totalid", "" + total);
                JSONArray response = new JSONArray(total.toString());
                JSONObject stat = response.getJSONObject(0);
                revmsg = stat.getString("status");
                Log.d("totalid", "" + revmsg);
            } catch (JSONException | IOException e) {
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
            if (revmsg.equals("Success")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Review.this);
                builder.setMessage("Thank you for your feedback")
                        .setTitle("Thanks");
                builder.setNeutralButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {

                                dialog.cancel();
                                rl_dialoguser.setVisibility(View.GONE);
                                if (checkInternet(Review.this))
                                    new getreviewdetail().execute();
                                else showErrorDialog(Review.this);
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            } else if (revmsg.equals("Duplicate rating detail")) {

                Toast.makeText(Review.this, "Already feedback is given", Toast.LENGTH_LONG).show();
            }
        }

    }

}