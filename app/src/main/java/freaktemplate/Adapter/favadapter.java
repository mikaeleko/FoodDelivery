package freaktemplate.Adapter;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Locale;

import freaktemplate.Getset.favgetset;
import freaktemplate.fooddelivery.R;

import static freaktemplate.fooddelivery.MainActivity.tf_opensense_medium;
import static freaktemplate.fooddelivery.MainActivity.tf_opensense_regular;

/**
 * Created by Redixbit 2 on 09-09-2016.
 */


public class favadapter extends BaseAdapter {

    private final ArrayList<favgetset> data1;
    private LayoutInflater inflater = null;


    public favadapter(Activity a, ArrayList<favgetset> str)   {
        Activity activity = a;
        data1 = str;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View vi = convertView;


        if (convertView == null) {
            vi = inflater.inflate(R.layout.cell_favourite, parent,false);
        }

        Spanned namefirst = Html.fromHtml(data1.get(position).getName());
        String s = String.valueOf(namefirst).substring(0, 1).toUpperCase();


        TextView txt_first = vi.findViewById(R.id.txt_first);
        txt_first.setText(s);
        txt_first.setTypeface(tf_opensense_regular);

        TextView txt_name = vi.findViewById(R.id.txt_name);
        txt_name.setText((data1.get(position).getName()));
        txt_name.setTypeface(tf_opensense_medium);
        TextView txt_address = vi.findViewById(R.id.txt_category);
        txt_address.setText(data1.get(position).getAddress());
        txt_address.setTypeface(tf_opensense_regular);
        TextView txt_distance = vi.findViewById(R.id.txt_distance);
        txt_distance.setTypeface(tf_opensense_regular);
        try {
            double distance = Double.parseDouble(data1.get(position).getDistance());
            txt_distance.setText("" + String.format(Locale.ENGLISH,"%.1f", distance) + " Km");
        }catch (NumberFormatException e)
        {
       e.printStackTrace();
        }

        return vi;
    }

}

