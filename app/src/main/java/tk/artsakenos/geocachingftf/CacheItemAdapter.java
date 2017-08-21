package tk.artsakenos.geocachingftf;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class CacheItemAdapter extends ArrayAdapter<CacheItem> {
    private final Context context;
    private ArrayList<CacheItem> items = null;

    public CacheItemAdapter(Context context, int resource, ArrayList<CacheItem> items) {
        super(context, resource, items);
        this.context = context;
        this.items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.cache_item_layout, parent, false);
        // ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        TextView textView01 = (TextView) rowView.findViewById(R.id.txtLine01);
        TextView textView02 = (TextView) rowView.findViewById(R.id.txtLine02);

        CacheItem ci = items.get(position);
        String line1 = ci.name;
        String line2 = ci.author + " [" + ci.difficulty + "] " + ci.datePlaced;

        if (ci.status == CacheItem.STATUS_NEW) {
            rowView.setBackgroundColor(Color.rgb(225, 255, 225)); // Giallino
        }
        if (ci.status == CacheItem.STATUS_FOUND) {
            rowView.setBackgroundColor(Color.rgb(255, 225, 225)); // Rossino
        }

        textView01.setText(line1);
        textView02.setText(line2);

        // if (s.startsWith("iPhone")) imageView.setImageResource(R.drawable.common_full_open_on_phone);
        // else imageView.setImageResource(R.drawable.common_ic_googleplayservices);
        return rowView;
    }
}
