package com.example.notepadia;

import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EditNoteActivity extends AppCompatActivity {
    private int noteId = -1; // Default value for new note
    private EditText etTitle, etContent;
    private TextView tvDateCreated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_Notepadia); // Pastikan tema sesuai dengan yang diizinkan ActionBar/Toolbar
        setContentView(R.layout.activity_edit_note);

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

        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        tvDateCreated = findViewById(R.id.tvDateCreated);

        // Get the note ID from the intent if it exists
        noteId = getIntent().getIntExtra("note_id", -1);
        if (noteId != -1) {
            // Load the note from the database and display its data
            Note note = DatabaseHelper.getInstance(this).getNoteById(noteId);
            if (note != null) {
                etTitle.setText(note.getTitle());
                etContent.setText(note.getContent());
                tvDateCreated.setText(formatDate(note.getDateCreated()));
            }
        } else {
            // New note, set the current date as the date created
            tvDateCreated.setText(formatDate(new Date()));
        }
    }

    private String formatDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return dateFormat.format(date);
    }

    @Override
    public void onBackPressed() {
        // Automatically save the note when the back button is pressed
        saveNote();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_back) {
            // The back button in the menu is clicked
            saveNote(); // Automatically save the note before going back
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveNote() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();
        Date dateCreated = new Date(); // Current date

        // Periksa apakah judul kosong atau hanya berisi spasi
        if (title.isEmpty()) {
            // Jika kosong, isi otomatis dengan "Untitled"
            title = "Untitled";
        }

        // Save the note to the database or update if it's an existing note
        Note note;
        if (noteId == -1) {
            // New note
            note = new Note();
            note.setTitle(title);
            note.setContent(content);
            note.setDateCreated(dateCreated);
            DatabaseHelper.getInstance(this).addNote(note);
        } else {
            // Existing note
            note = DatabaseHelper.getInstance(this).getNoteById(noteId);
            if (note != null) {
                note.setTitle(title);
                note.setContent(content);
                DatabaseHelper.getInstance(this).updateNote(note);
            }
        }

        finish(); // Go back to the main activity
    }
}
