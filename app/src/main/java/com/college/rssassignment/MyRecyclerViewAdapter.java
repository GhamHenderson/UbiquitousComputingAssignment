package com.college.rssassignment;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder>  {

    private List<Drawable> imageList;
    private List<String> title;
    private List<String> description;
    private List<String> images;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    // data is passed into the constructor
    MyRecyclerViewAdapter(Context context, List<String> title, List<String> descriptions, List<String> images) {
        this.mInflater = LayoutInflater.from(context);
        this.title = title;
        this.images = images;
        this.description = descriptions;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_row, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String titledata = title.get(position);
        String descriptionData = description.get(position);

//        Picasso.get()
//                .load(images.get(position))
//                .into((Target) imageList.get(position));

        holder.titleView.setText(titledata);
        holder.descriptionView.setText(descriptionData);
//      holder.imageView.setBackground((Drawable) imageList);
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return title.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView titleView;
        TextView descriptionView;
        ImageView imageView;

        ViewHolder(View itemView) {
            super(itemView);
//            imageView = itemView.findViewById(R.id.imageView3);
            descriptionView = itemView.findViewById(R.id.description);
            titleView = itemView.findViewById(R.id.RSSListItem);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    String getItem(int id) {
        return title.get(id);
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
