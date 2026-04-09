package com.git_blame_mama.grandlauncher;

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

public class AllowedContactsAdapter extends RecyclerView.Adapter<AllowedContactsAdapter.ViewHolder> {

    public interface OnAllowedContactActionListener {
        void onAddToMain(AllowedContact contact);
        void onDelete(AllowedContact contact);
    }

    private final List<AllowedContact> contacts = new ArrayList<>();
    private final OnAllowedContactActionListener listener;

    public AllowedContactsAdapter(OnAllowedContactActionListener listener) {
        this.listener = listener;
    }

    public void submit(List<AllowedContact> updatedContacts) {
        contacts.clear();
        contacts.addAll(updatedContacts);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_allowed_contact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AllowedContact contact = contacts.get(position);
        holder.tvName.setText(contact.name);
        holder.tvNumber.setText(contact.number);
        holder.ivContactIcon.setImageDrawable(
                IconResolver.resolveByKey(holder.itemView.getContext(), contact.iconKey));

        holder.btnAddToMain.setOnClickListener(v -> listener.onAddToMain(contact));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(contact));
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivContactIcon;
        TextView tvName;
        TextView tvNumber;
        Button btnAddToMain;
        Button btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivContactIcon = itemView.findViewById(R.id.ivContactIcon);
            tvName = itemView.findViewById(R.id.tvAllowedContactName);
            tvNumber = itemView.findViewById(R.id.tvAllowedContactNumber);
            btnAddToMain = itemView.findViewById(R.id.btnAllowedAddToMain);
            btnDelete = itemView.findViewById(R.id.btnAllowedDelete);
        }
    }
}
