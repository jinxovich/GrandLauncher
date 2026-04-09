package com.git_blame_mama.grandlauncher;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainMenuCellsAdapter extends RecyclerView.Adapter<MainMenuCellsAdapter.ViewHolder> {

    public interface OnMainCellDeleteListener {
        void onDelete(GridItem item);
    }

    private final List<GridItem> items = new ArrayList<>();
    private final OnMainCellDeleteListener listener;

    public MainMenuCellsAdapter(OnMainCellDeleteListener listener) {
        this.listener = listener;
    }

    public void submit(List<GridItem> updatedItems) {
        items.clear();
        items.addAll(updatedItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main_cell_manage, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GridItem item = items.get(position);
        holder.tvTitle.setText(item.label);
        holder.tvSubtitle.setText(buildSubtitle(holder.itemView.getContext(), item));
        holder.ivIcon.setImageDrawable(IconResolver.resolve(holder.itemView.getContext(), item));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String buildSubtitle(Context context, GridItem item) {
        if (item.type == GridItem.Type.APP) {
            return context.getString(R.string.cell_type_app, item.data);
        }
        if (item.type == GridItem.Type.SOS) {
            return context.getString(R.string.cell_type_sos, item.data);
        }
        return context.getString(R.string.cell_type_contact, item.data);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvTitle;
        TextView tvSubtitle;
        Button btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivCellIcon);
            tvTitle = itemView.findViewById(R.id.tvCellTitle);
            tvSubtitle = itemView.findViewById(R.id.tvCellSubtitle);
            btnDelete = itemView.findViewById(R.id.btnCellDelete);
        }
    }
}
