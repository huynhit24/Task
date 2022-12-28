package com.huynhkhoa.task;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
//import android.os.PersistableBundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.huynhkhoa.task.Constants.Constants;
import com.huynhkhoa.task.Services.SharedPreferenceClass;
import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    SharedPreferenceClass sharedPreferenceClass;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView navigationView;
    private TextView user_name, user_email;
    private CircleImageView userImage;
    private TextView user_phone, user_profile;
    String token, uid;
    Bundle saveStatus;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferenceClass = new SharedPreferenceClass(this);
        token = sharedPreferenceClass.getValue_string("token");
        saveStatus = savedInstanceState;
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        View hdView = navigationView.getHeaderView(0);
        user_name = (TextView) hdView.findViewById(R.id.username);
        user_email = (TextView) hdView.findViewById(R.id.user_email);
        userImage = (CircleImageView) hdView.findViewById(R.id.avatar);

        user_phone =  (TextView) hdView.findViewById(R.id.user_phone);
        user_profile = (TextView) hdView.findViewById(R.id.user_profile);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                setDrawerClick(item.getItemId());
                item.setChecked(true);
                drawerLayout.closeDrawers();
                return true;
            }
        });

        initDrawer();

        getUserProfile();
    }

    /**
     * Lấy thông tin User qua API Get User
     * */
    private void getUserProfile() {
        String url = Constants.BASE_URL + "/api/task/auth";
        final String token = sharedPreferenceClass.getValue_string("token");

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getBoolean("success")) {
                        JSONObject userObj = response.getJSONObject("user");
                        user_name.setText(userObj.getString("username"));
                        user_email.setText(userObj.getString("email"));

                        user_phone.setText(userObj.getString("phone"));
                        user_profile.setText(userObj.getString("profile"));
                        uid = userObj.getString("_id");

                        Picasso.with(getApplicationContext())
                                .load(userObj.getString("avatar"))
                                .placeholder(R.drawable.ic_account)
                                .error(R.drawable.ic_account)
                                .into(userImage);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(MainActivity.this, "Error " + error.toString(), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params  = new HashMap<String, String>();
                params.put("Authorization", token);
                return params;
            }
        };
        int socketTimeout = 30000;
        RetryPolicy policy = new DefaultRetryPolicy( socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }


    private void initDrawer() {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        ft.replace(R.id.content, new HomeFragment());
        ft.commit();

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };

        drawerToggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.white));
        drawerLayout.addDrawerListener(drawerToggle);
    }

    @Override
    public void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    private void setDrawerClick(int itemId) {
        switch (itemId) {
            case R.id.action_finished_task:
                getSupportFragmentManager().beginTransaction().replace(R.id.content, new FinishedTaskFragment()).commit();
                break;
            case R.id.action_home:
                getSupportFragmentManager().beginTransaction().replace(R.id.content, new HomeFragment()).commit();
                break;
            case R.id.action_update_userinfo:
                onEditButtonClick(uid);
                break;
            case R.id.action_logout:
                sharedPreferenceClass.clear();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
                break;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    Boolean isCheck = false; // true dùng descsort, false dùng ascsort
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_share:
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = "Share App";
                sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Chia sẻ qua"));
                return true;

            case R.id.refresh_menu:
                getSupportFragmentManager().beginTransaction().replace(R.id.content, new HomeFragment()).commit();
                return true;

            case R.id.sort_task:
                if(isCheck == true){
                    getSupportFragmentManager().beginTransaction().replace(R.id.content, new HomeFragment("descsort")).commit();
                    isCheck = false;
                }else{
                    getSupportFragmentManager().beginTransaction().replace(R.id.content, new HomeFragment("ascsort")).commit();
                    isCheck = true;
                }
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Show hộp thoại cập nhật Userinfo qua Id
     * Có chức năng tắt hộp thoại cập nhật
     * */
    public void showUpdateDialog(final  String  id, String phone, String profile)  {
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.custom_dialog_layout_update_userinfo, null);

        final EditText phone_field = alertLayout.findViewById(R.id.phone);
        final EditText profile_field = alertLayout.findViewById(R.id.profile);

        phone_field.setText(phone);
        profile_field.setText(profile);

        final AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setView(alertLayout)
                .setTitle("Cập nhật thông tin cá nhân")
                .setPositiveButton("Cập nhật", null)
                .setNegativeButton("Hủy bỏ", null)
                .create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button button = ((AlertDialog)alertDialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String phone = phone_field.getText().toString();
                        String profile = profile_field.getText().toString();

                        updateUserInfo(id, phone, profile);
                        finish();
                        startActivity(getIntent());
//                        getUserProfile();
//                        onRestart();
//                        getSupportFragmentManager().beginTransaction().replace(R.id.content, new HomeFragment()).commit();
                        alertDialog.dismiss();
                    }
                });
            }
        });

        alertDialog.show();
    }

    /**
     * Cập nhật 1 Task theo Id
     * Gọi API Put để cập nhật Task
     * */
    private  void  updateUserInfo(String id, String phone, String profile) {
        String url = Constants.BASE_URL + "/api/task/auth/"+id;
        HashMap<String, String> body = new HashMap<>();
        body.put("phone", phone);
        body.put("profile", profile);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, url, new JSONObject(body),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if(response.getBoolean("success")) {
                                Toast.makeText(MainActivity.this, response.getString("msg"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json");
                params.put("Authorization", token);
                return params;
            }
        };

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }

    public void onEditButtonClick(String uid) {
        showUpdateDialog(uid, user_phone.getText().toString(), user_profile.getText().toString());
    }
}