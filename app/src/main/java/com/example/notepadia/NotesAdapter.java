package com.example.notepadia;

import android.content.Context;
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
    private SimpleDateFormat dateFormat;

    public NotesAdapter(Context context, List<Note> notes) {
        this.context = context;
        this.notes = notes;
        dateFormat = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
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
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_note, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.cardView = convertView.findViewById(R.id.card_view);
            viewHolder.tvTitle = convertView.findViewById(R.id.tvNoteTitle);
            viewHolder.tvContent = convertView.findViewById(R.id.tvNoteContent);
            viewHolder.tvDateCreated = convertView.findViewById(R.id.tvNoteDate);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Note note = notes.get(position);
        viewHolder.tvTitle.setText(note.getTitle());
        viewHolder.tvContent.setText(note.getContent());

        if (note.getDateCreated() != null) {
            String formattedDate = dateFormat.format(note.getDateCreated()); // Format the date directly in the adapter
            viewHolder.tvDateCreated.setText(formattedDate);
        } else {
            viewHolder.tvDateCreated.setText("");
        }

        if (selectedItems.contains(position)) {
            // If the item is selected, change its background color or apply a visual indicator
            viewHolder.cardView.setBackgroundResource(R.drawable.selected_rounded);
        } else {
            // If the item is not selected, reset its background
            viewHolder.cardView.setBackgroundResource(R.drawable.rounded);
        }

        return convertView;
    }

    private static class ViewHolder {
        MaterialCardView cardView;
        TextView tvTitle;
        TextView tvContent;
        TextView tvDateCreated;
    }
}
