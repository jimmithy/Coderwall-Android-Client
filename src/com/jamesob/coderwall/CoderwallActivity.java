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

    private static final String API_KEY_NAME = "name";
    private static final String API_KEY_USERNAME = "username";
    private static final String API_KEY_LOCATION = "location";
    private static final String API_KEY_BADGES = "badges";
    public static final String API_KEY_BADGES_NAME = "name";
    public static final String API_KEY_BADGES_DESC = "description";
    public static final String API_KEY_BADGES_BADGE = "badge";
    private static final String API_KEY_ACCOUNTS = "accounts";
    private static final String API_KEY_ACCOUNTS_GITHUB = "github";

    private static final String URL_GITHUB = "https://www.github.com";
    private static final String URL_CODERWALL_PREFIX = "http://coderwall.com/";
    private static final String URL_CODERWALL_POSTFIX = ".json";

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

        alert.setTitle(R.string.app_name);
        alert.setMessage(R.string.enter_username);

        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
                new getCWProfileTask(CoderwallActivity.this).execute(URL_CODERWALL_PREFIX
                        + input.getText().toString() + URL_CODERWALL_POSTFIX);
            }
        });

        alert.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, R.string.menu_search);
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
            pDialog = ProgressDialog.show(context, "", getString(R.string.loading));
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
                Log.e(TAG, "Error connecting to URL");
            }

            return "";
        }

        protected void onPostExecute(String json) {
            try {
                JSONObject jsonOBJ = new JSONObject(json);

                txtFullName.setText(jsonOBJ.getString(API_KEY_NAME));
                txtUserName.setText("(" + jsonOBJ.getString(API_KEY_USERNAME) + ")");
                txtLocation.setText(jsonOBJ.getString(API_KEY_LOCATION));
                badgeAdapter.setItems(jsonOBJ.getJSONArray(API_KEY_BADGES));

                JSONObject userAccounts = jsonOBJ.optJSONObject(API_KEY_ACCOUNTS);
                final String githubUser = userAccounts.optString(API_KEY_ACCOUNTS_GITHUB);

                if (!githubUser.equals("")) {
                    imgGitHub.setVisibility(View.VISIBLE);
                    imgGitHub.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri
                                    .parse(URL_GITHUB + githubUser));
                            startActivity(browserIntent);
                        }
                    });
                } else {
                    imgGitHub.setVisibility(View.GONE);
                }

            } catch (JSONException e) {
                Log.e(TAG, "Error Parsing JSON", e);
                //Show Error Message
                txtFullName.setText(R.string.error);
                txtLocation.setText(R.string.error_desc);
                //Hide others
                txtUserName.setText("");
                imgGitHub.setVisibility(View.GONE);
            } finally {
                if (pDialog.isShowing()) pDialog.dismiss();
            }
        }

    }
}
