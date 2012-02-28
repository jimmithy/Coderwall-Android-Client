package com.jamesob.coderwall;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class BadgesAdapter extends BaseAdapter {

    private static final String TAG = "BadgesAdapter";
    private JSONArray badges;
    private Context context;

    public BadgesAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        if (badges != null) {
            return badges.length();
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        try {
            return badges.get(position);
        } catch (JSONException e) {
            Log.i(TAG, "Cannot return badge item.");
        }
        return null;
    }

    @Override
    public long getItemId(int pos) {
        return pos;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder v;

        if (convertView == null) {
            v = new ViewHolder();
            convertView = View.inflate(context, R.layout.badge_list_item, null);
            v.name = (TextView) convertView.findViewById(R.id.lblBadgeName);
            v.desc = (TextView) convertView.findViewById(R.id.lblBadgeDesc);
            v.badge = (ImageView) convertView.findViewById(R.id.imgBadge);
            convertView.setTag(v);
        } else {
            v = (ViewHolder) convertView.getTag();
        }

        try {
            JSONObject badge = badges.getJSONObject(position);

            v.name.setText(badge.getString(CoderwallActivity.API_KEY_BADGES_NAME));
            v.desc.setText(badge.getString(CoderwallActivity.API_KEY_BADGES_DESC));
            v.badge.setImageBitmap(grabImage(badge.getString(CoderwallActivity.API_KEY_BADGES_BADGE)));

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return convertView;
    }

    private Bitmap grabImage(String url) {
        Bitmap bitmap = null;
        InputStream in = null;
        try {
            in = OpenHttpConnection(url);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            bitmap = BitmapFactory.decodeStream(in, null, options);
            in.close();
        } catch (IOException e) {
            Log.e(TAG, "Error building the image", e);
        }
        return bitmap;
    }

    private InputStream OpenHttpConnection(String strURL) throws IOException {
        InputStream inputStream = null;
        URL url = new URL(strURL);
        URLConnection conn = url.openConnection();

        try {
            HttpURLConnection httpConn = (HttpURLConnection) conn;
            httpConn.setRequestMethod("GET");
            httpConn.connect();

            if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                inputStream = httpConn.getInputStream();
            }
        } catch (Exception ex) {
            Log.e(TAG, "Error gathering image from the interwebs", ex);
        }
        return inputStream;
    }

    public void setItems(JSONArray badges) {
        this.badges = badges;
        notifyDataSetChanged();
    }

    public class ViewHolder {
        TextView name;
        TextView desc;
        ImageView badge;
    }

}
