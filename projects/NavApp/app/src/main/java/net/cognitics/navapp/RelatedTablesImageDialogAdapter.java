package net.cognitics.navapp;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.util.List;

/**
 * Created by kbentley on 4/5/2018.
 */

public class RelatedTablesImageDialogAdapter extends ArrayAdapter<RelatedTablesImageDialog.Row> {
    private final List<RelatedTablesImageDialog.Row> list;
    private final Activity context;

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
            view = inflator.inflate(R.layout.activity_related_tables_image_dialog_row, null);
            final RelatedTablesImageDialog.ViewHolder viewHolder = new RelatedTablesImageDialog.ViewHolder();
            viewHolder.fid = (TextView) view.findViewById(R.id.name);
            viewHolder.imageView = (ImageView) view.findViewById(R.id.img);
            view.setTag(viewHolder);
        } else {
            view = convertView;
        }

        RelatedTablesImageDialog.ViewHolder holder = (RelatedTablesImageDialog.ViewHolder) view.getTag();

        holder.fid.setText(Integer.toString(list.get(position).fid));
        ByteArrayInputStream is = new ByteArrayInputStream(list.get(position).blob); //stream pointing to your blob or file
        //holder.imageView = new ImageView(context);
        holder.imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        holder.imageView.setAdjustViewBounds(true);
        holder.imageView.setImageBitmap(BitmapFactory.decodeStream(is));
        return view;
    }


}


