package com.mycompany.skips;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ViewUserActivity extends Activity {
    private ProgressDialog pDialog;
    String friendUserID, userID;
    String userName = "";
    private static final String TAG_USER = "user";
    JSONArray users = null;
    private static final String TAG_USERNAME = "userName";
    private static final String TAG_ID = "userID";
    private static final String TAG_SUCCESS = "success";
    TestParser jsonParser = new TestParser();
    private static final String url_delete_product = "http://testingforskipsproject.co.nf/delete_friendship.php";
    private static String url_all_users = "http://testingforskipsproject.co.nf/get_all_users.php";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_user);
        Intent intent = getIntent();
        if(intent != null) {
            friendUserID = "" + intent.getStringExtra("friendUserID");
            userID = "" + intent.getStringExtra("userID");
        }
        new LoadUsername().execute();
    }



    public void moveToFriendList(View view) {
        finish();
    }

    public void deleteFriend(View view) {
        new DeleteFriendship().execute();
    }
    class DeleteFriendship extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(ViewUserActivity.this);
            pDialog.setMessage("Deleting Product...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Deleting product
         * */
        protected String doInBackground(String... args) {

            // Check for success tag
            int success;
            try {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("userID", userID));
                params.add(new BasicNameValuePair("friendUserID", friendUserID));
                // getting product details by making HTTP request
                JSONObject json = jsonParser.makeHttpRequest(url_delete_product, "POST", params);

                // check your log for json response
                Log.d("Delete Product", json.toString());

                success = json.getInt(TAG_SUCCESS);
                // product successfully deleted
                // notify previous activity by sending code 100
                Intent i = getIntent();
                // send result code 100 to notify about product deletion
                setResult(100, i);
                finish();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once product deleted
            pDialog.dismiss();

        }

    }

    class LoadUsername extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        /**
         * Deleting product
         * */
        protected String doInBackground(String... args) {

            List<NameValuePair> userParams = new ArrayList<NameValuePair>();
            // getting JSON string from URL
            JSONObject json = jsonParser.makeHttpRequest(url_all_users, "GET", userParams);
            if (json != null) {
                // Check your log cat for JSON reponse
                Log.d("All Users: ", json.toString());

                try {
                    // Checking for SUCCESS TAG
                    int success = json.getInt(TAG_SUCCESS);

                    if (success == 1) {
                        // products found
                        // Getting Array of Products
                        users = json.getJSONArray(TAG_USER);

                        // looping through All Products
                        for (int i = 0; i < users.length(); i++) {
                            JSONObject c = users.getJSONObject(i);

                            // Storing each json item in variable
                            String id = c.getString(TAG_ID);

                            if(friendUserID.equals(id)) {
                                userName = c.getString(TAG_USERNAME);
                                return null;
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            TextView tv = (TextView)findViewById(R.id.friendName);
            tv.setText(userName);
        }

    }
}
