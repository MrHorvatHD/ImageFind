package horvatApps.ImageFind.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;

import horvatApps.ImageFind.R;
import horvatApps.ImageFind.db.models.ImageDetail;
import horvatApps.ImageFind.logic.MLForegroundService;
import horvatApps.ImageFind.ui.ShareIntentActivity;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.GridViewHolder> {
    private ArrayList<ImageDetail> recyclerList;
    private Context context;

    public RecyclerViewAdapter(Context context, ArrayList<ImageDetail> list) {
        this.recyclerList = list;
        this.context = context;
    }

    class GridViewHolder extends RecyclerView.ViewHolder {
        public ImageView itemImage;

        public GridViewHolder(View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.image_view);
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
                .apply(new RequestOptions().override(300, 300))
                .into(gridViewHolder.itemImage);

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

        gridViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent intent = new Intent(context, ShareIntentActivity.class);
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_STREAM, recyclerList.get(position).getUri());
                intent.setType("image/*");
                context.startActivity(intent);

                return true;
            }
        });

        /**/
    }

    @Override
    public int getItemCount() {
        return recyclerList.size();
    }

}
