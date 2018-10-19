package freaktemplate.Adapter;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.LinearLayoutManager;
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

import freaktemplate.Getset.restaurentGetSet;
import freaktemplate.fooddelivery.MainActivity;
import freaktemplate.fooddelivery.R;

import static freaktemplate.fooddelivery.MainActivity.tf_opensense_regular;

/**
 * Created by Redixbit 2 on 30-08-2016.
 */
public class restaurentadapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ITEM = 1;
    public final ArrayList<restaurentGetSet> moviesList;
    private final Activity activity;
    private OnLoadMoreListener onLoadMoreListener;
    private boolean isLoading;


    public restaurentadapter(RecyclerView recyclerView, Activity a, ArrayList<restaurentGetSet> moviesList) {
        activity = a;
        this.moviesList = moviesList;
        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleThreshold = linearLayoutManager.getChildCount();
                int totalItemCount = linearLayoutManager.getItemCount();
                int lastVisibleItem = linearLayoutManager.findFirstVisibleItemPosition();
                final int lastItem = lastVisibleItem + visibleThreshold;

                Log.e("Check", isLoading + "  " + "Total Item Count " + totalItemCount + "lastVisibleItem " + lastVisibleItem + "visible threshold " + visibleThreshold);
                if (totalItemCount >= MainActivity.numberOfRecord) {
                    if (!isLoading && totalItemCount == (lastVisibleItem + visibleThreshold)) {
                        if (onLoadMoreListener != null) {
                            onLoadMoreListener.onLoadMore();
                        }
                        isLoading = true;

                    }
                }

            }
        });

    }

    public void addItem(ArrayList<restaurentGetSet> item, int position) {
        if (item.size() != 0) {
            moviesList.addAll(item);
            notifyItemInserted(position);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_home, parent, false);
            return new MyViewHolder(itemView);
        }
        throw new RuntimeException("there is no type that matches the type " + viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof MyViewHolder) {
            restaurentGetSet data = moviesList.get(position);
            Typeface openSansSemiBold = Typeface.createFromAsset(activity.getAssets(), "fonts/OpenSans-Semibold.ttf");
            ((MyViewHolder) holder).txt_name.setText((data.getName()));
            ((MyViewHolder) holder).txt_name.setTypeface(openSansSemiBold);
            if (data.getCategory() != null) {
                StringBuilder sb = new StringBuilder();
                for (String s : data.getCategory()) {
                    sb.append(s).append(", ");
                }
                String cat = sb.toString().replace("\"", "").replace("[", "").replace("]", "");
                if (cat.endsWith(", ")) {
                    cat = cat.substring(0, cat.length() - 2);
                }
                ((MyViewHolder) holder).txt_category.setText(cat);
            }
            ((MyViewHolder) holder).txt_category.setTypeface(tf_opensense_regular);
            ((MyViewHolder) holder).txt_distance.setText((data.getDelivery_time()).replace("MIN", "").replace("MIn", "").replace("Min","").replace(" ", ""));
            ((MyViewHolder) holder).txt_distance.setTypeface(tf_opensense_regular);
            ((MyViewHolder) holder).txt_rating.setText("" + Float.parseFloat(data.getRatting()));
            ((MyViewHolder) holder).txt_rating.setTypeface(tf_opensense_regular);

            double distance = Double.parseDouble(data.getDistance());

            String status = data.getRes_status();
            Log.e("res_status", "" + status);
            if (status.equals("open")) {

                ((MyViewHolder) holder).txt_status.setText("OPEN :Open till  ");
                ((MyViewHolder) holder).txt_status.setTextColor(Color.parseColor("#14B457"));
                ((MyViewHolder) holder).txt_status.setTypeface(tf_opensense_regular);
                ((MyViewHolder) holder).time.setText((data.getOpen_time()));
                ((MyViewHolder) holder).time.setTextColor(Color.parseColor("#14B457"));
                ((MyViewHolder) holder).time.setTypeface(tf_opensense_regular);
            } else if (status.equals("closed")) {

                ((MyViewHolder) holder).txt_status.setText("CLOSE : open at ");
                ((MyViewHolder) holder).txt_status.setTextColor(Color.parseColor("#F73E46"));
                ((MyViewHolder) holder).txt_status.setTypeface(tf_opensense_regular);
                ((MyViewHolder) holder).time.setTypeface(tf_opensense_regular);
                ((MyViewHolder) holder).time.setTextColor(Color.parseColor("#F73E46"));
                ((MyViewHolder) holder).time.setText((data.getClose_time()));

            }

            String image = data.getImage().replace(" ", "%20");

            AlphaAnimation anim = new AlphaAnimation(0, 1);
            anim.setInterpolator(new LinearInterpolator());
            anim.setRepeatCount(Animation.ABSOLUTE);
            anim.setDuration(1000);
            Picasso.with(activity).load(activity.getResources().getString(R.string.link) + activity.getString(R.string.imagepath) + image).into(((MyViewHolder) holder).imageview);
            ((MyViewHolder) holder).imageview.startAnimation(anim);
        }


    }

    public void setOnLoadMoreListener(OnLoadMoreListener mOnLoadMoreListener) {
        this.onLoadMoreListener = mOnLoadMoreListener;
    }

    @Override
    public int getItemCount() {
        return moviesList.size();
    }


    @Override
    public int getItemViewType(int position) {
        return TYPE_ITEM;
    }

    public void setLoaded() {
        isLoading = false;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        final TextView txt_name;
        final TextView txt_category;
        final TextView txt_distance;
        final TextView txt_rating;
        final TextView txt_status;
        final TextView time;
        final ImageView imageview;

        MyViewHolder(View view) {
            super(view);
            txt_name = view.findViewById(R.id.txt_name);
            txt_category = view.findViewById(R.id.txt_category);
            txt_distance = view.findViewById(R.id.txt_distance);
            txt_rating = view.findViewById(R.id.txt_rating);
            txt_status = view.findViewById(R.id.txt_status);
            imageview = view.findViewById(R.id.image);
            time = view.findViewById(R.id.time);
        }
    }


    public interface OnLoadMoreListener {
        void onLoadMore();
    }


}

