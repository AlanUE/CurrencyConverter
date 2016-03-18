package sg.com.kaplan.pdma.currencyconverter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class currency_list extends BaseAdapter {
    // This variable is used for debug log (LogCat)
    private static final String TAG = "CC:currency_list";

    private LayoutInflater mInflater;
    private Bitmap[] country_flag;
    private String[] country_code;

    public currency_list(Context context, String[] name, Integer[] bitmapID) {
        mInflater = LayoutInflater.from(context);

        country_flag = new Bitmap[bitmapID.length];

        // load all bitmap and c_code
        for (int i = 0; i < bitmapID.length; i++) {
            country_flag[i] = BitmapFactory.decodeResource(context.getResources(), bitmapID[i].intValue());
        }

        country_code = name.clone();
    }

    public int getCount() {
        return country_flag.length;
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        try {
            if (convertView == null) {
                // uses currencylist.xml to display each currency selection
                convertView = mInflater.inflate(R.layout.currency_list, null);
                // then create a holder for this view for faster access
                holder = new ViewHolder();

                holder.c_flag = (ImageView) convertView.findViewById(R.id.conntry_flag);
                holder.c_code = (TextView) convertView.findViewById(R.id.country_code);

                // store this holder in the list
                convertView.setTag(holder);
            } else {
                // load the holder of this view
                holder = (ViewHolder) convertView.getTag();
            }

            holder.c_flag.setImageBitmap(country_flag[position]);
            holder.c_code.setText(country_code[position]);
        } catch (Exception e) {
            Log.e(TAG, "getView:" + e.toString());
        }

        return convertView;
    }

    /* class ViewHolder */
    private class ViewHolder {
        ImageView c_flag;
        TextView c_code;
    }
}
