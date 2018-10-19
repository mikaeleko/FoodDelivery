package freaktemplate.fooddelivery;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
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

import static freaktemplate.fooddelivery.MainActivity.changeStatsBarColor;
import static freaktemplate.fooddelivery.MainActivity.checkInternet;
import static freaktemplate.fooddelivery.MainActivity.showErrorDialog;
import static freaktemplate.fooddelivery.MainActivity.tf_opensense_regular;

public class LoginAsDelivery extends AppCompatActivity {

    private TextView txt_title;
    private TextInputEditText edit_email;
    private TextInputEditText edit_pwd;
    private Button btn_login;
    private TextInputLayout inputLayoutEmail, inputLayoutPassword;
    private static final String MY_PREFS_NAME = "Fooddelivery";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_as_delivery);
        getSupportActionBar().hide();

        changeStatsBarColor(LoginAsDelivery.this);


        initViews();

        setFont();
    }


    private void initViews() {
        txt_title = findViewById(R.id.txt_title);
        edit_email = findViewById(R.id.edit_email);
        edit_pwd = findViewById(R.id.edit_pwd);
        btn_login = findViewById(R.id.btn_login);
        inputLayoutEmail = findViewById(R.id.input_layout_email);
        inputLayoutPassword = findViewById(R.id.input_layout_password);

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitform();
            }
        });

        ImageButton ib_back = findViewById(R.id.ib_back);
        ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void setFont() {
        txt_title.setTypeface(tf_opensense_regular);
        edit_email.setTypeface(tf_opensense_regular);
        edit_pwd.setTypeface(tf_opensense_regular);
        btn_login.setTypeface(tf_opensense_regular);

    }

    private boolean validateEmail() {
        String email = edit_email.getText().toString().trim();
        if (email.isEmpty() || !isValidEmail(email)) {
            inputLayoutEmail.setError(getString(R.string.err_msg_email));
            requestFocus(edit_email);
            return false;
        } else {
            inputLayoutEmail.setErrorEnabled(false);
        }
        return true;
    }

    private boolean validatePassword() {
        if (edit_pwd.getText().toString().trim().isEmpty()) {
            inputLayoutPassword.setError(getString(R.string.err_msg_password));
            requestFocus(edit_pwd);
            return false;
        } else {
            inputLayoutPassword.setErrorEnabled(false);
        }
        return true;
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void submitform() {
        if (!validateEmail()) {
            return;
        }
        if (!validatePassword()) {
            return;
        }
        String userEmail = edit_email.getText().toString();
        String password = edit_pwd.getText().toString();
        if (checkInternet(LoginAsDelivery.this))
            submitdata(userEmail, password);

        else {
            showErrorDialog(LoginAsDelivery.this);
        }

    }

    private void submitdata(String userEmail, String password) {

        //creating a string request to send request to the url
        String hp = getString(R.string.link) + getString(R.string.servicepath) + "deliveryboy_login.php?email=" + userEmail + "&password=" + password;
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
                            JSONObject jo_data = obj.getJSONObject("data");
                            if (jo_data.getString("success").equals("1")) {
                                JSONArray ja_login = jo_data.getJSONArray("login");
                                JSONObject jo_detail = ja_login.getJSONObject(0);
                                String id = jo_detail.getString("id");
                                String name = jo_detail.getString("name");
                                String phone = jo_detail.getString("phone");
                                String email = jo_detail.getString("email");
                                String vehicle_no = jo_detail.getString("vehicle_no");
                                String vehicle_type = jo_detail.getString("vehicle_type");

                                saveToSharedPref(id, name, phone, email, vehicle_no, vehicle_type);

                            } else {
                                Toast.makeText(LoginAsDelivery.this, jo_data.getString("login"), Toast.LENGTH_SHORT).show();
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

    private void saveToSharedPref(String id, String name, String phone, String email, String vehicle_no, String vehicle_type) {
        SharedPreferences.Editor edit = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        edit.putBoolean("isDeliverAccountActive", true);
        edit.putString("DeliveryUserId", id);
        edit.putString("DeliveryUserName", name);
        edit.putString("DeliveryUserPhone", phone);
        edit.putString("DeliveryUserEmail", email);
        edit.putString("DeliveryUserVNo", vehicle_no);
        edit.putString("DeliveryUserVType", vehicle_type);
        edit.apply();
        Intent i = new Intent(LoginAsDelivery.this, DeliveryStatus.class);
        startActivity(i);
        finish();

    }


}
