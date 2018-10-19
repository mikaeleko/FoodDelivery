package freaktemplate.fooddelivery;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hbb20.CountryCodePicker;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MIME;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import freaktemplate.Getset.registergetset;

import static freaktemplate.fooddelivery.MainActivity.changeStatsBarColor;
import static freaktemplate.fooddelivery.MainActivity.checkInternet;
import static freaktemplate.fooddelivery.MainActivity.showErrorDialog;
import static freaktemplate.fooddelivery.MainActivity.tf_opensense_regular;

public class Register extends AppCompatActivity {
    private static final String MY_PREFS_NAME = "Fooddelivery";
    private static final int RESULT_LOAD_IMAGE = 1;
    private static final int RESULT_cam_IMAGE = 2;
    private EditText input_phone;
    private EditText input_name;
    private EditText input_email;
    private EditText input_password;
    private Button btn_signup;
    private ImageView img_back;
    private CheckBox checkbox;
    private ProgressDialog progressDialog;
    private String response;
    private ArrayList<registergetset> reglist;
    private String username;
    private String email;
    private String password;
    private String phonenumberfull;
    private String user2;
    private String email2;
    private String phonenumberfull2;
    private String username2;
    private Button camera;
    private Button gallery;
    private ImageView img_user;
    private String picturepath = "";
    private CountryCodePicker ccp;
    private TextView txt_code;
    String status;
    private String imageprofile;
    String id, name, image;
    String strSuccess;
    private TextInputLayout inputLayoutName, inputLayoutEmail, inputLayoutPassword, inputLayoutPhone;
    private static final int REQUEST_EXTERNAL_STORAGE = 2;
    private static final String[] PERMISSIONS_STORAGE = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA

    };
    private String key;


    private static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        changeStatsBarColor(Register.this);


        getIntents();

        setfont();

        initializations();

        setClickListener();
    }

    private void getIntents() {
        Intent iv = getIntent();
        key = iv.getStringExtra("key");
        if (key == null) {
            key = "";
        }
        Log.e("key123", key);
    }


    private void initializations() {
        reglist = new ArrayList<>();
        inputLayoutName = findViewById(R.id.input_layout_name);
        inputLayoutEmail = findViewById(R.id.input_layout_email);
        inputLayoutPassword = findViewById(R.id.input_layout_password);
        inputLayoutPhone = findViewById(R.id.input_layout_phone);
        img_back = findViewById(R.id.img_back);
        btn_signup = findViewById(R.id.btn_signup);
        input_name.addTextChangedListener(new MyTextWatcher(input_name));
        input_email.addTextChangedListener(new MyTextWatcher(input_email));
        input_phone.addTextChangedListener(new MyTextWatcher(input_phone));
        input_password.addTextChangedListener(new MyTextWatcher(input_password));
        txt_code = findViewById(R.id.txt_code);
        ccp = findViewById(R.id.ccp);
        checkbox = findViewById(R.id.checkbox);
        camera = findViewById(R.id.camera);
        gallery = findViewById(R.id.gallery);
        img_user = findViewById(R.id.img_user);
    }


    private void setfont() {
        input_phone = findViewById(R.id.input_phone);
        input_phone.setTypeface(tf_opensense_regular);
        input_name = findViewById(R.id.input_name);
        input_name.setTypeface(tf_opensense_regular);
        input_email = findViewById(R.id.input_email);
        input_email.setTypeface(tf_opensense_regular);
        input_password = findViewById(R.id.input_password);
        input_password.setTypeface(tf_opensense_regular);
        btn_signup = findViewById(R.id.btn_signup);
        btn_signup.setTypeface(tf_opensense_regular);
        TextView txt_title = findViewById(R.id.txt_title);
        txt_title.setTypeface(tf_opensense_regular);
    }

    private void setClickListener() {
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int permission = ActivityCompat.checkSelfPermission(Register.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

                if (permission != PackageManager.PERMISSION_GRANTED) {
                    verifyStoragePermissions(Register.this);
                } else {
                    Intent i = new Intent(Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(i, RESULT_LOAD_IMAGE);
                }
            }
        });
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int permission = ActivityCompat.checkSelfPermission(Register.this, Manifest.permission.CAMERA);

                if (permission != PackageManager.PERMISSION_GRANTED) {
                    // We don't have permission so prompt the user
                    verifyStoragePermissions(Register.this);

                } else {
                    Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                    startActivityForResult(intent, RESULT_cam_IMAGE);
                }
            }
        });

        btn_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitForm();

            }
        });

        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void submitForm() {
        if (!validatephone()) {
            return;
        }
        if (!validateName()) {
            return;
        }
        if (!validateEmail()) {
            return;
        }
        if (!validatePassword()) {
            return;
        }
        if (!checkbox.isChecked()) {
            Toast.makeText(getApplicationContext(), "select terms and conditions", Toast.LENGTH_SHORT).show();
            return;
        }
        if (picturepath.equals("")) {
            Toast.makeText(getApplicationContext(), "Choose Your Profile Picture", Toast.LENGTH_LONG).show();

        } else {
            img_user.setImageBitmap(decodeFile(picturepath));
            Log.d("picturepath", "" + picturepath);
            username = input_name.getText().toString();
            String phonenumber = input_phone.getText().toString();
            password = input_password.getText().toString();
            email = input_email.getText().toString();
            txt_code.setText(ccp.getSelectedCountryCodeWithPlus());
            String pluscode = txt_code.getText().toString();
            phonenumberfull = pluscode + phonenumber;
            Log.e("phonumberfull", phonenumberfull);
            if (checkInternet(Register.this))
                new postingData().execute();
            else {
                showErrorDialog(Register.this);
            }
        }


    }


    private boolean validatecheckbox() {
        return true;
    }

    private boolean validateName() {
        if (input_name.getText().toString().trim().isEmpty()) {
            inputLayoutName.setError(getString(R.string.err_msg_name));
            requestFocus(input_name);
            return false;
        } else {
            inputLayoutName.setErrorEnabled(false);
        }
        return true;
    }

    private boolean validatephone() {

        if (input_phone.getText().toString().trim().isEmpty()) {
            inputLayoutPhone.setError(getString(R.string.err_msg_phone));
            requestFocus(input_phone);
            return false;
        } else {
            inputLayoutPhone.setErrorEnabled(false);
        }
        return true;
    }


    private boolean validateEmail() {
        String email = input_email.getText().toString().trim();
        if (email.isEmpty() || !isValidEmail(email)) {
            inputLayoutEmail.setError(getString(R.string.err_msg_email));
            requestFocus(input_email);
            return false;
        } else {
            inputLayoutEmail.setErrorEnabled(false);
        }
        return true;
    }

    private boolean validatePassword() {
        if (input_password.getText().toString().trim().isEmpty()) {
            inputLayoutPassword.setError(getString(R.string.err_msg_password));
            requestFocus(input_password);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {


            Uri selectedImage = data.getData();
            Bitmap photo;
            if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK) {
                String[] filePathColumn = {MediaStore.MediaColumns.DATA};

                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                picturepath = cursor.getString(columnIndex);

                Log.d("picturepath", "" + picturepath);
                cursor.close();
                try {
                    photo = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                    img_user.setImageBitmap(decodeFile(picturepath));
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if (requestCode == RESULT_cam_IMAGE && resultCode == RESULT_OK && null != data) {
                Log.d("photo", "" + data.getExtras().get("data"));
                photo = (Bitmap) data.getExtras().get("data");
                img_user.setImageBitmap(photo);
                img_user.setImageBitmap(photo);
                Uri tempUri = getImageUri(getApplicationContext(), photo);
                // CALL THIS METHOD TO GET THE ACTUAL PATH
                File finalFile = new File(getRealPathFromURI(tempUri));
                picturepath = String.valueOf(finalFile);


            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }


    private String getRealPathFromURI(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        String temp = cursor.getString(idx);
        cursor.close();
        return temp;
    }

    private Bitmap decodeFile(String path) {
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, o);
            // The new size we want to scale to
            final int REQUIRED_SIZE = 70;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while (o.outWidth / scale / 2 >= REQUIRED_SIZE && o.outHeight / scale / 2 >= REQUIRED_SIZE)
                scale *= 2;

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeFile(path, o2);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;

    }

    class postingData extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(Register.this);
            progressDialog.setMessage("Loading..");
            progressDialog.setCancelable(true);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub
            postdata();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (progressDialog.isShowing())
                progressDialog.dismiss();
            new getlogin().execute();

        }
    }

    private void postdata() {
        // TODO Auto-generated method stub
        HttpClient httpClient = new DefaultHttpClient();
        String boundary = "-------------" + System.currentTimeMillis();


        HttpEntity entity = MultipartEntityBuilder.create().setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                .setBoundary(boundary).addTextBody("email", email, ContentType.create("text/plain", MIME.UTF8_CHARSET))
                .addTextBody("fullname", username, ContentType.create("text/plain", MIME.UTF8_CHARSET))
                .addTextBody("password", password, ContentType.create("text/plain", MIME.UTF8_CHARSET))
                .addTextBody("phone_no", phonenumberfull)
                .addTextBody("referral_code", "1234")
                 .addBinaryBody("file", new File(picturepath), ContentType.create("application/octet-stream"), "filename").build();


        HttpPost httpPost = new HttpPost(getString(R.string.link) + getString(R.string.servicepath) + "userregister.php");
        httpPost.setHeader("Content-type", "multipart/form-data; boundary=" + boundary);
        httpPost.setEntity(entity);
        HttpResponse response1 = null;
        try {
            response1 = httpClient.execute(httpPost);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        HttpEntity result = response1.getEntity();
        if (result != null) {

            try {
                String responseStr = EntityUtils.toString(result).trim();
                Log.e("Response", responseStr);
                response = responseStr;

            } catch (org.apache.http.ParseException | IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }


    class getlogin extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(Register.this);
            progressDialog.setMessage("Loading..");
            progressDialog.setCancelable(true);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub
            reglist.clear();
            try {
                JSONArray jsonArray = new JSONArray(response);
                final JSONObject Obj = jsonArray.getJSONObject(0);
                Log.e("Obj", Obj.toString());
                strSuccess = Obj.getString("status");
                if (Obj.getString("status").equals("Success")) {
                    JSONObject data = Obj.getJSONObject("user_detail");
                    user2 = data.getString("id");
                    email2 = data.getString("email");
                    phonenumberfull2 = data.getString("phone_no");
                    username2 = data.getString("fullname");
                    imageprofile = data.getString("image");
                    Log.e("image111", imageprofile);
                    Log.e("user2", "" + user2);
                    Log.e("Obj1", Obj.toString());
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Toast.makeText(Register.this, Obj.getString("error"), Toast.LENGTH_SHORT).show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                }

            } catch (JSONException e) {
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
            if (progressDialog.isShowing())
                progressDialog.dismiss();
            if (strSuccess.equals("Success")) {
                SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                editor.putString("userid", "" + user2);
                editor.putString("username", "" + username2);
                editor.putString("usermailid", "" + email2);
                editor.putString("usermobileno", "" + phonenumberfull2);
                imageprofile = getString(R.string.link) + getString(R.string.imagepath) + imageprofile;
                editor.putString("imageprofile", "" + imageprofile);
                editor.apply();

                Intent iv;
                switch (key) {
                    case "": {
                        iv = new Intent(Register.this, MainActivity.class);
                        iv.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
                        startActivity(iv);
                        Toast.makeText(Register.this, "Registration Successful", Toast.LENGTH_LONG).show();
                        finish();

                        break;
                    }
                    case "PlaceOrder": {
                        iv = new Intent(Register.this, PlaceOrder.class);
                        iv.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
                        startActivity(iv);
                        Toast.makeText(Register.this, "Registration Successful", Toast.LENGTH_LONG).show();
                        finish();


                        break;
                    }
                    case "review": {
                        iv = new Intent(Register.this, MainActivity.class);
                        iv.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
                        startActivity(iv);
                        Toast.makeText(Register.this, "Registration Successful", Toast.LENGTH_LONG).show();
                        finish();
                        break;
                    }


                }
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
                case R.id.input_phone:
                    validatephone();
                    break;
                case R.id.input_name:
                    validateName();
                    break;
                case R.id.input_email:
                    validateEmail();
                    break;
                case R.id.input_password:
                    validatePassword();
                    break;
                case R.id.checkbox:
                    validatecheckbox();
                    break;

            }
        }
    }

    private static void verifyStoragePermissions(final Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            new AlertDialog.Builder(activity)
                    .setTitle("Storage")
                    .setMessage("Need permission to upload image!")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(activity,
                                    PERMISSIONS_STORAGE,
                                    REQUEST_EXTERNAL_STORAGE);
                        }
                    })
                    .create()
                    .show();


        } else {
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        }
    }


}
