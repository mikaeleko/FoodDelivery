package freaktemplate.fooddelivery;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.hbb20.CountryCodePicker;

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
import java.util.Arrays;
import java.util.List;

import freaktemplate.Getset.registergetset;

import static freaktemplate.fooddelivery.MainActivity.changeStatsBarColor;
import static freaktemplate.fooddelivery.MainActivity.checkInternet;
import static freaktemplate.fooddelivery.MainActivity.showErrorDialog;
import static freaktemplate.fooddelivery.MainActivity.tf_opensense_regular;

/**
 * Created by Redixbit 2 on 03-10-2016.
 */
public class Login extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String MY_PREFS_NAME = "Fooddelivery";
    private static final int RC_SIGN_IN = 0;
    private static final String TAG = "MainActivity";
    //facebook
    private EditText edt_email;
    private EditText edt_pwd;
    private TextView btn_register;
    private Button btn_login;
    private Button btn_login_delivery;
    private Button btn_fb;
    private String method;
    private String user2;
    private String imageprofile;
    private String key;
    private String email2;
    private String username2;
    private String phonenumberfull2;
    private String ppic;
    private String name;
    private String email;
    private String username;
    private String password;
    private String imagefb;
    //google
    private Button btn_google;
    private String personname;
    private String personemail;
    private String personPhotoUrl;
    private RelativeLayout rel_center;
    private String phoneNumber;
    private TextInputLayout inputLayoutEmail, inputLayoutPassword;
    private GoogleApiClient mGoogleApiClient;
    private CallbackManager callbackManager;
    private List<String> permissionNeedsFacebook;
    private boolean isSuccess = false;

    private static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        changeStatsBarColor(Login.this);


        gettingIntents();

        initializations();


    }

    private void gettingIntents() {
        //getting intent
        Intent iv = getIntent();
        key = iv.getStringExtra("key");
        if (key == null) {
            key = "";
        }
        String checknumber = iv.getStringExtra("number");

    }

    private void initializations() {

        //initialize
        rel_center = findViewById(R.id.rel_center);
        edt_email = findViewById(R.id.edit_email);
        edt_pwd = findViewById(R.id.edit_pwd);
        btn_register = findViewById(R.id.btn_register);
        edt_email.addTextChangedListener(new MyTextWatcher(edt_email));
        edt_pwd.addTextChangedListener(new MyTextWatcher(edt_pwd));
        btn_login = findViewById(R.id.btn_login);
        btn_login_delivery = findViewById(R.id.btn_login_delivery);
        btn_fb = findViewById(R.id.btn_fb);
        btn_google = findViewById(R.id.btn_google);
        inputLayoutEmail = findViewById(R.id.input_layout_email);
        inputLayoutPassword = findViewById(R.id.input_layout_password);

        ArrayList<registergetset> login = new ArrayList<>();

        setfont();

        ImageButton ib_back = findViewById(R.id.ib_back);
        ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        //setting facebook login
        callbackManager = CallbackManager.Factory.create();
        permissionNeedsFacebook = Arrays.asList("email");
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                getUserDetail(loginResult);
            }

            @Override
            public void onCancel() {
                Log.e("check1", "Cancelled by user");

            }

            @Override
            public void onError(FacebookException error) {
                Log.e("check1", error.getMessage());

            }
        });
        btn_fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                method = "facebook";
                ViewDialog alert = new ViewDialog();
                alert.showDialog(Login.this);
            }
        });


        //setting google sign in
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        btn_google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                method = "google";
                ViewDialog alert = new ViewDialog();
                alert.showDialog(Login.this);
            }
        });


        //register new profile
        btn_register.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                String prodel = "new";
                SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                editor.putString("delete", "" + prodel);
                editor.apply();
                Intent iv = new Intent(Login.this, Register.class);
                iv.putExtra("key", "" + key);
                startActivity(iv);
            }
        });


        //login for existing user
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                String prodel = "new";
                SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                editor.putString("delete", "" + prodel);
                editor.apply();
                method = "login";
                submitform();
            }
        });

        //login for delivery boy

        btn_login_delivery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Login.this, LoginAsDelivery.class);
                startActivity(i);
            }
        });


    }


    private boolean isValidPhoneNumber(CharSequence phoneNumber) {
        return !TextUtils.isEmpty(phoneNumber) && Patterns.PHONE.matcher(phoneNumber).matches();
    }


    private boolean validateUsing_libphonenumber(String countryCode, String phNumber) {
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        String isoCode = phoneNumberUtil.getRegionCodeForCountryCode(Integer.parseInt(countryCode));
        Phonenumber.PhoneNumber phoneNumber = null;
        try {
            //phoneNumber = phoneNumberUtil.parse(phNumber, "IN");  //if you want to pass region code
            phoneNumber = phoneNumberUtil.parse(phNumber, isoCode);
        } catch (NumberParseException e) {
            System.err.println(e);
        }
        boolean isValid = phoneNumberUtil.isValidNumber(phoneNumber);
        if ((checkInternet(Login.this))) {
            if (isValid) {
                rel_center.setEnabled(true);
                if (method.equals("facebook")) {
                    String prodel = "new";
                    SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                    editor.putString("myfbpic", "" + method);
                    editor.putString("delete", "" + prodel);
                    editor.apply();
                    LoginManager.getInstance().logInWithReadPermissions(Login.this, permissionNeedsFacebook);

                } else {
                    String prodel = "new";
                    SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                    editor.putString("delete", "" + prodel);
                    editor.apply();
                    Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                    startActivityForResult(signInIntent, RC_SIGN_IN);
                }
                String internationalFormat = phoneNumberUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
                Toast.makeText(this, getString(R.string.txt_valid_phone_number) + internationalFormat, Toast.LENGTH_LONG).show();
                return true;
            } else {
                Toast.makeText(this, getString(R.string.txt_invalid_phonenumber) + phoneNumber, Toast.LENGTH_LONG).show();
                return false;
            }
        } else {

            showErrorDialog(Login.this);
            return false;
        }

    }


    private void setfont() {
        edt_email = findViewById(R.id.edit_email);
        edt_email.setTypeface(tf_opensense_regular);
        edt_pwd = findViewById(R.id.edit_pwd);
        edt_pwd.setTypeface(tf_opensense_regular);
        btn_register = findViewById(R.id.btn_register);
        btn_register.setTypeface(tf_opensense_regular);
        edt_email.addTextChangedListener(new MyTextWatcher(edt_email));
        edt_pwd.addTextChangedListener(new MyTextWatcher(edt_pwd));
        btn_login = findViewById(R.id.btn_login);
        btn_login.setTypeface(tf_opensense_regular);
        btn_login_delivery.setTypeface(tf_opensense_regular);
        btn_fb = findViewById(R.id.btn_fb);
        btn_fb.setTypeface(tf_opensense_regular);
        btn_google = findViewById(R.id.btn_google);
        btn_google.setTypeface(tf_opensense_regular);
        TextView txt_title = findViewById(R.id.txt_title);
        txt_title.setTypeface(tf_opensense_regular);

    }


    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }


    private void getUserDetail(LoginResult loginResult) {
        GraphRequest data_request = GraphRequest.newMeRequest(
                loginResult.getAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject json_object,
                            GraphResponse response) {
                        Log.e("User Data", json_object.toString());
                        String json = json_object.toString();
                        try {
                            JSONObject profile = new JSONObject(json);
                            // getting name of the user
                            name = profile.getString("name");
                            name = name.replace(" ", "%20");
                            // getting email of the user
                            String regId = profile.getString("id");
                            email = profile.getString("email");
                            JSONObject picture = profile.getJSONObject("picture");
                            JSONObject data = picture.getJSONObject("data");
                            ppic = data.getString("url");
                            if (name != null) {
                                if (ppic != null) {
                                    imagefb =  regId ;
                                    Log.d("fbimage", "" + imagefb);
                                    email = email.replace(" ", "%20");
                                    new getlogin().execute();
                                }
                            }
                        } catch (JSONException e) {
                            Log.e("Error", e.getMessage());
                        }

                    }

                });
        Bundle permission_param = new Bundle();
        permission_param.putString("fields", "id,name,email,picture");
        data_request.setParameters(permission_param);
        data_request.executeAsync();
    }


    private void submitform() {
        if (!validateEmail()) {
            return;
        }
        if (!validatePassword()) {
            return;
        }
        username = edt_email.getText().toString();
        password = edt_pwd.getText().toString();
        if (checkInternet(Login.this))
            new getlogin().execute();
        else {
            showErrorDialog(Login.this);
        }

    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private boolean validateEmail() {
        String email = edt_email.getText().toString().trim();
        if (email.isEmpty() || !isValidEmail(email)) {
            inputLayoutEmail.setError(getString(R.string.err_msg_email));
            requestFocus(edt_email);
            return false;
        } else {
            inputLayoutEmail.setErrorEnabled(false);
        }
        return true;
    }

    private boolean validatePassword() {
        if (edt_pwd.getText().toString().trim().isEmpty()) {
            inputLayoutPassword.setError(getString(R.string.err_msg_password));
            requestFocus(edt_pwd);
            return false;
        } else {
            inputLayoutPassword.setErrorEnabled(false);
        }
        return true;
    }

    @Override
    public void onConnected(Bundle bundle) {
        boolean mSignInClicked = false;
        Toast.makeText(this, "User is Connect", Toast.LENGTH_LONG).show();

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

        // Result returned numberOfRecords launching the Intent numberOfRecords GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            int statusCode = result.getStatus().getStatusCode();
            Log.d("statuscode", "" + statusCode);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.e(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            Log.e(TAG, "display name: " + acct);
            personname = acct.getDisplayName().replace(" ", "%20");
            try {
                personPhotoUrl = acct.getPhotoUrl().toString().replace(" ", "%20").replace("https://","");
            } catch (NullPointerException e) {
                e.printStackTrace();

            }
            personemail = acct.getEmail().replace(" ", "%20");
            String googleid = acct.getId().replace(" ", "%20");

            new getlogin().execute();


        } else {
            Log.e(TAG, "handleSignInResult:" + result.isSuccess());
        }
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    class ViewDialog {
        void showDialog(Activity activity) {
            final Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.newnumberdialog);
            final TextView tvIsValidPhone = dialog.findViewById(R.id.tvIsValidPhone);
            final EditText edtPhone = dialog.findViewById(R.id.edtPhoneNumber);
            TextView txt_description = dialog.findViewById(R.id.txt_description);
            txt_description.setTypeface(tf_opensense_regular);

            Button btnValidate = dialog.findViewById(R.id.btnValidate);
            final TextView txt_code = dialog.findViewById(R.id.txt_code);
//            final CountryCodePicker ccp = dialog.findViewById(R.id.ccp);
            btnValidate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    txt_code.setText(ccp.getSelectedCountryCodeWithPlus());
                    txt_code.setText("+62");
                    String countryCode = txt_code.getText().toString();
                    phoneNumber = edtPhone.getText().toString().trim();
                    if (countryCode.length() > 0 && phoneNumber.length() > 0) {
                        if (isValidPhoneNumber(phoneNumber)) {
                            boolean status = validateUsing_libphonenumber(countryCode, phoneNumber);
                            if (status) {
                                tvIsValidPhone.setText(getString(R.string.txt_valid_phone_number));
                            } else {
                                tvIsValidPhone.setText(R.string.txt_invalid_phonenumber);
                            }
                        } else {
                            tvIsValidPhone.setText(R.string.txt_invalid_phonenumber);
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.txt_phone_required, Toast.LENGTH_SHORT).show();
                    }
                }
            });
            Button btn_cancel = dialog.findViewById(R.id.btn_cancel);
            btn_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        }
    }

    class getlogin extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub
            URL hp = null;
            try {
                switch (method) {
                    case "login":
                        hp = new URL(getString(R.string.link) + getString(R.string.servicepath) + "userlogin.php?email=" + username + "&password=" + password);
                        break;
                    case "facebook":
                        hp = new URL(getString(R.string.link) + getString(R.string.servicepath) + "userlogin.php?login_type=Facebook&fullname=" + name + "&email=" + email + "&phone_no=" + phoneNumber + "&referral_code=" + "1234" + "&image=" + imagefb);
                        break;
                    case "google":
                        hp = new URL(getString(R.string.link) + getString(R.string.servicepath) + "userlogin.php?login_type=Google&fullname=" + personname + "&email=" + personemail + "&phone_no=" + phoneNumber + "&referral_code=" + "1234" + "&image=" + personPhotoUrl);
                        break;
                }
                Log.e("URLdetail", "" + hp);
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
                JSONArray jsonArray = new JSONArray(total.toString());
                final JSONObject Obj = jsonArray.getJSONObject(0);
                Log.e("Obj", Obj.toString());
                if (Obj.getString("status").equals("Success")) {
                    isSuccess = true;
                    JSONObject data = Obj.getJSONObject("user_detail");
                    user2 = data.getString("id");
                    email2 = data.getString("email");
                    phonenumberfull2 = data.getString("phone_no");
                    username2 = data.getString("fullname");
                    imageprofile = data.getString("image");
                    Log.e("image111", imageprofile);
                    Log.e("user2", "" + user2);
                    Log.e("Obj1", Obj.toString());
                } else if (Obj.getString("status").equals("Failed")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Toast.makeText(Login.this, Obj.getString("error"), Toast.LENGTH_SHORT).show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                }

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                e.getMessage();
            } catch (NullPointerException e) {
                // TODO: handle exception
                e.getMessage();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);


            if (isSuccess) {
                SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME,
                        MODE_PRIVATE).edit();
                editor.putString("userid", "" + user2);
                editor.putString("username", "" + username2);
                editor.putString("usermailid", "" + email2);
                editor.putString("usermobileno", "" + phonenumberfull2);
                if (method.equals("login")) {
                    imageprofile = getString(R.string.link) + getString(R.string.imagepath) + imageprofile;
                    editor.putString("imageprofile", "" + imageprofile);
                } else {
                    editor.putString("imageprofile", "" + imageprofile);
                }
                editor.apply();

                switch (key) {
                    case "": {
                        Intent iv = new Intent(Login.this, MainActivity.class);
                        iv.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
                        startActivity(iv);
                        finish();
                        break;
                    }
                    case "PlaceOrder": {
                        Intent iv = new Intent(Login.this, PlaceOrder.class);
                        iv.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
                        startActivity(iv);
                        finish();
                        break;
                    }
                    case "review": {
                        Intent iv = new Intent(Login.this, MainActivity.class);
                        iv.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
                        startActivity(iv);
                        break;
                    }
                }
                Toast.makeText(Login.this, "Login Successful", Toast.LENGTH_LONG).show();
            }
        }

    }

    private class MyTextWatcher implements TextWatcher {

        private final View view;

        private MyTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.input_email:
                    validateEmail();
                    break;
                case R.id.input_password:
                    validatePassword();
                    break;
            }
        }
    }
}


