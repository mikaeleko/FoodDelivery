package freaktemplate.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import freaktemplate.Getset.CitylistGetSet;
import freaktemplate.fooddelivery.R;

import static android.content.Context.MODE_PRIVATE;
import static freaktemplate.fooddelivery.MainActivity.tf_opensense_regular;

/**
 * Created by RedixbitUser on 3/23/2018.
 */

public class CityListAdapter extends BaseAdapter {
    private final ArrayList<CitylistGetSet> data1;
    private LayoutInflater inflater = null;
    private final SharedPreferences sharedpreferences;
    private static final String MyPREFERENCES = "Fooddelivery" ;

    public CityListAdapter(Activity a, ArrayList<CitylistGetSet> nearbylist,Context context1) {
        Activity activity = a;
        data1 = nearbylist;
        sharedpreferences = context1.getSharedPreferences(MyPREFERENCES, MODE_PRIVATE);
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
    public long getItemId(int position) {
        return Long.parseLong(data1.get(position).getId());
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View vi = convertView;

        ImageView selected_img;
        if (convertView == null) {
            vi = inflater.inflate(R.layout.cell_citylist, parent,false);
            TextView txt_name = vi.findViewById(R.id.txt_name);
            selected_img = vi.findViewById(R.id.img_selected);
            txt_name.setText((data1.get(position).getName()));
            txt_name.setTypeface(tf_opensense_regular);
        }
        else {
            TextView txt_name = vi.findViewById(R.id.txt_name);
            selected_img = vi.findViewById(R.id.img_selected);
            txt_name.setText((data1.get(position).getName()));
            txt_name.setTypeface(tf_opensense_regular);
        }
        String cityName = sharedpreferences.getString("CityName", null);
        if (cityName != null && !cityName.isEmpty() && !cityName.equals("null") && cityName.equals(data1.get(position).getName())) {
            selected_img.setVisibility(View.VISIBLE);
        }else{

            selected_img.setVisibility(View.GONE);
        }
        return vi;
    }
}
