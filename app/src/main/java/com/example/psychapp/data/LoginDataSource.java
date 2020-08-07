package com.example.psychapp.data;

import android.util.Log;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.psychapp.PsychApp;
import com.example.psychapp.R;
import com.example.psychapp.data.model.LoggedInUser;
import com.example.psychapp.ui.login.LoginViewModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {
    Result res = null;
    Object lock = new Object();

    public Result<LoggedInUser> getUserInfo(){
        return res;
    }

    public void login(String code) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(PsychApp.context);
        String url = PsychApp.serverUrl + "users/" + code;

        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            res = new Result.Success(new LoggedInUser(Integer.parseInt(response.get("id").toString()), response.get("name").toString(), Integer.parseInt(response.get("researcherId").toString())));
                        } catch (JSONException e) {
                            res = new Result.Error(new IOException("Error with JSONObject", e));
                        }
                        LoginViewModel.instance.authenticateResult(res);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        res = new Result.Error(new IOException("Error logging in", error));
                        LoginViewModel.instance.authenticateResult(res);
                    }
                });

        // add it to the RequestQueue
        queue.add(postRequest);
    }


    public void logout() {
        // TODO: revoke authentication
    }
}