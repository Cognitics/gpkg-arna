package net.cognitics.navapp;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by kbentley on 4/5/2018.
 */

public class RelatedTablesImageDialogAdapter extends ArrayAdapter<RelatedTablesImageDialog.Row> {
    private final List<RelatedTablesImageDialog.Row> list;
    private final Activity context;

    static class ViewHolder {
        protected TextView name;
    }

    public RelatedTablesImageDialogAdapter(Activity context, List<RelatedTablesImageDialog.Row> list) {
        super(context, R.layout.activity_related_tables_image_dialog_row, list);
        this.context = context;
        this.list = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;

        if (convertView == null) {
            LayoutInflater inflator = context.getLayoutInflater();
            view = inflator.inflate(R.layout.activity_table_row, null);
            final TableDialogAdapter.ViewHolder viewHolder = new TableDialogAdapter.ViewHolder();
            viewHolder.name = (TextView) view.findViewById(R.id.name);
            view.setTag(viewHolder);
        } else {
            view = convertView;
        }

        RelatedTablesImageDialog.ViewHolder holder = (RelatedTablesImageDialog.ViewHolder) view.getTag();

        holder.fid.setText(Integer.toString(list.get(position).fid));
        return view;
    }


}


