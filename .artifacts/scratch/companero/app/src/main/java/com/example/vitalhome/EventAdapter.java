package com.example.vitalhome;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private final List<Event> events;

    public EventAdapter(List<Event> events) {
        this.events = events;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.title.setText(event.getTitle());
        holder.meta.setText(event.getMeta());

        int iconRes;
        int bgColorRes;
        int tintColorRes;

        switch (event.getType()) {
            case WATER:
                iconRes = android.R.drawable.ic_menu_compass;
                bgColorRes = R.color.warning_bg;
                tintColorRes = R.color.warning_text;
                break;
            case SOS_MANUAL:
                iconRes = android.R.drawable.ic_menu_call;
                bgColorRes = R.color.surface_muted;
                tintColorRes = R.color.text_secondary;
                break;
            case FALL:
            default:
                iconRes = android.R.drawable.ic_dialog_alert;
                bgColorRes = R.color.danger_bg;
                tintColorRes = R.color.danger_text;
                break;
        }

        holder.icon.setImageResource(iconRes);
        holder.icon.setImageTintList(ColorStateList.valueOf(
                ContextCompat.getColor(holder.itemView.getContext(), tintColorRes)));
        holder.iconBackground.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(holder.itemView.getContext(), bgColorRes)));
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView meta;
        ImageView icon;
        FrameLayout iconBackground;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.textEventTitle);
            meta = itemView.findViewById(R.id.textEventMeta);
            icon = itemView.findViewById(R.id.iconEvent);
            iconBackground = itemView.findViewById(R.id.iconBackground);
        }
    }
}