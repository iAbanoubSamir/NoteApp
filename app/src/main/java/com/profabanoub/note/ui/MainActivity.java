package com.profabanoub.note.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.profabanoub.note.R;
import com.profabanoub.note.adapter.NoteAdapter;
import com.profabanoub.note.pojo.Note;
import com.profabanoub.note.viewmodel.NoteViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import static com.profabanoub.note.ui.AddEditNoteActivity.EXTRA_DESCRIPTION;
import static com.profabanoub.note.ui.AddEditNoteActivity.EXTRA_ID;
import static com.profabanoub.note.ui.AddEditNoteActivity.EXTRA_PRIORITY;
import static com.profabanoub.note.ui.AddEditNoteActivity.EXTRA_TITLE;

public class MainActivity extends AppCompatActivity {
    private NoteViewModel noteViewModel;
    private FloatingActionButton fab;

    public static final int Add_NOTE_REQUEST = 1;
    public static final int Edit_NOTE_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fab = findViewById(R.id.fab);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        NoteAdapter noteAdapter = new NoteAdapter();
        recyclerView.setAdapter(noteAdapter);

        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);
        noteViewModel.getAllNotes().observe(this, new Observer<List<Note>>() {
            @Override
            public void onChanged(List<Note> notes) {
                noteAdapter.submitList(notes);
            }
        });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                noteViewModel.delete(noteAdapter.getNoteAt(viewHolder.getAdapterPosition()));
                Toast.makeText(MainActivity.this, "Note Deleted !", Toast.LENGTH_SHORT).show();
            }
        }).attachToRecyclerView(recyclerView);

        noteAdapter.setOnItemClickListener(new NoteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Note note) {
                Intent intent = new Intent(MainActivity.this, AddEditNoteActivity.class);
                intent.putExtra(EXTRA_ID, note.getId());
                intent.putExtra(EXTRA_TITLE, note.getTitle());
                intent.putExtra(EXTRA_DESCRIPTION, note.getDescription());
                intent.putExtra(EXTRA_PRIORITY, note.getPriority());

                startActivityForResult(intent, Edit_NOTE_REQUEST);
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddEditNoteActivity.class);
                startActivityForResult(intent, Add_NOTE_REQUEST);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Add_NOTE_REQUEST && resultCode == RESULT_OK) {
            String title = data.getStringExtra(EXTRA_TITLE);
            String description = data.getStringExtra(EXTRA_DESCRIPTION);
            int priority = data.getIntExtra(EXTRA_PRIORITY, 1);

            Note note = new Note(title, description, priority);
            noteViewModel.insert(note);

            Toast.makeText(this, "Note Saved", Toast.LENGTH_SHORT).show();

        } else if (requestCode == Edit_NOTE_REQUEST && resultCode == RESULT_OK) {
            int id = data.getIntExtra(EXTRA_ID, -1);

            if (id == -1) {
                Toast.makeText(this, "Note can not be updated !", Toast.LENGTH_SHORT).show();
                return;
            }

            String title = data.getStringExtra(EXTRA_TITLE);
            String description = data.getStringExtra(EXTRA_DESCRIPTION);
            int priority = data.getIntExtra(EXTRA_PRIORITY, 1);

            Note note = new Note(title, description, priority);
            note.setId(id);

            noteViewModel.update(note);

            Toast.makeText(this, "Note Updated !", Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(this, "Note Failed to Save", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_all_notes:
                noteViewModel.deleteAll();
                Toast.makeText(this, "All Notes Deleted !", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}