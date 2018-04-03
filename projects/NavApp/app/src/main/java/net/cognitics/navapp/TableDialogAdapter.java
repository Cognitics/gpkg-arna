package net.cognitics.navapp;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by kbentley on 4/3/2018.
 */

public class TableDialogAdapter extends ArrayAdapter<TableDialogActivity.Row> {



    private final List<TableDialogActivity.Row> list;
    private final Activity context;

    static class ViewHolder {
        protected TextView name;
    }

    public TableDialogAdapter(Activity context, List<TableDialogActivity.Row> list) {
        super(context, R.layout.activity_table_row, list);
        this.context = context;
        this.list = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;

        if (convertView == null) {
            LayoutInflater inflator = context.getLayoutInflater();
            view = inflator.inflate(R.layout.activity_table_row, null);
            final ViewHolder viewHolder = new ViewHolder();
            viewHolder.name = (TextView) view.findViewById(R.id.name);
            view.setTag(viewHolder);
        } else {
            view = convertView;
        }

        ViewHolder holder = (ViewHolder) view.getTag();
        holder.name.setText(list.get(position).getText());
        return view;
    }


}
