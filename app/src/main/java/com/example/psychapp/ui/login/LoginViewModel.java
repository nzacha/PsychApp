package com.example.psychapp.ui.login;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.psychapp.ui.questions.QuestionnaireActivity;
import com.example.psychapp.applications.PsychApp;
import com.example.psychapp.R;
import com.example.psychapp.data.Exceptions;
import com.example.psychapp.data.Result;
import com.example.psychapp.data.LoggedInUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import static com.example.psychapp.applications.PsychApp.context;

public class LoginViewModel extends ViewModel {
    public static LoginViewModel instance;

    private MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    private MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();

    private Result res;
    private String code;

    LoginViewModel(String code) {
        this.code = code;
    }

    LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    public void login() {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(PsychApp.context);
        String url = PsychApp.serverUrl + "users/" + code;

        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //make model from data
                        try {
                            JSONObject project = response.getJSONObject("project");
                            LoggedInUser user = new LoggedInUser(Integer.parseInt(response.get("id").toString()), response.get("name").toString(), response.getInt("projectId"), project.getInt("study_length"), project.getInt("tests_per_day"), project.getInt("tests_time_interval"), project.getBoolean("allow_individual_times"), project.getBoolean("allow_user_termination"), project.getBoolean("automatic_termination"), response.getInt("progress"), response.getString("code"), response.getBoolean("isActive"));
                            Log.d("wtf", user.toString());
                            if(user.isActive())
                                res = new Result.Success(user);
                            else
                                res = new Result.Error(new Exceptions.UserInactive());

                        } catch (JSONException e) {
                            res = new Result.Error(new IOException("Error with JSONObject", e));
                        }
                        authenticateResult();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("wtf", error.toString());
                        if(error == null || error.networkResponse == null) {
                            res = new Result.Error(new Exception("Unknown error"));
                            authenticateResult();
                            return;
                        }
                        switch(error.networkResponse.statusCode){
                            case 400:
                                res = new Result.Error(new Exceptions.UserInactive());
                                break;
                            case 404:
                                res = new Result.Error(new Exceptions.InvalidCredentials());
                                break;
                            default:
                                Log.d("wtf",error.networkResponse.statusCode+": "+error.networkResponse.data);
                                res = new Result.Error(new Exception());
                        }
                        authenticateResult();
                    }
                });

        // add it to the RequestQueue
        queue.add(postRequest);
    }

    public void authenticateResult(){
        if (res instanceof Result.Success) {
            LoggedInUser data = ((Result.Success<LoggedInUser>) res).getData();
            loginResult.postValue(new LoginResult(data));
        } else {
            Result.Error error = (Result.Error) res;
            if (error.getError() instanceof Exceptions.UserInactive){
                loginResult.postValue(new LoginResult(R.string.user_inactive));
            }else if (error.getError() instanceof Exceptions.InvalidCredentials){
                loginResult.postValue(new LoginResult(R.string.login_failed));
            }else {
                loginResult.postValue(new LoginResult(R.string.unkown_error));
            }
        }
    }

    public void setCode(String code){
        this.code = code;
    }

    public void loginDataChanged(String code) {
        if (!isCodeValid(code)) {
            loginFormState.setValue(new LoginFormState(LoginFormState.invalid_code));
        } else {
            loginFormState.setValue(new LoginFormState(true));
        }
    }

    // A placeholder username validation check
    private boolean isCodeValid(String code) {
        if (code == null || code.isEmpty()) {
            return false;
        }
        /*
        if (code.contains("@")) {
            return Patterns.EMAIL_ADDRESS.matcher(code).matches();
        } else {
            return !code.trim().isEmpty();
        }
        */
        return true;
    }
}