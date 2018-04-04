package net.cognitics.navapp;

import android.app.ListActivity;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class TableDialogActivity extends ListActivity {

    public static String RESULT_TEXT = "text";
    public static String TITLE_TEXT = "title";
    public static String ROW_STRINGS = "row_string_array";

    private List<Row> rows;

    public TableDialogActivity() {
        this.rows = new ArrayList<>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_dialog);

        Intent intent = getIntent();
        String titleText = intent.getStringExtra(this.TITLE_TEXT);
        TextView textView = findViewById(R.id.titleText);
        textView.setText(titleText);
        ArrayList<String> rowStrings = intent.getStringArrayListExtra(this.ROW_STRINGS);
        for(String rowString : rowStrings)
        {
            rows.add(new Row(rowString));
        }
        ArrayAdapter<Row> adapter = new TableDialogAdapter(this, rows);
        setListAdapter(adapter);
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Row r = rows.get(position);
                Intent returnIntent = new Intent();
                returnIntent.putExtra(RESULT_TEXT, r.getText());
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });
    }

    private void populateRowList() {

    }

    public class Row {
        String text;
        Row(String text)
        {
            this.text = text;
        }
        String getText()
        {
            return text;
        }

    }
}