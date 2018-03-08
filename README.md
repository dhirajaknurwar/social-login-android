# social-login-android

FACEBOOK LOGIN STEPS:


  Step1:   Login with https://developers.facebook.com/

  Step2:  Go to MyApps ----Add New App

  Step3:  Enter Display Name and Contact Email and click Create App Id

  Step4: Settings---Basic ---Add platform as Android---

Enter Below Details

-- Google Play Package Name
-- Key Hashes
-- Class Name

  Step5:  Products Tab ---Facebook Login --- Settings ---Client OAuth Settings---Client OAuth Login (ENABLE)


LINKED IN LOGIN: 

Step1: login with : https://www.linkedin.com/developer/
Step2: Create Application(By Specifying some details)
Step3: In Dashboard --Application Settings---Authentication-- Default Application Permissions---> Check Checkbox for r_emailaddress and r_basicprofile
Step4: In Dashboard --Mobile --->Android Settings-- Add Package name and Hash key and Update the Same
Step5: Ready to Use



GOOGLE PLUS:

https://developers.google.com/identity/sign-in/android/start-integrating ----

Step1: implementation 'com.google.android.gms:play-services-auth:11.8.0' in gradle file
Step2: Configure project by spaciying ...Application Name, package Name & SHA1
Step3: Integrate require code in your activity

E.g

 private GoogleSignInClient mGoogleSignInClient;
//this method call on Click for Google Plus login
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


@Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
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
        if (account != null)
            Log.d("GPL", "" + account.toString());
    }


SIGNIN WITH TRUECALLER 

Step1:SignIn and Create App in URL https://developer.truecaller.com/dashboard/apps/create
Step2: Specify your application name, package name and SHA1

Add in build.gradle :  implementation(name: "truesdk-0.6", ext: "aar")


App key
qf3s80054836b7af64e63ba83752d0025a1ec
Package name
com.master.sociallogins
Fingerprint
878d073aa46cd3237d9e7b09defaf633351a39c3

use Api key as Private Key
Step3: Add meta data in Androidmanifest.xml  ---in Application tag   --<meta-data android:name="com.truecaller.android.sdk.PartnerKey" android:value="@string/partnerKey"/>


 /*signin with true caller*/
    private TrueClient trueClient = null;


 private ITrueCallback iTrueCallback = new ITrueCallback() {
        @Override
        public void onSuccesProfileShared(@NonNull TrueProfile trueProfile) {
            Log.d("SP", trueProfile.payload);
        }

        @Override
        public void onFailureProfileShared(@NonNull TrueError trueError) {
            Log.d("SP", "" + trueError.getErrorType());
        }
    };


Step4: In Activity --On Button Click 

 trueClient = new TrueClient(getApplicationContext(), iTrueCallback);
 trueClient.getTruecallerUserProfile(MainActivity.this);


 SIGN IN WITH TWITTER

 Step1: Login and create App through followed URL https://apps.twitter.com

 Step: Fill the proper Details , Additional Permissions—Request email addresses from users

 Step3: Copy Consumer key and Consumer Secret Key

 In Activity:

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

 Call Above method in On Create of Activity

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

 Call Above method in On Click On Twitter Login Button

Video Link:  https://youtu.be/RW8uEncpSO8




