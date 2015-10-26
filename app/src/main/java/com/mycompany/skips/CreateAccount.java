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
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.ArrayList;
import java.util.List;


public class CreateAccount extends Activity {
    private ProgressDialog pDialog;
    TestParser jsonParser = new TestParser();
    private static String url_create_user = "http://testingforskipsproject.co.nf/create_user.php";
    private static String url_all_products = "http://testingforskipsproject.co.nf/get_all_users.php";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_PRODUCTS = "user";
    private static final String TAG_PASSWORD = "userPassword";
    private static final String TAG_PID = "userID";
    private static final String TAG_NAME = "userName";
    String userID;
    JSONArray products = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
    }
    public void createAccount(View v) {
        TextView userNameTextV = (TextView) findViewById(R.id.userName);
        TextView passwordTextV = (TextView) findViewById(R.id.password);
        TextView confPasswordTextV = (TextView) findViewById(R.id.confPassword);
        String userName = userNameTextV.getText().toString();
        String password = passwordTextV.getText().toString();
        String confPassword = confPasswordTextV.getText().toString();
        if(password.length() == 0 || userName.length() == 0 || confPassword.length() == 0) {
            int duration = Toast.LENGTH_LONG;
            Context context = getApplicationContext();
            Toast toast = Toast.makeText(context, "Please fill in every element in the form.", duration);
            toast.show();
        } else {
            if (password.equals(confPassword)) {
                //create account in user database
                new CreateNewUser().execute();
            } else {
                int duration = Toast.LENGTH_LONG;
                Context context = getApplicationContext();
                Toast toast = Toast.makeText(context, "Passwords do not match! Please try again.", duration);
                toast.show();
            }
        }
    }
    class CreateNewUser extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(CreateAccount.this);
            pDialog.setMessage("Creating Profile..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Creating product
         * */
        protected String doInBackground(String... args) {
            EditText inputUsername = (EditText) findViewById(R.id.userName);
            EditText inputPassword = (EditText) findViewById(R.id.password);
            String username = inputUsername.getText().toString();
            String password = inputPassword.getText().toString();

            // Building Parameters
            List<NameValuePair> paramsCreate = new ArrayList<NameValuePair>();
            paramsCreate.add(new BasicNameValuePair("userName", username));
            paramsCreate.add(new BasicNameValuePair("userPassword", new String(Hex.encodeHex(DigestUtils.sha1(password)))));

            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject jsonCreate = jsonParser.makeHttpRequest(url_create_user, "POST", paramsCreate);

            // check log cat fro response
            Log.d("Create Response", jsonCreate.toString());

            // check for success tag
            try {
                int success = jsonCreate.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // successfully created product
                    // now we must find the userID to transfer to the mapActivity
                    List<NameValuePair> paramsID = new ArrayList<NameValuePair>();
                    // getting JSON string from URL
                    JSONObject jsonID = jsonParser.makeHttpRequest(url_all_products, "GET", paramsID);
                    if (jsonID != null) {
                        // Check your log cat for JSON reponse
                        Log.d("All Users: ", jsonID.toString());

                        try {
                            // Checking for SUCCESS TAG
                            success = jsonID.getInt(TAG_SUCCESS);

                            if (success == 1) {
                                // products found
                                // Getting Array of Products
                                products = jsonID.getJSONArray(TAG_PRODUCTS);

                                // looping through All Products
                                for (int i = 0; i < products.length(); i++) {
                                    JSONObject c = products.getJSONObject(i);

                                    // Storing each json item in variable
                                    String id = c.getString(TAG_PID);
                                    String name = c.getString(TAG_NAME);
                                    String pass = c.getString(TAG_PASSWORD);
                                    // creating new HashMap

                                    if((name.equals(username))&&(pass.equals(new String(Hex.encodeHex(DigestUtils.sha1(password)))))) {
                                        userID = id;
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    // send the intent (focus) to the Map Activity
                    Intent i = new Intent(getApplicationContext(), MapActivity.class);
                    i.putExtra("userID", userID);
                    startActivity(i);

                    // closing this screen
                    finish();
                } else {
                    // failed to create product
                    int duration = Toast.LENGTH_LONG;
                    Context context = getApplicationContext();
                    Toast toast = Toast.makeText(context, "There was an error adding the profile to the system.", duration);
                    toast.show();
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
            // dismiss the dialog once done
            pDialog.dismiss();
        }

    }
}
