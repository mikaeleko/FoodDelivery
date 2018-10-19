package freaktemplate.Adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import freaktemplate.Getset.reviewgetset;
import freaktemplate.fooddelivery.R;

import static freaktemplate.fooddelivery.MainActivity.tf_opensense_medium;
import static freaktemplate.fooddelivery.MainActivity.tf_opensense_regular;

/**
 * Created by Redixbit 2 on 24-09-2016.
 */

public class reviewadapter extends RecyclerView.Adapter<reviewadapter.MyViewHolder> {
    private final ArrayList<reviewgetset> data1;
    private final Activity activity;


    public class MyViewHolder extends RecyclerView.ViewHolder {
        final TextView txt_name;
        final TextView txt_desc;
        final TextView txt_rating;
        final ImageView imageview;

        MyViewHolder(View view) {
            super(view);
            txt_name = view.findViewById(R.id.txt_name);
            txt_desc = view.findViewById(R.id.txt_desc);
            txt_rating = view.findViewById(R.id.txt_rating);
            imageview = view.findViewById(R.id.image_url1);

        }
    }

    public reviewadapter(Activity a, ArrayList<reviewgetset> str) {
        activity = a;
        data1 = str;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cell_review, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        reviewgetset movie = data1.get(position);

        holder.txt_name.setText(data1.get(position).getUsername() + " : ");
        holder.txt_name.setTypeface(tf_opensense_regular);

        holder.txt_desc.setText(Html.fromHtml(data1.get(position).getReview_text()));
        holder.txt_desc.setTypeface(tf_opensense_regular);


        holder.txt_rating.setText((data1.get(position).getRatting() + "/5"));
        holder.txt_rating.setTypeface(tf_opensense_medium);


        String image = data1.get(position).getImage().replace(" ", "%20");
        String imagePath = activity.getString(R.string.link)+activity.getString(R.string.imagepath)+image;

        Log.d("revimage", imagePath);
      try {
          Picasso.with(activity)
                  .load(imagePath).fit().into(holder.imageview);
      }catch (IllegalArgumentException e)
      {
          e.printStackTrace();
      }

    }

    @Override
    public int getItemCount() {
        return data1.size();
    }
}
