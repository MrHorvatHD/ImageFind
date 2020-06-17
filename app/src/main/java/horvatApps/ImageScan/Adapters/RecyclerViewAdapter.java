package horvatApps.ImageScan.Adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;

import horvatApps.ImageScan.R;
import horvatApps.ImageScan.db.models.ImageDetail;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.GridViewHolder> {
    private ArrayList<ImageDetail> recyclerList;
    private Context context;

    public RecyclerViewAdapter(Context context, ArrayList<ImageDetail> list) {
        this.recyclerList = list;
        this.context = context;
    }

    class GridViewHolder extends RecyclerView.ViewHolder {
        public ImageView itemImage;
        public TextView itemName;

        public GridViewHolder(View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.image_view);
            itemName = itemView.findViewById(R.id.text_view_content);
        }
    }


    @NonNull
    @Override
    public GridViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_grid_item, parent, false);
        return new GridViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final GridViewHolder gridViewHolder, final int position) {

        Glide.with(context)
                .load(recyclerList.get(position).getThumb())
                .apply(new RequestOptions().centerCrop().override(512, 512))
                .into(gridViewHolder.itemImage);

        String imgName = recyclerList.get(position).getName();
        if (imgName.length() > 16)
            imgName = imgName.substring(0, 16).concat("...");

        gridViewHolder.itemName.setText(imgName);
        //gridViewHolder.itemName.setText(recyclerList.get(position).getImageText());


        gridViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(recyclerList.get(position).getUri(), "image/*");

                context.startActivity(intent);
                // Toast.makeText(context, "aaaa", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return recyclerList.size();
    }

}
