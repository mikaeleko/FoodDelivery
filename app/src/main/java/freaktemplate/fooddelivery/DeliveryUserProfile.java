package freaktemplate.fooddelivery;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import static freaktemplate.fooddelivery.MainActivity.tf_opensense_regular;

public class DeliveryUserProfile extends AppCompatActivity {
    private static final String MY_PREFS_NAME = "Fooddelivery";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_user_profile);
        getSupportActionBar().hide();

        initViews();

    }



    private void initViews() {
        TextView txt_header = findViewById(R.id.txt_header);
        txt_header.setTypeface(tf_opensense_regular);
        TextView txt_name_tittle = findViewById(R.id.txt_name_tittle);
        txt_name_tittle.setTypeface(tf_opensense_regular);
        TextView txt_name = findViewById(R.id.txt_name);
        txt_name.setTypeface(tf_opensense_regular);
        TextView txt_contact_tittle = findViewById(R.id.txt_contact_tittle);
        txt_contact_tittle.setTypeface(tf_opensense_regular);
        TextView txt_contact = findViewById(R.id.txt_contact);
        txt_contact.setTypeface(tf_opensense_regular);
        TextView txt_email_tittle = findViewById(R.id.txt_email_tittle);
        txt_email_tittle.setTypeface(tf_opensense_regular);
        TextView txt_email = findViewById(R.id.txt_email);
        txt_email.setTypeface(tf_opensense_regular);
        TextView txt_vehicle_no_tittle = findViewById(R.id.txt_vehicle_no_tittle);
        txt_vehicle_no_tittle.setTypeface(tf_opensense_regular);
        TextView txt_vehicle_no = findViewById(R.id.txt_vehicle_no);
        txt_vehicle_no.setTypeface(tf_opensense_regular);
        TextView txt_vehicle_type_tittle = findViewById(R.id.txt_vehicle_type_tittle);
        txt_vehicle_type_tittle.setTypeface(tf_opensense_regular);
        TextView txt_vehicle_type = findViewById(R.id.txt_vehicle_type);
        txt_vehicle_type.setTypeface(tf_opensense_regular);

        ImageButton ib_back = findViewById(R.id.ib_back);
        ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        //getting shared preference

        SharedPreferences sp = getSharedPreferences(MY_PREFS_NAME,MODE_PRIVATE);
        txt_name.setText(sp.getString("DeliveryUserName", ""));
        txt_contact.setText(sp.getString("DeliveryUserPhone", ""));
        txt_email.setText(sp.getString("DeliveryUserEmail", ""));
        txt_vehicle_no.setText(sp.getString("DeliveryUserVNo", ""));
        txt_vehicle_type.setText(sp.getString("DeliveryUserVType", ""));
    }
}
