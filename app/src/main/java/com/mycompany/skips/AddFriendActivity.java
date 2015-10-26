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
import android.widget.EditText;
import android.widget.Toast;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class AddFriendActivity extends Activity {
    private ProgressDialog pDialog;
    TestParser jParser = new TestParser();
    String userID;
    JSONArray users = null;
    Intent create;
    private static final String TAG_USERS = "user";
    private static final String TAG_PASSWORD = "userPassword";
    private static final String TAG_PID = "userID";
    private static final String TAG_NAME = "userName";
    private static String url_all_products = "http://testingforskipsproject.co.nf/get_all_users.php";
    private static final String TAG_SUCCESS = "success";
    private static String url_create_friend = "http://testingforskipsproject.co.nf/create_friend.php";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        create = new Intent(this,FriendActivity.class);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        Intent intent = getIntent();
        if(intent != null) {
            userID = "" + intent.getStringExtra("userID");
        }
        create.putExtra("userID", userID);
    }

    public void addFriend(View view) {
        new AddFriendship().execute();
    }

    class AddFriendship extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(AddFriendActivity.this);
            pDialog.setMessage("Adding friend. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * getting All products from url
         * */
        protected String doInBackground(String... args) {
            EditText inputFriendname = (EditText) findViewById(R.id.friendName);
            String friendTwo = inputFriendname.getText().toString();
            //must convert the username to the userID to be stored in friends
            boolean legitUser = false;
            String friendID = "";
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            // getting JSON string from URL
            JSONObject json = jParser.makeHttpRequest(url_all_products, "GET", params);
            if (json != null) {
                // Check your log cat for JSON reponse
                Log.d("All Users: ", json.toString());

                try {
                    // Checking for SUCCESS TAG
                    int success = json.getInt(TAG_SUCCESS);

                    if (success == 1) {
                        // products found
                        // Getting Array of Products
                        users = json.getJSONArray(TAG_USERS);

                        // looping through All Products
                        for (int i = 0; i < users.length(); i++) {
                            JSONObject c = users.getJSONObject(i);

                            // Storing each json item in variable
                            String id = c.getString(TAG_PID);
                            String name = c.getString(TAG_NAME);
                            // creating new HashMap

                            if ((name.equals(friendTwo))) {
                                legitUser = true;
                                friendID = id;
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            //if no user is found tell user and return
            if(!legitUser) {
                Context context = getApplicationContext();
                CharSequence text = "Username was not found in the system! Please try a new username.";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                return null;
            }

            // Building Parameters
            List<NameValuePair> paramsCreate = new ArrayList<NameValuePair>();
            paramsCreate.add(new BasicNameValuePair("friend1", userID));
            paramsCreate.add(new BasicNameValuePair("friend2", friendID));

            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject jsonCreateOne = jParser.makeHttpRequest(url_create_friend, "POST", paramsCreate);

            // check log cat fro response
            Log.d("Create Response", jsonCreateOne.toString());

            // check for success tag
            try {
                int success = jsonCreateOne.getInt(TAG_SUCCESS);

                if (success == 1) {

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //reverse the friendship
            // Building Parameters
            List<NameValuePair> paramsCreateTwo = new ArrayList<NameValuePair>();
            paramsCreateTwo.add(new BasicNameValuePair("friend1", friendID));
            paramsCreateTwo.add(new BasicNameValuePair("friend2", userID));

            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject jsonCreateTwo = jParser.makeHttpRequest(url_create_friend, "POST", paramsCreateTwo);

            // check log cat fro response
            Log.d("Create Response", jsonCreateTwo.toString());

            // check for success tag
            try {
                int success = jsonCreateTwo.getInt(TAG_SUCCESS);

                if (success == 1) {

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all products
            pDialog.dismiss();

            startActivity(create);
        }
    }

}
