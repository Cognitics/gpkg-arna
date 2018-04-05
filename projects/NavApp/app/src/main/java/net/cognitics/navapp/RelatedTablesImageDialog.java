package net.cognitics.navapp;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class RelatedTablesImageDialog extends ListActivity {

    static class ViewHolder {
        protected TextView fid;
        //protected Ima
    }

    private ArrayList<RelatedTablesImageDialog.Row> rows;

    public static String TITLE_TEXT = "title";
    public static String PARCELABLE_ROWS = "rows_array";
    public static String ROW_FIDS = "row_fid_array";
    public static String ROW_BLOBS = "row_blob_array";
    public static String RESULT_INT = "result_int";

    public RelatedTablesImageDialog() {
        this.rows = new ArrayList<>();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_related_tables_image_dialog);

        Intent intent = getIntent();
        String titleText = intent.getStringExtra(this.TITLE_TEXT);
        TextView textView = findViewById(R.id.titleText);
        textView.setText(titleText);
        //TODO: We actually need an array of byte arrays here.
        rows = intent.getParcelableArrayListExtra(PARCELABLE_ROWS);

        ArrayAdapter<RelatedTablesImageDialog.Row> adapter = new RelatedTablesImageDialogAdapter(this, rows);
        setListAdapter(adapter);
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RelatedTablesImageDialog.Row r = rows.get(position);
                Intent returnIntent = new Intent();
                returnIntent.putExtra(RESULT_INT, r.fid);
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });
    }

    public class Row implements Parcelable
    {
        public int fid;
        public byte[] blob;

        Row(int fid, byte[] blob)
        {
            this.fid = fid;
            this.blob = blob;
        }

        Row(Parcel in)
        {
            this.fid = in.readInt();
            in.readByteArray(this.blob);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeInt(fid);
            parcel.writeByteArray(blob);
        }
        public  Parcelable.Creator<Row> CREATOR = new Parcelable.Creator<Row>() {
            @Override
            public Row createFromParcel(Parcel in) {
                return new Row(in);
            }

            @Override
            public Row[] newArray(int size) {
                return new Row[size];
            }
        };
    }
}
