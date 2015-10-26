package com.mycompany.skips;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FriendActivity extends Activity {
    private ProgressDialog pDialog;
    // Creating JSON Parser object
    TestParser jParser = new TestParser();

    private ArrayList<HashMap<String, String>> userList;
    private String userID;
    // url to get all products list
    private static String url_all_users = "http://testingforskipsproject.co.nf/get_all_users.php";
    private static String url_all_friends = "http://testingforskipsproject.co.nf/get_all_friends.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_USER = "user";
    private static final String TAG_ID = "userID";
    private static final String TAG_USERNAME = "userName";
    private static final String TAG_FRIENDSHIP = "friendship";
    private static final String TAG_FRIEND1 = "friend1";
    private static final String TAG_FRIEND2 = "friend2";
    // products JSONArray
    JSONArray users = null, friends = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);
        Intent intent = getIntent();
        if(intent != null) {
            userID = "" + intent.getStringExtra("userID");
        }
        // Hashmap for ListView
        userList = new ArrayList<HashMap<String, String>>();

        // Loading products in Background Thread
        new LoadAllUsers().execute();

        // Get listview
        ListView lv = (ListView)findViewById(R.id.userList);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // getting values from selected ListItem
                String pid = ((TextView) view.findViewById(R.id.userID)).getText().toString();

                // Starting new intent
                Intent in = new Intent(getApplicationContext(),ViewUserActivity.class);
                // sending pid to next activity
                in.putExtra("friendUserID", pid);
                in.putExtra("userID", userID);
                // starting new activity and expecting some response back
                startActivityForResult(in, 100);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // if result code 100
        if (resultCode == 100) {
            // if result code 100 is received
            // means user edited/deleted product
            // reload this screen again
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }

    }

    public void moveToAddFriend(View view) {
        Intent create = new Intent(this,AddFriendActivity.class);
        create.putExtra("userID", userID);
        startActivity(create);
    }

    class LoadAllUsers extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(FriendActivity.this);
            pDialog.setMessage("Loading your friends. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * getting All products from url
         * */
        protected String doInBackground(String... args) {
            //build friend list
            List<NameValuePair> friendParams = new ArrayList<NameValuePair>();
            JSONObject friendJson = jParser.makeHttpRequest(url_all_friends, "GET", friendParams);
            ArrayList<Integer> friendsList = new ArrayList<Integer>();
            if (friendJson != null){
                try {
                    int success = friendJson.getInt(TAG_SUCCESS);
                    if(success == 1) {
                        friends = friendJson.getJSONArray(TAG_FRIENDSHIP);
                        for (int i = 0; i < friends.length(); i++) {
                            JSONObject friend = friends.getJSONObject(i);
                            String friendOne = friend.getString(TAG_FRIEND1);
                            String friendTwo = friend.getString(TAG_FRIEND2);
                            int friend1 = Integer.parseInt(friendOne);
                            int friend2 = Integer.parseInt(friendTwo);
                            if(friend1 == Integer.parseInt(userID)) {
                                friendsList.add(friend2);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // Building Parameters
            List<NameValuePair> userParams = new ArrayList<NameValuePair>();
            // getting JSON string from URL
            JSONObject json = jParser.makeHttpRequest(url_all_users, "GET", userParams);
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
                            if(friendsList.contains(Integer.parseInt(id))) {
                                String name = c.getString(TAG_USERNAME);

                                // creating new HashMap
                                HashMap<String, String> map = new HashMap<String, String>();

                                // adding each child node to HashMap key => value
                                map.put(TAG_ID, id);
                                map.put(TAG_USERNAME, name);

                                // adding HashList to ArrayList
                                userList.add(map);
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
            // dismiss the dialog after getting all products
            pDialog.dismiss();
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Updating parsed JSON data into ListView
                     * */
                    ListAdapter adapter = new SimpleAdapter(FriendActivity.this, userList,
                            R.layout.list_item, new String[]{TAG_ID, TAG_USERNAME},
                            new int[]{R.id.userID, R.id.userName});
                    // updating listview
                    ListView lv = (ListView)findViewById(R.id.userList);
                    lv.setAdapter(adapter);


                }
            });
        }
    }
}
