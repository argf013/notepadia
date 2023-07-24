package com.example.notepadia;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.os.Bundle;

import android.content.Intent;
import android.text.Html;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private List<Note> notes = new ArrayList<>();
    private NotesAdapter adapter;

    private ActionMode actionMode;
    private List<Integer> selectedItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(R.style.Theme_Notepadia); // Pastikan tema sesuai dengan yang diizinkan ActionBar/Toolbar
        setContentView(R.layout.activity_main);

        // Ambil ActionBar atau Toolbar (tergantung dari jenis yang Anda gunakan)
        ActionBar actionBar = getSupportActionBar();
        // Jika Anda menggunakan Toolbar sebagai ActionBar, Anda juga bisa menggunakan:
        // Toolbar toolbar = findViewById(R.id.toolbar);

        if (actionBar != null) {
            // Ganti warna font judul ActionBar atau Toolbar
            actionBar.setTitle(Html.fromHtml("<font color=\"#1A1A1A\">" + getString(R.string.app_name) + "</font>"));
            // Jika Anda menggunakan Toolbar sebagai ActionBar, Anda juga bisa menggunakan:
            // toolbar.setTitle(Html.fromHtml("<font color=\"red\">" + getString(R.string.app_name) + "</font>"));
        }

        setContentView(R.layout.activity_main);

        GridView gridViewNotes = findViewById(R.id.gridViewNotes);
        adapter = new NotesAdapter(this, notes);
        gridViewNotes.setAdapter(adapter);

        // Daftarkan konteks menu pada GridView
        registerForContextMenu(gridViewNotes);

        gridViewNotes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (actionMode != null) {
                    // In ActionMode, update selection
                    toggleSelection(position);
                } else {
                    // Normal click, open edit activity
                    Note selectedNote = notes.get(position);
                    Intent intent = new Intent(MainActivity.this, EditNoteActivity.class);
                    intent.putExtra("note_id", selectedNote.getId());
                    startActivity(intent);
                }
            }
        });

        gridViewNotes.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (actionMode == null) {
                    // Start ActionMode for multi-selection
                    actionMode = startActionMode(actionModeCallback);
                }
                toggleSelection(position);
                return true;
            }
        });

        findViewById(R.id.btnAddNote).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EditNoteActivity.class);
                startActivity(intent);
            }
        });
    }

    private void showConfirmationDialog(String message, Runnable onConfirm) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("Yes", (dialog, which) -> onConfirm.run())
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_export) {
            showConfirmationDialog("Do you want to export the notes?", this::exportNotesToJson);
            return true;
        } else if (id == R.id.action_import) {
            showConfirmationDialog("Do you want to import the notes?", this::importNotesFromJson);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void exportNotesToJson() {
        File exportFile = new File(getExternalFilesDir(null), "notes.json");
        JSONArray jsonArray = new JSONArray();
        for (Note note : notes) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("id", note.getId());
                jsonObject.put("title", note.getTitle());
                jsonObject.put("content", note.getContent());
                jsonObject.put("date_created", formatDate(note.getDateCreated()));
                jsonArray.put(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        try {
            FileWriter fileWriter = new FileWriter(exportFile);
            fileWriter.write(jsonArray.toString());
            fileWriter.flush();
            fileWriter.close();
            Toast.makeText(this, "Notes exported to " + exportFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Export failed.", Toast.LENGTH_SHORT).show();
        }
    }

    private void importNotesFromJson() {
        File importFile = new File(getExternalFilesDir(null), "notes.json");
        StringBuilder stringBuilder = new StringBuilder();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(importFile));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            bufferedReader.close();

            JSONArray jsonArray = new JSONArray(stringBuilder.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Note note = new Note();
                note.setId(jsonObject.getInt("id"));
                note.setTitle(jsonObject.getString("title"));
                note.setContent(jsonObject.getString("content"));
                note.setDateCreated(parseDate(jsonObject.getString("date_created")));
                DatabaseHelper.getInstance(this).addNote(note); // Add note to database
            }
            // Refresh the notes list after importing
            notes.clear();
            notes.addAll(DatabaseHelper.getInstance(this).getAllNotes());
            adapter.notifyDataSetChanged();
            Toast.makeText(this, "Notes imported from " + importFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Import failed.", Toast.LENGTH_SHORT).show();
        }
    }

    private String formatDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return dateFormat.format(date);
    }

    private Date parseDate(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        try {
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the notes list when the activity is resumed
        notes.clear();
        notes.addAll(DatabaseHelper.getInstance(this).getAllNotes());
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.context_menu_note, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // Ambil informasi tentang item yang dipilih dalam GridView
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = info.position;

        switch (item.getItemId()) {
            case R.id.menu_delete_note:
                // Panggil metode untuk menghapus note dari database dan list
                deleteNoteAtPosition(position);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    // ... Existing code ...

    private void deleteNoteAtPosition(int position) {
        Note selectedNote = notes.get(position);

        // Show confirmation dialog before deleting
        showConfirmationDialog("Do you want to delete this note?", () -> {
            // Delete note from database
            DatabaseHelper.getInstance(this).deleteNote(selectedNote.getId());

            // Delete note from list
            notes.remove(position);
            adapter.notifyDataSetChanged();
        });
    }
    private void toggleSelection(int position) {
        if (selectedItems.contains(position)) {
            selectedItems.remove(Integer.valueOf(position));
        } else {
            selectedItems.add(position);
        }
        adapter.setSelectedItems(selectedItems);
        adapter.notifyDataSetChanged();

        if (selectedItems.isEmpty()) {
            actionMode.finish();
        } else {
            actionMode.setTitle(selectedItems.size() + " selected");
            actionMode.invalidate();
        }
    }

    private ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            getMenuInflater().inflate(R.menu.menu_context_action, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.action_delete) {
                deleteSelectedItems();
                mode.finish();
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
            selectedItems.clear();
            adapter.setSelectedItems(selectedItems);
            adapter.notifyDataSetChanged();
        }
    };

    private void deleteSelectedItems() {
            List<Integer> selectedIds = new ArrayList<>();
            for (int position : selectedItems) {
                Note selectedNote = notes.get(position);
                selectedIds.add(selectedNote.getId());
            }

            for (int id : selectedIds) {
                DatabaseHelper.getInstance(this).deleteNote(id);
            }

            // Refresh the notes list after deletion
            notes.clear();
            notes.addAll(DatabaseHelper.getInstance(this).getAllNotes());
            adapter.notifyDataSetChanged();

            Toast.makeText(this, "Selected items deleted", Toast.LENGTH_SHORT).show();
    }
}
