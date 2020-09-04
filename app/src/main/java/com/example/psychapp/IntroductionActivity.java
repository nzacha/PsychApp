package com.example.psychapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.psychapp.ui.ConsentActivity;
import com.example.psychapp.ui.login.LoginActivity;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import static com.example.psychapp.PsychApp.context;

public class IntroductionActivity extends AppCompatActivity {
    private final String DESCRIPTION = "Description";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_introduction);

        Bundle b = getIntent().getExtras();
        boolean exitParam = false;
        if(b != null)
            exitParam = b.getBoolean("new_user");
        final boolean newUser = exitParam;

        try {
            loadDescription((TextView) findViewById(R.id.descriptionText),(TextView) findViewById(R.id.nameText),(TextView) findViewById(R.id.emailText),(TextView) findViewById(R.id.phoneText));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        if(!newUser) {
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } else {
            CollapsingToolbarLayout toolbar_layout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
            float dip = 32f;
            Resources r = getResources();
            float px = TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, dip, r.getDisplayMetrics());
            toolbar_layout.setExpandedTitleMarginStart((int) px);
        }

        Button okButton = findViewById(R.id.ok_button);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(newUser){
                    Intent intent= new Intent(getApplicationContext(), ConsentActivity.class);
                    startActivity(intent);
                }
                finish();
            }
        });
    }

    public void loadDescription(TextView description, TextView name, TextView email, TextView phone) throws IOException, ClassNotFoundException {
        if(PsychApp.isNetworkConnected(this)) {
            setDescriptionFromDB(description, name, email, phone);
        } else {
            loadDescriptionFromFile(description, name, email, phone);
        }
        String text = description.getText().toString();
        if(text.equals("null") || text.equals("")){
            description.setText("No description available");
        }
    }

    private void loadDescriptionFromFile(TextView description, TextView name, TextView email, TextView phone) throws IOException, ClassNotFoundException {
        FileInputStream fis = context.openFileInput(DESCRIPTION);
        ObjectInputStream is = new ObjectInputStream(fis);
        String[] data = (String[]) is.readObject();
        description.setText(data[0]);
        name.setText(data[1]);
        email.setText(data[2]);
        phone.setText(data[3]);
        is.close();
        fis.close();
        Log.d("wtf", "Description loaded from Phone");
    }

    public void saveDescriptionLocally(String[] data) throws IOException {
        FileOutputStream fos = context.openFileOutput(DESCRIPTION, Context.MODE_PRIVATE);
        ObjectOutputStream os = new ObjectOutputStream(fos);
        os.writeObject(data);
        os.close();
        fos.close();
        Log.d("wtf", "Description saved on Phone");
    }

    public void setDescriptionFromDB(final TextView description,final TextView name,final TextView email,final TextView phone) throws IOException {
        final RequestQueue queue = Volley.newRequestQueue(this);
        String url = PsychApp.serverUrl + "researchers/"+ LoginActivity.user.getResearcherId();
        // prepare the Request
        final String[] text = new String[4];
        final JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            text[0] = response.getString("description");
                            if(!text[0].equals("null")){
                                description.setText(text[0]);
                            }
                            text[1] = response.getString("name");
                            if(!text[1].equals("null")){
                                name.setText(text[1]);
                            }
                            text[2] = response.getString("email");
                            if(!text[2].equals("null")){
                                email.setText(text[2]);
                            }
                            text[3] = response.getString("phone");
                            if(!text[3].equals("null")){
                                phone.setText(text[3]);
                            }
                            saveDescriptionLocally(text);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("wtf", "error");
                    }
                }
        );

        // add it to the RequestQueue
        queue.add(getRequest);
    }
}