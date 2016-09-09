package com.example.abov.chathub.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.example.abov.chathub.fragments.ChatFragment;
import com.example.abov.chathub.models.User;
import com.example.abov.chathub.R;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;


import org.json.JSONException;
import org.json.JSONObject;


public class FacebookLoginActivity extends AppCompatActivity implements View.OnClickListener,
        ChatFragment.OnFragmentInteractionListener {

    private TextView info;
    private CheckBox rememberMeCheckBox;
    private LoginButton loginButton;
    private Button chatWithFriendsButton;
    private Button goBackButton;

    private SharedPreferences sharedPreferences;
    private CallbackManager callbackManager;
    private Fragment mChatFragment;
    private User mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        setContentView(R.layout.activity_facebook_login);

        mChatFragment = new ChatFragment();
        initialiseViews();
        createCurrentUser();

    }

    private void createCurrentUser() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String lastRegisteredUserName = sharedPreferences.getString("userName", "");
        String lastRegisteredUserId = sharedPreferences.getString("userId", "");

        if (!lastRegisteredUserName.isEmpty() && !lastRegisteredUserId.isEmpty()) {
            info.setText("Registered as \n" + lastRegisteredUserName);
            mCurrentUser = new User(lastRegisteredUserName, lastRegisteredUserId);
        }
    }

    private void initialiseViews() {
        info = (TextView) findViewById(R.id.info);
        rememberMeCheckBox = (CheckBox) findViewById(R.id.remember_me_checkBox);
        loginButton = (LoginButton) findViewById(R.id.login_button);
        chatWithFriendsButton = (Button) findViewById(R.id.chat_with_friends_button);
        goBackButton = (Button) findViewById(R.id.go_back_button);

        rememberMeCheckBox.setOnClickListener(this);
        chatWithFriendsButton.setOnClickListener(this);
        goBackButton.setOnClickListener(this);

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                try {
                                    String userName = object.getString("name");
                                    String userId = object.getString("id");
                                    mCurrentUser = new User(userName, userId);

                                    info.setText("Welcome " + userName);

                                    saveUserInfo(rememberMeCheckBox.isChecked());

                                } catch (JSONException ex) {
                                    ex.printStackTrace();

                                }
                            }
                        });
                request.executeAsync();
            }

            @Override
            public void onCancel() {

                info.setText("Login attempt canceled.");
            }

            @Override
            public void onError(FacebookException e) {

                info.setText("Login attempt failed.");
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.remember_me_checkBox:
                saveUserInfo(rememberMeCheckBox.isChecked());
                break;
            case R.id.chat_with_friends_button:
                manageFragment(true);
                break;
            case R.id.go_back_button:
                finish();
            default:
                break;
        }
    }

    private void saveUserInfo(boolean checked) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (checked) {
            if (mCurrentUser != null) {
                editor.putString("userName", mCurrentUser.getUserName());
                editor.putString("userId", mCurrentUser.getUserId());
                Toast.makeText(FacebookLoginActivity.this, "Your account info successfully saved", Toast.LENGTH_SHORT).show();
            }
        } else {
            editor.clear();
        }
        editor.apply();
    }

    private void manageFragment(boolean b) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (b) {
            if (mCurrentUser != null) {
                manageViews(false);
                Bundle bundle = new Bundle();
                bundle.putSerializable(ChatFragment.ARG_PARAM1,  mCurrentUser);
                mChatFragment.setArguments(bundle);
                fragmentTransaction.add(R.id.chat_fragment_layout, mChatFragment);
            } else {
                AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setTitle("Dear User");
                alertDialog.setMessage("Please Register First.");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
        } else {
            manageViews(true);
            fragmentTransaction.remove(mChatFragment);
        }
        fragmentTransaction.commit();
    }

    private void manageViews(boolean b) {
        if (b) {
            info.setVisibility(View.VISIBLE);
            loginButton.setVisibility(View.VISIBLE);
            chatWithFriendsButton.setVisibility(View.VISIBLE);
            goBackButton.setVisibility(View.VISIBLE);
            rememberMeCheckBox.setVisibility(View.VISIBLE);
        } else {
            info.setVisibility(View.INVISIBLE);
            loginButton.setVisibility(View.INVISIBLE);
            chatWithFriendsButton.setVisibility(View.INVISIBLE);
            goBackButton.setVisibility(View.INVISIBLE);
            rememberMeCheckBox.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void removeChatFragment() {
        manageFragment(false);
    }
}
