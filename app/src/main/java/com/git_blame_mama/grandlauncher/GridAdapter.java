package com.git_blame_mama.grandlauncher;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GridAdapter extends RecyclerView.Adapter<GridAdapter.GridViewHolder> {

    private final List<GridItem> items;
    private final OnItemClickListener listener;

    // Интерфейс для обработки кликов в MainActivity
    public interface OnItemClickListener {
        void onItemClick(GridItem item);
    }

    public GridAdapter(List<GridItem> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GridViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Загружаем макет одной кнопки (item_grid.xml)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grid, parent, false);
        return new GridViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GridViewHolder holder, int position) {
        GridItem item = items.get(position);
        Context context = holder.itemView.getContext();

        holder.tvLabel.setText(item.label);
        holder.ivIcon.setImageDrawable(IconResolver.resolve(context, item));

        int colorResId;
        switch (item.type) {
            case SOS:
                colorResId = R.color.tile_sos;
                break;
            case CONTACT:
                colorResId = R.color.tile_contact;
                break;
            case APP:
                colorResId = R.color.tile_app;
                break;
            default:
                colorResId = R.color.tile_default;
                break;
        }

        holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, colorResId));
        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // Класс, хранящий ссылки на элементы интерфейса одной плитки
    public static class GridViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView ivIcon;
        TextView tvLabel;

        public GridViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            tvLabel = itemView.findViewById(R.id.tvLabel);
        }
    }
}