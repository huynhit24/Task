
package com.huynhkhoa.task;
import static android.content.Context.ALARM_SERVICE;
import static androidx.core.content.ContextCompat.getSystemService;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
import android.content.BroadcastReceiver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.huynhkhoa.task.Adapters.TaskListAdapter;
import com.huynhkhoa.task.Constants.Constants;
import com.huynhkhoa.task.Notifications.BroadcastNotify;
import com.huynhkhoa.task.Services.SharedPreferenceClass;
import com.huynhkhoa.task.Interfaces.RecyclerViewClickListener;
import com.huynhkhoa.task.Models.TaskModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HomeFragment extends Fragment implements RecyclerViewClickListener {
    FloatingActionButton floatingActionButton;
    SharedPreferenceClass sharedPreferenceClass;
    String token;
    TaskListAdapter taskListAdapter;
    RecyclerView recyclerView;
    TextView empty_tv;
    ProgressBar progressBar;
    ArrayList<TaskModel> arrayList;
    String typeSort;

    public HomeFragment() {
        // bổ trống
    }

    public HomeFragment(String typeSort) {
        this.typeSort = typeSort;// khởi tạo giá trị sắp xếp là ascsort hoặc descsort
//        getSortedTasks(typeSort);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        sharedPreferenceClass = new SharedPreferenceClass(getContext());
        token = sharedPreferenceClass.getValue_string("token");

        floatingActionButton = view.findViewById(R.id.add_task_btn);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialog();
            }
        });

        recyclerView = view.findViewById(R.id.recycler_view);
        empty_tv = view.findViewById(R.id.empty_tv);
        progressBar = view.findViewById(R.id.progress_bar);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        if(typeSort == "ascsort" || typeSort == "descsort") {
            getSortedTasks(typeSort);
        }else{
            if(typeSort == "" || typeSort == null) {
//                getTasks(); // thay the bang danh sach sap xep
                getSortedTasks("descsort");
            }else {
                getTasksByTextSearch(typeSort);
            }

        }


        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        createNotificationChannel();
        return view;
    }


    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            showDeleteDialog(arrayList.get(position).getId(), position);
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    };

    /**
     * Show hộp thoại thêm 1 Task mới vào danh sách
     * Có chức năng hùy bỏ hộp thoại
     * */
    public void showAlertDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.custom_dialog_layout, null);

        final EditText title_field = alertLayout.findViewById(R.id.title);
        final EditText description_field = alertLayout.findViewById(R.id.description);
        final EditText duedateAt_field = alertLayout.findViewById(R.id.duedateAt);

        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(alertLayout)
                .setTitle("Thêm Task")
                .setPositiveButton("Thêm", null)
                .setNegativeButton("Hủy bỏ", null)
                .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInter) {
                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String title = title_field.getText().toString();
                        String description = description_field.getText().toString();
                        String duedateAt = duedateAt_field.getText().toString();
                        if(!TextUtils.isEmpty(title)) {
                            addTask(title, description, duedateAt);
                            dialog.dismiss();
                        } else {
                            Toast.makeText(getActivity(), "Nhập tiêu đề Task ...", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        dialog.show();
    }

    /**
     * Show hộp thoại cập nhật Task theo Task được chọn qua ID cho trong danh sách
     * Có chức năng tắt hộp thoại cập nhật
     * */
    public void showUpdateDialog(final  String  id, String title, String description)  {
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.custom_dialog_layout, null);

        final EditText title_field = alertLayout.findViewById(R.id.title);
        final EditText description_field = alertLayout.findViewById(R.id.description);
        final EditText duedateAt_field = alertLayout.findViewById(R.id.duedateAt);

        title_field.setText(title);
        description_field.setText(description);

        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setView(alertLayout)
                .setTitle("Cập nhật Task")
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
                        String title = title_field.getText().toString();
                        String description = description_field.getText().toString();
                        String duedateAt = duedateAt_field.getText().toString();
                        updateTask(id, title, description, duedateAt);
                        alertDialog.dismiss();
                    }
                });
            }
        });

        alertDialog.show();
    }

    /**
     * Show hộp thoại xóa 1 Task có trong dánh sách theo ID được chọn
     * Có chức năng hủy xóa
     * */
    public void showDeleteDialog(final String id, final  int position) {
        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle("Bạn có muốn xóa Task này ?")
                .setPositiveButton("Có", null)
                .setNegativeButton("Không", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getTasks();
                    }
                })
                .create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button button = ((AlertDialog)alertDialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteTask(id, position);
                        alertDialog.dismiss();
                    }
                });
            }
        });

        alertDialog.show();
    }

    /**
     * Show hộp thoại đánh dấu 1 Task là đã hoàn thành
     * Có chức năng tắt hộp thoại
     * */
    public void showFinishedTaskDialog(final String id, final int position) {
        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle("Đánh dấu Task này đã hoàn thành?")
                .setPositiveButton("Có", null)
                .setNegativeButton("Không", null)
                .create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button button = ((AlertDialog)alertDialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateToFinishTask(id, position);
                        alertDialog.dismiss();
                    }
                });
            }
        });

        alertDialog.show();
    }

    /**
     * Lấy ra tất cả danh sách Tasks có trong database
     * */
    public void getTasks() {
        arrayList = new ArrayList<>();
        progressBar.setVisibility(View.VISIBLE);
        String url = Constants.BASE_URL + "/api/task";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getBoolean("success")) {
                        JSONArray jsonArray = response.getJSONArray("tasks");

                        if(jsonArray.length() == 0) {
                            empty_tv.setVisibility(View.VISIBLE);
                        } else {
                            empty_tv.setVisibility(View.GONE);
                            for(int i = 0; i < jsonArray.length(); i ++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                TaskModel taskModel = new TaskModel(
                                        jsonObject.getString("_id"),
                                        jsonObject.getString("title"),
                                        jsonObject.getString("description"),
                                        jsonObject.getString("duedateAt")
                                );
                                arrayList.add(taskModel);
                            }

                            taskListAdapter = new TaskListAdapter(getActivity(), arrayList, HomeFragment.this);
                            recyclerView.setAdapter(taskListAdapter);
                        }

                    }
                    progressBar.setVisibility(View.GONE);
                } catch (JSONException e) {
                    e.printStackTrace();
                    progressBar.setVisibility(View.GONE);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {


                NetworkResponse response = error.networkResponse;

                if (error == null || error.networkResponse == null) {
                    return;
                }

                String body;

//                final String statusCode = String.valueOf(error.networkResponse.statusCode);

                try {
                    body = new String(error.networkResponse.data,"UTF-8");
                    JSONObject errorObject = new JSONObject(body);


                    if(errorObject.getString("msg").equals("Token not valid")) {
                        sharedPreferenceClass.clear();
                        startActivity(new Intent(getActivity(), LoginActivity.class));
                        Toast.makeText(getActivity(), "Session expired", Toast.LENGTH_SHORT).show();
                    }

                    Toast.makeText(getActivity(), errorObject.getString("msg") , Toast.LENGTH_SHORT).show();
                } catch (UnsupportedEncodingException | JSONException e) {
                    // exception
                }


                progressBar.setVisibility(View.GONE);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", token);
                return headers;
            }
        };

        // set retry policy
        int socketTime = 3000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTime,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);

        // request add
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(jsonObjectRequest);
    }

    /**
     * Lấy ra tất cả danh sách Tasks có trong database có sắp xếp
     * */
    public void getSortedTasks(String typeSort) {
        arrayList = new ArrayList<>();
        progressBar.setVisibility(View.VISIBLE);
        String url = Constants.BASE_URL + "/api/task/" + typeSort;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getBoolean("success")) {
                        JSONArray jsonArray = response.getJSONArray("tasks");

                        if(jsonArray.length() == 0) {
                            empty_tv.setVisibility(View.VISIBLE);
                        } else {
                            empty_tv.setVisibility(View.GONE);
                            for(int i = 0; i < jsonArray.length(); i ++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);

                                TaskModel taskModel = new TaskModel(
                                        jsonObject.getString("_id"),
                                        jsonObject.getString("title"),
                                        jsonObject.getString("description"),
                                        jsonObject.getString("duedateAt")
                                );
                                arrayList.add(taskModel);
                            }

                            taskListAdapter = new TaskListAdapter(getActivity(), arrayList, HomeFragment.this);
                            recyclerView.setAdapter(taskListAdapter);
                        }

                    }
                    progressBar.setVisibility(View.GONE);
                } catch (JSONException e) {
                    e.printStackTrace();
                    progressBar.setVisibility(View.GONE);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {


                NetworkResponse response = error.networkResponse;

                if (error == null || error.networkResponse == null) {
                    return;
                }

                String body;

//                final String statusCode = String.valueOf(error.networkResponse.statusCode);

                try {
                    body = new String(error.networkResponse.data,"UTF-8");
                    JSONObject errorObject = new JSONObject(body);


                    if(errorObject.getString("msg").equals("Token not valid")) {
                        sharedPreferenceClass.clear();
                        startActivity(new Intent(getActivity(), LoginActivity.class));
                        Toast.makeText(getActivity(), "Session expired", Toast.LENGTH_SHORT).show();
                    }

                    Toast.makeText(getActivity(), errorObject.getString("msg") , Toast.LENGTH_SHORT).show();
                } catch (UnsupportedEncodingException | JSONException e) {
                    // exception
                }


                progressBar.setVisibility(View.GONE);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", token);
                return headers;
            }
        };

        // set retry policy
        int socketTime = 3000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTime,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);

        // request add
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(jsonObjectRequest);
    }

    /**
     * Lấy ra tất cả danh sách Tasks có trong database có chừa từ cần tìm
     * */
    public void getTasksByTextSearch(String textSearch) {
        arrayList = new ArrayList<>();
        progressBar.setVisibility(View.VISIBLE);
        String url = Constants.BASE_URL + "/api/task/search?title=" + textSearch;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getBoolean("success")) {
                        JSONArray jsonArray = response.getJSONArray("tasks");

                        if(jsonArray.length() == 0) {
                            empty_tv.setVisibility(View.VISIBLE);
                        } else {
                            empty_tv.setVisibility(View.GONE);
                            for(int i = 0; i < jsonArray.length(); i ++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);

                                TaskModel taskModel = new TaskModel(
                                        jsonObject.getString("_id"),
                                        jsonObject.getString("title"),
                                        jsonObject.getString("description"),
                                        jsonObject.getString("duedateAt")
                                );
                                arrayList.add(taskModel);
                            }

                            taskListAdapter = new TaskListAdapter(getActivity(), arrayList, HomeFragment.this);
                            recyclerView.setAdapter(taskListAdapter);
                        }

                    }
                    progressBar.setVisibility(View.GONE);
                } catch (JSONException e) {
                    e.printStackTrace();
                    progressBar.setVisibility(View.GONE);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {


                NetworkResponse response = error.networkResponse;

                if (error == null || error.networkResponse == null) {
                    return;
                }

                String body;

//                final String statusCode = String.valueOf(error.networkResponse.statusCode);

                try {
                    body = new String(error.networkResponse.data,"UTF-8");
                    JSONObject errorObject = new JSONObject(body);


                    if(errorObject.getString("msg").equals("Token not valid")) {
                        sharedPreferenceClass.clear();
                        startActivity(new Intent(getActivity(), LoginActivity.class));
                        Toast.makeText(getActivity(), "Session expired", Toast.LENGTH_SHORT).show();
                    }

                    Toast.makeText(getActivity(), errorObject.getString("msg") , Toast.LENGTH_SHORT).show();
                } catch (UnsupportedEncodingException | JSONException e) {
                    // exception
                }


                progressBar.setVisibility(View.GONE);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", token);
                return headers;
            }
        };

        // set retry policy
        int socketTime = 3000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTime,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);

        // request add
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(jsonObjectRequest);
    }

    /**
     * Xóa 1 Task theo Id
     * Gọi API Delete xóa Task
     * */
    private void deleteTask(final String id, final  int position) {
        String url = Constants.BASE_URL + "/api/task/"+id;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.DELETE, url, null
                , new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getBoolean("success")) {
                        Toast.makeText(getActivity(), response.getString("msg"), Toast.LENGTH_SHORT).show();
                        arrayList.remove(position);
                        taskListAdapter.notifyItemRemoved(position);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), error.toString(), Toast.LENGTH_SHORT).show();
            }
        });

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(jsonObjectRequest);
    }

    /**
     * Thêm 1 Task mới vào danh sách
     * Gọi API Post để thêm Task
     * */
    private void addTask(String title, String description, String duedateAt) {
        String url = Constants.BASE_URL + "/api/task";

        HashMap<String, String> body = new HashMap<>();
        body.put("title", title);
        body.put("description", description);
        body.put("duedateAt", duedateAt);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                url, new JSONObject(body), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getBoolean("success")) {
                        Toast.makeText(getActivity(), "Thêm Task thành công!", Toast.LENGTH_SHORT).show();
                        getTasks();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                NetworkResponse response = error.networkResponse;
                if(error instanceof ServerError && response != null) {
                    try {
                        String res = new String(response.data, HttpHeaderParser.parseCharset(response.headers,  "utf-8"));
                        JSONObject obj = new JSONObject(res);
                        Toast.makeText(getActivity(), obj.getString("msg"), Toast.LENGTH_SHORT).show();
                    } catch (JSONException | UnsupportedEncodingException je) {
                        je.printStackTrace();
                    }
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", token);
                return headers;
            }
        };

        // set retry policy
        int socketTime = 3000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTime,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);

        // request add
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(jsonObjectRequest);
    }

    /**
     * Cập nhật 1 Task theo Id
     * Gọi API Put để cập nhật Task
     * */
    private  void  updateTask(String id, String title, String description, String duedateAt) {
        String url = Constants.BASE_URL + "/api/task/"+id;
        HashMap<String, String> body = new HashMap<>();
        body.put("title", title);
        body.put("description", description);
        body.put("duedateAt", duedateAt);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, url, new JSONObject(body),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if(response.getBoolean("success")) {
                                getTasks();
                                Toast.makeText(getActivity(), response.getString("msg"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), error.toString(), Toast.LENGTH_SHORT).show();
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

        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(jsonObjectRequest);
    }

    /**
     * Cập nhật trạng thái của 1 Task là hoàn thành
     * Gọi API Get để thay đổi field finished thành true
     * */
    private void updateToFinishTask(String id,final int position) {
        String url = Constants.BASE_URL + "/api/task/"+id;
        HashMap<String, String> body = new HashMap<>();
        body.put("finished", "true");

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, url, new JSONObject(body),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if(response.getBoolean("success")) {
                                arrayList.remove(position);
                                getTasks();

                                taskListAdapter.notifyItemRemoved(position);
                                Toast.makeText(getActivity(), response.getString("msg"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), error.toString(), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json");
                return params;
            }
        };

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(jsonObjectRequest);
    }

    @Override
    public void onItemClick(int position) {
        Toast.makeText(getActivity(), "Position "+ position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLongItemClick(int position) {
        showUpdateDialog(arrayList.get(position).getId(), arrayList.get(position).getTitle(), arrayList.get(position).getDescription());
        Toast.makeText(getActivity(), "Position "+ position, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onEditButtonClick(int position) {
        showUpdateDialog(arrayList.get(position).getId(), arrayList.get(position).getTitle(), arrayList.get(position).getDescription());
    }

    @Override
    public void onDeleteButtonClick(int position) {
        showDeleteDialog(arrayList.get(position).getId(), position);

    }

    @Override
    public void onDoneButtonClick(int position) {
        showFinishedTaskDialog(arrayList.get(position).getId(), position);
        Toast.makeText(getActivity(), "Position "+ position, Toast.LENGTH_SHORT).show();
    }

//    public static final String ALARM_SERVICE = "ALARM_SERVICE";
    @Override
    public void onClockButtonClick(int position, String title, String description) {
        Toast.makeText(getActivity(), "Position "+ position, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getActivity(), BroadcastNotify.class);

        //gửi data qua BroadcastNotify
//        final TextView title_field = (TextView) getActivity().findViewById(R.id.title);
//        final TextView description_field = (TextView) getActivity().findViewById(R.id.description);
        intent.putExtra("task_title", title);
        intent.putExtra("task_description", description);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);

        long timeAtButtonClick = System.currentTimeMillis();
        long tenSecondsMillis = 1000*10;
        alarmManager.set(AlarmManager.RTC_WAKEUP, timeAtButtonClick + tenSecondsMillis,
                pendingIntent);
    }

    /**
     * Notifications
     * */
    private void createNotificationChannel() {
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Task Notifications";
            String description = "You gotta listen";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("TaskNotify", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}

