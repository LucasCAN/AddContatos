package com.example.luca_.addcontatos;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.linkedin.platform.APIHelper;
import com.linkedin.platform.LISessionManager;
import com.linkedin.platform.errors.LIApiError;
import com.linkedin.platform.errors.LIAuthError;
import com.linkedin.platform.listeners.ApiListener;
import com.linkedin.platform.listeners.ApiResponse;
import com.linkedin.platform.listeners.AuthListener;
import com.linkedin.platform.utils.Scope;
import com.squareup.picasso.Picasso;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

/*
App adiciona novos contatos e permite o login em redes sociais(Facebook, Instagram, Linkedln)
 */

public class MainActivity extends AppCompatActivity
        implements AuthenticationListener {

    EditText nome, telefone, email;
    private CallbackManager callbackManager;

    //insta
    private String token = null;
    private AppPreferences appPreferences = null;
    private AuthenticationDialog authenticationDialog = null;
    private ImageView btn_login = null;
    private View info = null;

    //linkedin
    private ImageView img_Login,img_Logout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //computePackageHash(); D/KeyHash:: TSQmXpoNl3c4kamA7TDjbK+HIKw=
        initializeControls();//linkedin initialize

        nome = findViewById(R.id.txtName);
        telefone =  findViewById(R.id.txtPhone);
        email = findViewById(R.id.txtEmail);

        final Button button = findViewById(R.id.button);
        final Button button2 = findViewById(R.id.button2);

        //insta btn
        ImageView btn_login = findViewById(R.id.btn_login);
        appPreferences = new AppPreferences(this);

        //instagram
        //check already have access token
        token = appPreferences.getString(AppPreferences.TOKEN);
        if (token != null) {
            getUserInfoByAccessToken(token);
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //cria intent para o contato
                Intent i = new Intent(ContactsContract.Intents.Insert.ACTION);
                i.setType(ContactsContract.RawContacts.CONTENT_TYPE);

                //get dado inserido
                EditText nome = findViewById(R.id.txtName);
                EditText telefone = findViewById(R.id.txtPhone);
                EditText email = findViewById(R.id.txtEmail);

                i.putExtra(ContactsContract.Intents.Insert.NAME,nome.getText().toString())
                        .putExtra(ContactsContract.Intents.Insert.EMAIL, email.getText())
                        .putExtra(ContactsContract.Intents.Insert.EMAIL_TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                        .putExtra(ContactsContract.Intents.Insert.PHONE, telefone.getText())
                        .putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
                startActivity(i);

                Toast.makeText(getApplicationContext(), "Adicione o Contato!",Toast.LENGTH_SHORT).show();
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Resources resources = getResources();

                Intent emailIntent = new Intent();
                emailIntent.setAction(Intent.ACTION_SEND);

                PackageManager pm = getPackageManager();
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.setType("text/plain");

                Intent openInChooser = Intent.createChooser(emailIntent,"Escolha app para Compartilhar...   ");

                List<ResolveInfo> resInfo = pm.queryIntentActivities(sendIntent, 0);
                List<LabeledIntent> intentList = new ArrayList<LabeledIntent>();
                for (int i = 0; i < resInfo.size(); i++) {
                    ResolveInfo ri = resInfo.get(i);
                    String packageName = ri.activityInfo.packageName;
                    if(packageName.contains("android.email")) {
                        emailIntent.setPackage(packageName);
                    } else if(packageName.contains("android.gm") ||packageName.contains("com.whatsapp")) {
                        Intent intent = new Intent();
                        intent.setComponent(new ComponentName(packageName, ri.activityInfo.name));
                        intent.setAction(Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        if(packageName.contains("android.gm")) {
                            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{String.valueOf(email.getText())});
                            intent.putExtra(Intent.EXTRA_SUBJECT, "Adicionado!");
                            intent.putExtra(Intent.EXTRA_TEXT, "Você foi adicionado!");
                            intent.setType("message/rfc822");
                        }else if (packageName.contains("com.whatsapp")) {
                            Intent w = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse("http://api.whatsapp.com/send?phone=" + String.valueOf(telefone.getText()) + "&text=" + "Olá! Te Adicionei!"));
                        }

                        intentList.add(new LabeledIntent(intent, packageName, ri.loadLabel(pm), ri.icon));
                    }
                }

                // convert intentList to array
                LabeledIntent[] extraIntents = intentList.toArray( new LabeledIntent[ intentList.size() ]);

                openInChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents);
                startActivity(openInChooser);

            }


        });

        //configura disponibilidade dos botões do contato
        nome.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //verficar se tem entrada
                button.setEnabled(!nome.getText().toString().trim().isEmpty());
                button2.setEnabled(!nome.getText().toString().trim().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }


        });

        //Configurar Botao para logar no Facebook
        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        callbackManager = CallbackManager.Factory.create();
        loginButton.registerCallback(callbackManager, new  FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {

                        try {
                            String name = object.getString("name");
                            String email = object.getString("email");
                            Toast.makeText(getApplicationContext(), "Name: " + name + " Email: " + email, Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,gender, birthday");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                Log.i("TAG", "onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.i("TAG", "onError");
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

        //linkedin
        LISessionManager.getInstance(getApplicationContext()).onActivityResult(this, requestCode, resultCode, data);

    }
//___________________________________________________________________________________________________________________________
    //linkedin
    private void initializeControls(){
        img_Login = findViewById(R.id.img_login);
        img_Logout = findViewById(R.id.img_logout);

        //Default VISIBILIDADE BOTOES -------------------ARRUMAR
        img_Login.setVisibility(View.VISIBLE);
        img_Logout.setVisibility(View.GONE);
        img_Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.img_login:
                        handleLogin();
                        break;
                    case R.id.img_logout:
                        handleLogout();
                        break;
                }
            }
        });
    }
    public void handleLogin(){
        LISessionManager.getInstance(getApplicationContext()).init(this, buildScope(), new AuthListener() {
            @Override
            public void onAuthSuccess() {
                //img_Login.setVisibility(View.GONE);
                //img_Logout.setVisibility(View.VISIBLE);
                fetchPersonalInfo();
            }

            @Override
            public void onAuthError(LIAuthError error) {
                // lida com errros de autenticacao
                Log.e("ERROR", error.toString());
            }
        }, true);
    }
    private void handleLogout(){
        LISessionManager.getInstance(getApplicationContext()).clearSession();
        //img_Login.setVisibility(View.VISIBLE);
        //img_Logout.setVisibility(View.GONE);
    }

    //constroi lista de permissoes da sessao requerida do linkedln
    private static Scope buildScope() {
        return Scope.build(Scope.R_BASICPROFILE, Scope.W_SHARE, Scope.R_EMAILADDRESS);
    }

    private void fetchPersonalInfo(){
        String url = "https://api.linkedin.com/v2/me:(id,first-name,last-name,public-profile-url,picture-url,email-address,picture-urls::(original))";
        //code=AQTok2xtnkrqaCZUOGuR4qWPFEdGZDaB2feC_bj6HrMDDw5MqkW7ZC1NtEGu_OEv1XQ669c4YX2VB5pGjZN5qnHi169rCoHif0s7zExSta_2nwOaF_DAZ28bJ6_WSOTfZe3Y6lbIPJhcwvX4k2iF2cEIQyzCa6-HIOHk9_OBXZBqmE3rrlMO4O0U83_aVw&state=aRandomString
        APIHelper apiHelper = APIHelper.getInstance(getApplicationContext());
        apiHelper.getRequest(this, url, new ApiListener() {
            @Override
            public void onApiSuccess(ApiResponse apiResponse) {
                // Success!
                try {
                    JSONObject jsonObject = apiResponse.getResponseDataAsJson();
                    String firstName = jsonObject.getString("firstName");
                    String lastName = jsonObject.getString("lastName");
                    String emailAddress = jsonObject.getString("emailAddress");


                    StringBuilder sb = new StringBuilder();
                    sb.append("First Name: "+firstName);
                    sb.append("\n\n");
                    sb.append("Last Name: "+lastName);
                    sb.append("\n\n");
                    sb.append("Email: "+emailAddress);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onApiError(LIApiError liApiError) {
                // Error making GET request!
                Log.e("ERROR_1",liApiError.getMessage());
            }
        });
    }



//___________________________________________________________________________________________________________________________
//INSTAGRAM
    public void login() {
        info.setVisibility(View.VISIBLE);
    }

    public void logout() {
        token = null;
        info.setVisibility(View.GONE);
        appPreferences.clear();
    }

    private void getUserInfoByAccessToken(String token) {
        new RequestInstagramAPI().execute();
    }

    @Override
    public void onTokenReceived(String auth_token) {
        if (auth_token == null)
            return;
        appPreferences.putString(AppPreferences.TOKEN, auth_token);
        token = auth_token;
        getUserInfoByAccessToken(token);
    }

    //onclick do instagram
    public void onClick(View v) {
        if(token!=null)
        {
            logout();
        }
        else {
            authenticationDialog = new AuthenticationDialog(this, this);
            authenticationDialog.setCancelable(true);
            authenticationDialog.show();
        }
    }

    private class RequestInstagramAPI extends AsyncTask<Void, String, String> {

        @Override
        protected String doInBackground(Void... params) {
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(getResources().getString(R.string.get_user_info_url) + token);
            try {
                HttpResponse response = httpClient.execute(httpGet);
                HttpEntity httpEntity = response.getEntity();
                return EntityUtils.toString(httpEntity);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            if (response != null) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Log.e("response", jsonObject.toString());
                    JSONObject jsonData = jsonObject.getJSONObject("data");
                    if (jsonData.has("id")) {
                        appPreferences.putString(AppPreferences.USER_ID, jsonData.getString("id"));
                        appPreferences.putString(AppPreferences.USER_NAME, jsonData.getString("username"));
                        appPreferences.putString(AppPreferences.PROFILE_PIC, jsonData.getString("profile_picture"));

                        login();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else {
                Toast toast = Toast.makeText(getApplicationContext(),"Teste!",Toast.LENGTH_LONG);
                toast.show();
            }
        }
    }

    @Override
    protected void  onRestart(){
        super.onRestart();
    }

    @Override
    protected void  onPause(){
        super.onPause();
    }

    @Override
    protected void  onResume(){
        super.onResume();
    }

    @Override
    protected void  onDestroy(){
        super.onDestroy();
    }

}
