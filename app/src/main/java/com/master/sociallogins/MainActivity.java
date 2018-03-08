package com.master.sociallogins;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.linkedin.platform.APIHelper;
import com.linkedin.platform.LISession;
import com.linkedin.platform.LISessionManager;
import com.linkedin.platform.errors.LIApiError;
import com.linkedin.platform.errors.LIAuthError;
import com.linkedin.platform.listeners.ApiListener;
import com.linkedin.platform.listeners.ApiResponse;
import com.linkedin.platform.listeners.AuthListener;
import com.linkedin.platform.utils.Scope;
import com.truecaller.android.sdk.ITrueCallback;
import com.truecaller.android.sdk.TrueClient;
import com.truecaller.android.sdk.TrueError;
import com.truecaller.android.sdk.TrueProfile;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String topCardUrl = "https://" + "api.linkedin.com" + "/v1/people/~:(first-name,last-name,email-address,formatted-name,phone-numbers,public-profile-url,picture-url,picture-urls::(original))";
    private static final int RC_SIGN_IN = 1000;
    //FACEBOOK CallBack manager
    private CallbackManager callbackManager;
    private String id = "";
    private String name = "";
    private String email = "";
    private String lastName = "";
    private URL photoUrl;
    private String personPhotoUrl = "";
    //FACEBOOK CallBack manager


    private boolean isLinkedInClicked = false;
    private GoogleSignInClient mGoogleSignInClient;
    private TwitterAuthClient mTwitterAuthClient;

    /*signin with true caller*/
    private TrueClient trueClient = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeFBSdk();
        setContentView(R.layout.activity_main);
        initViews();
        twitterInit();
        getHashKey();
    }

    private void initializeFBSdk() {
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        LoginManager.getInstance().logOut();
        callbackManager = CallbackManager.Factory.create();
        setFacebookLoginCallBack();

    }

    private void initViews() {
        Button mFbLoginButton = findViewById(R.id.facebookLogin);
        mFbLoginButton.setOnClickListener(this);

        Button mGooglePlusLoginButton = findViewById(R.id.googlePlus);
        mGooglePlusLoginButton.setOnClickListener(this);

        Button mLinkedInLoginButton = findViewById(R.id.linkedInLogin);
        mLinkedInLoginButton.setOnClickListener(this);

        Button mTwitterLoginButton = findViewById(R.id.twitterLogin);
        mTwitterLoginButton.setOnClickListener(this);

        Button mTrueCallerLoginButton = findViewById(R.id.trueCallerLogin);
        mTrueCallerLoginButton.setOnClickListener(this);

    }

    //START FACEBOOK LOGIN CODE//
    private void setFacebookLoginCallBack() {
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // App code
                        new FacebookLoginAsyncTask().execute(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        // App code
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.facebookLogin:
                LoginManager.getInstance().logInWithReadPermissions(MainActivity.this, Arrays.asList("public_profile,email"));
                break;
            case R.id.googlePlus:
                signInWithGoogle();
                break;
            case R.id.linkedInLogin:
                linkedInInitialize();
                break;

            case R.id.trueCallerLogin:
                trueClient = new TrueClient(getApplicationContext(), iTrueCallback);
                trueClient.getTruecallerUserProfile(MainActivity.this);
                break;

            case R.id.twitterLogin:
                mTwitterAuthClient.authorize(this, new com.twitter.sdk.android.core.Callback<TwitterSession>() {
                    @Override
                    public void success(Result<TwitterSession> twitterSessionResult) {
                        // Success
                        Toast.makeText(getApplicationContext(),"TWITTER SUCCESS: "+twitterSessionResult.data.getUserName()+ " \n"+twitterSessionResult.data.getUserId(),Toast.LENGTH_SHORT).show();
                        Log.d("TWI", twitterSessionResult.data.getUserName()+ " "+twitterSessionResult.data.getUserId());
                    }

                    @Override
                    public void failure(TwitterException e) {
                        e.printStackTrace();
                    }
                });

                break;

            default:
                break;
        }
    }


    public class FacebookLoginAsyncTask extends AsyncTask<AccessToken, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String doInBackground(AccessToken... params) {
            GraphRequest request = GraphRequest.newMeRequest(params[0],
                    new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(JSONObject object,
                                                GraphResponse response) {

                            LoginManager.getInstance().logOut();
                            Log.v("MainActivity", response.toString());
                            try {

                                String profile_pic = object.getString("id");
                                if (object.has("id"))
                                    id = object.getString("id");
                                if (object.has("first_name")) {
                                    name = object.getString("first_name");
                                    Log.v("First Name", object.getString("first_name"));
                                }
                                if (object.has("email")) {
                                    email = object.getString("email");
                                    Log.v("Email", object.getString("email"));

                                }
                                try {
                                    photoUrl = new URL(
                                            "https://graph.facebook.com/"
                                                    + profile_pic + "/picture");
                                    personPhotoUrl = photoUrl.toString();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }


                            } catch (JSONException jse) {
                                LoginManager.getInstance().logOut();
                                Log.e("fb json exception", jse.toString());
                            }
                        }
                    });
            Bundle parameters = new Bundle();
            parameters.putString("fields", "id,first_name,email,gender");
            request.setParameters(parameters);
            GraphRequest.executeBatchAndWait(request);
            return null;
        }

        protected void onPostExecute(String file_url) {
            LoginManager.getInstance().logOut();

            Log.i("Login" + "Id", id);
            Log.i("Login" + "FirstName", name);
            Log.i("Login" + "LastName", lastName);
            Log.i("Login" + "email", email);
            Log.i("Login" + "photoUrl", personPhotoUrl);
            //TODO making api call to SignUp

            Toast.makeText(getApplicationContext(),"FB :LOGIN SUCCESS:"+email,Toast.LENGTH_SHORT).show();
        }
    }
    //END FACEBOOK LOGIN CODE//

    private void getHashKey() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.master.sociallogins", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));

            }
        } catch (PackageManager.NameNotFoundException |
                NoSuchAlgorithmException e) {

        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        } else {
            if (trueClient != null) {
                if (trueClient.onActivityResult(requestCode, resultCode, data)) {
                        return;
                }
            } else {
                callbackManager.onActivityResult(requestCode, resultCode, data);
                if (isLinkedInClicked) {
                    Log.d("LI", "" + data);
                    LISessionManager.getInstance(getApplicationContext()).onActivityResult(this, requestCode, resultCode, data);
                }
            }
        }
        if (mTwitterAuthClient!=null)
        mTwitterAuthClient.onActivityResult(requestCode, resultCode, data);
    }


    private void linkedInInitialize() {
        isLinkedInClicked = true;
        LISessionManager.getInstance(getApplicationContext()).init(this, buildScope(), new AuthListener() {
            @Override
            public void onAuthSuccess() {
                // Authentication was successful.  You can now do
                // other calls with the SDK.
                APIHelper apiHelper = APIHelper.getInstance(getApplicationContext());
                apiHelper.getRequest(MainActivity.this, topCardUrl, new ApiListener() {
                    @Override
                    public void onApiSuccess(ApiResponse s) {
                        Log.d("LIRES", "" + s.getResponseDataAsJson());
                        Toast.makeText(getApplicationContext(),"LI LOGIN SUCCESS: "+s.getResponseDataAsJson(),Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onApiError(LIApiError error) {
                        Log.d("LIERROR", "" + error.toString());
                    }
                });
            }

            @Override
            public void onAuthError(LIAuthError error) {
                // Handle authentication errors
                Log.d("LIAuthError", "" + error.toString());
                Toast.makeText(getApplicationContext(), "" + error.toString(), Toast.LENGTH_SHORT).show();
            }
        }, true);
    }



    // Build the list of member permissions our LinkedIn session requires
    private Scope buildScope() {
        return Scope.build(Scope.R_BASICPROFILE, Scope.R_EMAILADDRESS, Scope.W_SHARE);
    }

    private void logOutLinkedIn() {
        if (isLogin())
            LISessionManager.getInstance(getApplicationContext()).clearSession();

    }

    private boolean isLogin() {
        LISessionManager sessionManager = LISessionManager.getInstance(getApplicationContext());
        LISession session = sessionManager.getSession();
        return session.isValid();
    }

    ///////////START GOOGLE PLUS///////////
    private void signInWithGoogle() {
        initGoogle();
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);

    }
    private void initGoogle() {
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestId()
                .requestProfile()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("MainActivity", "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }

    private void updateUI(GoogleSignInAccount account) {
        if (account != null) {
            Log.d("GPL", "" + account.getEmail());
            Toast.makeText(getApplicationContext(),"GOOGLE LOGIN SUCCESS: "+account.getEmail(),Toast.LENGTH_SHORT).show();
        }
    }
    ///////////END GOOGLE PLUS///////////

    /////START TWITTER LOGIN CODE///////
    private void twitterInit(){

        TwitterConfig config = new TwitterConfig.Builder(this)
                .logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(new TwitterAuthConfig("kYeVdo3s3RV7WfDCS7f0SH1WG", "GGyGjaoYl3JobVOQ2pVof8DFt13XQ5f0JqmPDndp0Zw6EZetzU"))
                .debug(true)
                .build();
        Twitter.initialize(config);
        mTwitterAuthClient= new TwitterAuthClient();
    }

    private void handleTwitterSession(TwitterSession session) {

    }
    /////END TWITTER LOGIN CODE///////


    ///START TRUE CALLER LOGIN//////
    private ITrueCallback iTrueCallback = new ITrueCallback() {
        @Override
        public void onSuccesProfileShared(@NonNull TrueProfile trueProfile) {
            Log.d("SP", trueProfile.email);
            Toast.makeText(getApplicationContext(),"TRUE CALLER LOGIN SUCCESS: "+trueProfile.email,Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onFailureProfileShared(@NonNull TrueError trueError) {
            Log.d("SP", "" + trueError.getErrorType());
        }
    };

    ///END TRUE CALLER LOGIN//////
}
