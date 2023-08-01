package com.example.notepadia;

import static com.example.notepadia.DateUtils.formatDate;

import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import android.widget.TextView;
import android.widget.Toast;

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
        setTheme(R.style.Theme_Notepadia);
        setContentView(R.layout.activity_edit_note);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle(Html.fromHtml("<font color=\"#1A1A1A\">" + getString(R.string.app_name) + "</font>"));
        }

        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        tvDateCreated = findViewById(R.id.tvDateCreated);

        noteId = getIntent().getIntExtra("note_id", -1);
        if (noteId != -1) {
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


    @Override
    public void onBackPressed() {
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
            saveNote();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveNote() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();
        Date dateCreated = new Date();

        if (title.isEmpty() && content.isEmpty()) {
            finish();
            return;
        }

        if (title.isEmpty()) {
            title = "Untitled";
        }

        Note note;
        if (noteId == -1) {
            // New note
            note = new Note();
            note.setTitle(title);
            note.setContent(content);
            note.setDateCreated(dateCreated);
            DatabaseHelper.getInstance(this).addNote(note);
        } else {
            note = DatabaseHelper.getInstance(this).getNoteById(noteId);
            if (note != null) {
                note.setTitle(title);
                note.setContent(content);
                DatabaseHelper.getInstance(this).updateNote(note);
            }
        }

        finish();
    }

}
