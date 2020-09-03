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
            loadDescription((TextView) findViewById(R.id.descriptionText));
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

    public void loadDescription(TextView description) throws IOException, ClassNotFoundException {
        if(PsychApp.isNetworkConnected(this)) {
            setDescriptionFromDB(description);
        } else {
            loadDescriptionFromFile(description);
        }
    }

    private void loadDescriptionFromFile(TextView description) throws IOException, ClassNotFoundException {
        FileInputStream fis = context.openFileInput(DESCRIPTION);
        ObjectInputStream is = new ObjectInputStream(fis);
        description.setText((String) is.readObject());
        is.close();
        fis.close();
        Log.d("wtf", "Description loaded from Phone");
    }

    public void saveDescriptionLocally(String description) throws IOException {
        FileOutputStream fos = context.openFileOutput(DESCRIPTION, Context.MODE_PRIVATE);
        ObjectOutputStream os = new ObjectOutputStream(fos);
        os.writeObject(description);
        os.close();
        fos.close();
        Log.d("wtf", "Description saved on Phone");
    }

    public void setDescriptionFromDB(final TextView description) throws IOException {
        final RequestQueue queue = Volley.newRequestQueue(this);
        String url = PsychApp.serverUrl + "researchers/"+PsychApp.researcherId;
        // prepare the Request
        final String[] text = new String[1];
        final JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            text[0] = (String) response.get("description");
                            description.setText(text[0]);
                            saveDescriptionLocally(text[0]);
                            Log.d("wtf", text[0]);
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