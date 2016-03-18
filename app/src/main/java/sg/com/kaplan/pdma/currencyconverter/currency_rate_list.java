package sg.com.kaplan.pdma.currencyconverter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;

public class currency_rate_list extends BaseAdapter {
    // This variable is used for debug log (LogCat)
    private static final String TAG = "CC:currency_rate_list";

    private LayoutInflater mInflater;
    private Bitmap[] mCountry_flag;
    private String[] mCountry_code;
    private Cursor mCurrency_rate_data;
    private double mCurrency_rate[];
    private String mCurrency_rate_display[];

    private int mBaseCurrencyPosition;

    public currency_rate_list(Context context, Integer[] name, Integer[] bitmapID, Cursor rate_data) {
        mInflater = LayoutInflater.from(context);

        mCountry_flag = new Bitmap[bitmapID.length];

        // load all bitmap and c_code
        for (int i = 0; i < bitmapID.length; i++) {
            mCountry_flag[i] = BitmapFactory.decodeResource(context.getResources(), bitmapID[i].intValue());
        }

        mCountry_code = new String[name.length];

        for (int j = 0; j < name.length; j++) {
            mCountry_code[j] = context.getString(name[j]);
        }

        mCurrency_rate_data = rate_data;

        // update currency c_rate
        updateCurrencyRate();

        // set default currency
        mBaseCurrencyPosition = 0;
    }

    @Override
    public void finalize() {
        Log.d(TAG, "Close SQL cursor...");
        mCurrency_rate_data.close();
    }

    public int getCount() {
        return mCountry_flag.length;
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        //Log.d(TAG, ">>>>> getView: position=" + Integer.toString(position));

        try {
            if (convertView == null) {
                // uses currencyratelist.xml to display each currency selection
                convertView = mInflater.inflate(R.layout.currency_rate_list, null);
                // then create a holder for this view for faster access
                holder = new ViewHolder();

                holder.c_flag = (ImageView) convertView.findViewById(R.id.conntry_flag);
                holder.c_code = (TextView) convertView.findViewById(R.id.country_code);
                holder.c_rate = (TextView) convertView.findViewById(R.id.country_currency_rate);

                // store this holder in the list
                convertView.setTag(holder);
            } else {
                // load the holder of this view
                holder = (ViewHolder) convertView.getTag();
            }

            holder.c_flag.setImageBitmap(mCountry_flag[position]);
            holder.c_code.setText(mCountry_code[position]);
            holder.c_rate.setText(mCurrency_rate_display[position]);

        } catch (Exception e) {
            Log.e(TAG, "getView:" + e.toString());
        }

        //Log.d(TAG, "<<<<< getView: position=" + Integer.toString(position));

        return convertView;
    }

    public void SetBaseCurrencyIndex(int value) {
        mBaseCurrencyPosition = value;

        // update display c_rate
        double rate_base = 1.0;

        if (mBaseCurrencyPosition < mCurrency_rate.length) {
            rate_base = mCurrency_rate[mBaseCurrencyPosition];
        }

        mCurrency_rate_display = new String[mCurrency_rate_data.getCount()];

        for (int i = 0; i < mCurrency_rate_data.getCount(); i++) {
            mCurrency_rate_display[i] = String.format(Locale.US, "%.3f", mCurrency_rate[i] / rate_base);
        }
    }

    public String getDisplayString(int position) {
        String result = "1.000";

        if (position < mCurrency_rate.length) {
            result = mCurrency_rate_display[position];
        }

        return result;
    }

    public void updateCurrencyRate() {
        Log.d(TAG, ">>>>> updateCurrencyRate");

        // update currency c_rate data
        mCurrency_rate_data.requery();

        mCurrency_rate = new double[mCurrency_rate_data.getCount()];

        int count = mCurrency_rate_data.getCount();
        int columnCount = mCurrency_rate_data.getColumnCount();

        for (int i = 0; i < count; i++) {
            if (mCurrency_rate_data.moveToPosition(i) == true) {
                if (columnCount == 1) {
                    // only currency c_rate data in the query result set
                    mCurrency_rate[i] = mCurrency_rate_data.getDouble(0);
                } else {
                    // all data in the query result set
                    // So the c_rate data in the 2nd column (refer to CurrencyConverterDB class
                    mCurrency_rate[i] = mCurrency_rate_data.getDouble(1);
                }
            } else {
                mCurrency_rate[i] = 1.0;
            }
        }

        // deactive currency c_rate data
        mCurrency_rate_data.deactivate();

        Log.d(TAG, "<<<<< updateCurrencyRate");
    }

    public double getCurrencyRate(int position) {
        double rate_sel = 1.0;

        if (position < mCurrency_rate.length) {
            rate_sel = mCurrency_rate[position];
        }

        return rate_sel;
    }

    /* class ViewHolder */
    private class ViewHolder {
        ImageView c_flag;
        TextView c_code;
        TextView c_rate;
    }
}
