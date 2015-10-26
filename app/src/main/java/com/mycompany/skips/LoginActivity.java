package com.mycompany.skips;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class LoginActivity extends Activity {
    private ProgressDialog pDialog;
    // Creating JSON Parser object
    TestParser jParser = new TestParser();
    Intent mapIntent = null;
    private ArrayList<HashMap<String, String>> userList;
    private String userName = "", password = "", userID = "";
    private boolean legitUser = false;
    // url to get all products list
    private static String url_all_products = "http://testingforskipsproject.co.nf/get_all_users.php";
    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_PRODUCTS = "user";
    private static final String TAG_PASSWORD = "userPassword";
    private static final String TAG_PID = "userID";
    private static final String TAG_NAME = "userName";
    // products JSONArray
    JSONArray products = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);
    }

    public void submit(View view) {
        EditText userNameTextV = (EditText) findViewById(R.id.userName);
        EditText passwordTextV = (EditText) findViewById(R.id.password);
        userName = userNameTextV.getText().toString();
        password = passwordTextV.getText().toString();
        password = new String(Hex.encodeHex(DigestUtils.sha1(password)));
        mapIntent = new Intent(this,MapActivity.class);
        new CheckForUser().execute();

    }
    public void sendToCreateActivity(View view) {
        Intent create = new Intent(this,CreateAccount.class);
        startActivity(create);
    }
    class CheckForUser extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(LoginActivity.this);
            pDialog.setMessage("Checking username and password. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * getting All products from url
         * */
        protected String doInBackground(String... args) {
            // Building Parameters
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
                        products = json.getJSONArray(TAG_PRODUCTS);

                        // looping through All Products
                        for (int i = 0; i < products.length(); i++) {
                            JSONObject c = products.getJSONObject(i);

                            // Storing each json item in variable
                            String id = c.getString(TAG_PID);
                            String name = c.getString(TAG_NAME);
                            String pass = c.getString(TAG_PASSWORD);
                            // creating new HashMap

                            if((name.equals(userName))&&(pass.equals(password))) {
                                legitUser = true;
                                userID = id;
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
            if(legitUser) {
                mapIntent.putExtra("userName", userName);
                mapIntent.putExtra("userID", userID);
                mapIntent.putExtra("password", password);
                startActivity(mapIntent);
            }
            legitUser = false;
        }
    }
}
