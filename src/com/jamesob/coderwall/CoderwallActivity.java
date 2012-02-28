package com.jamesob.coderwall;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class CoderwallActivity extends Activity {
    private static final String TAG = "CoderwallActivity";
    private ListView badgesList;
    private BadgesAdapter badgeAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        badgesList = (ListView) findViewById(R.id.listBadges);
        badgeAdapter = new BadgesAdapter(this);
        badgesList.setAdapter(badgeAdapter);

        promptForUser();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public void promptForUser() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Coderwall for Android");
        alert.setMessage("Please enter the username below");

        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
                new getCWProfileTask(CoderwallActivity.this).execute("http://coderwall.com/"
                        + input.getText().toString() + ".json");
            }
        });

        alert.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "Search");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                promptForUser();
                return true;

        }
        return false;
    }

    private class getCWProfileTask extends AsyncTask<String, Void, String> {
        TextView txtFullName = (TextView) findViewById(R.id.lblFullName);
        TextView txtUserName = (TextView) findViewById(R.id.lblUserName);
        TextView txtLocation = (TextView) findViewById(R.id.lblLocation);
        ImageView imgGitHub = (ImageView) findViewById(R.id.imgGitHub);
        private ProgressDialog pDialog;
        private Context context;

        public getCWProfileTask(Context context) {
            this.context = context;
        }

        protected void onPreExecute() {
            pDialog = ProgressDialog.show(context, "", "Loading...");
        }

        @Override
        protected String doInBackground(String... params) {
            URL cwProfileURL;
            String line;
            StringBuilder builder = new StringBuilder();

            try {
                cwProfileURL = new URL(params[0]);
                URLConnection tc = cwProfileURL.openConnection();
                BufferedReader inReader = new BufferedReader(new InputStreamReader(tc
                        .getInputStream()));
                while ((line = inReader.readLine()) != null) {
                    builder.append(line);
                }
                return builder.toString();

            } catch (MalformedURLException e) {
                Log.e(TAG, "Error with URL");
            } catch (IOException e) {
                Log.e(TAG, "Error with URL");
            }

            return "";
        }

        protected void onPostExecute(String json) {
            try {
                JSONObject jsonOBJ = new JSONObject(json);

                txtFullName.setText(jsonOBJ.getString("name"));
                txtUserName.setText("(" + jsonOBJ.getString("username") + ")");
                txtLocation.setText(jsonOBJ.getString("location"));
                badgeAdapter.setItems(jsonOBJ.getJSONArray("badges"));

                JSONObject userAccounts = jsonOBJ.optJSONObject("accounts");
                final String githubUser = userAccounts.optString("github");
                if (githubUser != null) {
                    imgGitHub.setVisibility(View.VISIBLE);
                    imgGitHub.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri
                                    .parse("http://github.com/" + githubUser));
                            startActivity(browserIntent);
                        }
                    });
                } else {
                    imgGitHub.setVisibility(View.GONE);
                }

            } catch (JSONException e) {
                Log.e(TAG, "Error Parsing JSON", e);
                txtFullName.setText("Error Finding User!");
            } finally {
                if (pDialog.isShowing()) pDialog.dismiss();
            }
        }

    }
}
