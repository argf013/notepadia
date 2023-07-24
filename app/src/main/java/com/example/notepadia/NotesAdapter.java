package com.example.notepadia;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NotesAdapter extends BaseAdapter {
    private Context context;
    private List<Note> notes;
    private List<Integer> selectedItems = new ArrayList<>();

    public NotesAdapter(Context context, List<Note> notes) {
        this.context = context;
        this.notes = notes;
    }

    public void setSelectedItems(List<Integer> selectedItems) {
        this.selectedItems = selectedItems;
    }

    @Override
    public int getCount() {
        return notes.size();
    }

    @Override
    public Object getItem(int position) {
        return notes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return notes.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Inflate the layout for the item
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_note, parent, false);
        }

        // Get the card view
        MaterialCardView cardView = convertView.findViewById(R.id.card_view);

        // Set the background color for the card view based on its selected state
//        if (selectedItems.contains(position)) {
//            cardView.setSelected(true);
//        } else {
//            cardView.setSelected(false);
//        }

        TextView tvTitle = convertView.findViewById(R.id.tvNoteTitle);
        TextView tvContent = convertView.findViewById(R.id.tvNoteContent);
        TextView tvDateCreated = convertView.findViewById(R.id.tvNoteDate);

        Note note = notes.get(position);
        tvTitle.setText(note.getTitle());
        tvContent.setText(note.getContent());

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String formattedDate = dateFormat.format(note.getDateCreated());
        tvDateCreated.setText(formattedDate);

        if (selectedItems.contains(position)) {
            // If the item is selected, change its background color or apply a visual indicator
            convertView.setBackgroundResource(R.drawable.selected_rounded);
        } else {
            // If the item is not selected, reset its background
            convertView.setBackgroundResource(R.drawable.rounded);
        }

        return convertView;
    }

}
