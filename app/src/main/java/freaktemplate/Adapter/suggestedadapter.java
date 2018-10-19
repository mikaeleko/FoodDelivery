package freaktemplate.Adapter;

import android.app.Activity;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Random;

import freaktemplate.Getset.restaurentGetSet;
import freaktemplate.fooddelivery.R;

import static freaktemplate.fooddelivery.MainActivity.tf_opensense_regular;

/**
 * Created by Redixbit on 20-04-2017.
 */
public class suggestedadapter extends RecyclerView.Adapter<suggestedadapter.MyViewHolder> {
    private final ArrayList<restaurentGetSet> moviesList;
    private final Activity activity;
    private Random randomGenerator;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        final TextView txt_name;
        final TextView txt_category;
        final TextView txt_distance;
        final TextView txt_rating;
        final TextView txt_status;
        final TextView time;
        public TextView txt_distance1;
        final ImageView imageview;


        MyViewHolder(View view) {
            super(view);
            txt_name = view.findViewById(R.id.txt_name);
            txt_category = view.findViewById(R.id.txt_category);
            txt_distance = view.findViewById(R.id.txt_distance);
            txt_rating = view.findViewById(R.id.txt_rating);
            txt_status = view.findViewById(R.id.txt_status);
            imageview= view.findViewById(R.id.image);
            time= view.findViewById(R.id.time);
            randomGenerator = new Random();
        }
    }


    public suggestedadapter(Activity a, ArrayList<restaurentGetSet> moviesList, String cat) {
        activity =a;
        this.moviesList = moviesList;
        String category = cat;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cell_home, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {


        int index = randomGenerator.nextInt(moviesList.size());
        Log.d("index",""+index);
        restaurentGetSet data = moviesList.get(position);
        holder.txt_name.setText((data.getName()));
        holder.txt_name.setTypeface(tf_opensense_regular);
        if (data.getCategory() != null) {
            StringBuilder sb = new StringBuilder();
            for (String s:data.getCategory()) {
                sb.append(s).append(",");
            }
            holder.txt_category.setText(sb.toString().replace("\"", "").replace("[", "").replace("]", ""));
            holder.txt_category.setTypeface(tf_opensense_regular);
        }



        holder.txt_distance.setText((data.getDelivery_time()));
        holder.txt_distance.setTypeface(tf_opensense_regular);

        holder.txt_rating.setText(""+Float.parseFloat(data.getRatting()));
        holder.txt_rating.setTypeface(tf_opensense_regular);


        String status =data.getRes_status();
        Log.e("res_status",""+status);
        if(status.equals("open"))
        {

            holder.txt_status.setText("OPEN :Open till  ");
            holder.txt_status.setTextColor(Color.parseColor("#14B457"));
            holder.txt_status.setTypeface(tf_opensense_regular);

            holder.time.setText((data.getOpen_time()));
            holder.time.setTextColor(Color.parseColor("#14B457"));
            holder.time.setTypeface(tf_opensense_regular);
        }
        else if(status.equals("closed"))
        {

            holder.txt_status.setText("CLOSE : open at ");
            holder.txt_status.setTextColor(Color.parseColor("#F73E46"));
            holder.txt_status.setTypeface(tf_opensense_regular);

            holder.time.setTypeface(tf_opensense_regular);
            holder.time.setTextColor(Color.parseColor("#F73E46"));
            holder.time.setText((data.getClose_time()));

        }

        String image = data.getImage().replace(" ", "%20");

        AlphaAnimation anim = new AlphaAnimation(0,1);
        anim.setInterpolator(new LinearInterpolator());
        anim.setRepeatCount(Animation.ABSOLUTE);
        anim.setDuration(1000);
        Picasso.with(activity)
                .load(activity.getString(R.string.link)+activity.getString(R.string.imagepath)+image)
               .into(holder.imageview);
        holder.imageview.startAnimation(anim);
    }

    @Override
    public int getItemCount() {
        return moviesList.size();
    }
}
