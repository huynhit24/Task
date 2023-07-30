package com.huynhkhoa.task;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
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
import com.huynhkhoa.task.Adapters.FinishedTaskAdapter;
import com.huynhkhoa.task.Constants.Constants;
import com.huynhkhoa.task.Services.SharedPreferenceClass;
import com.huynhkhoa.task.Interfaces.RecyclerViewClickListener;
import com.huynhkhoa.task.Models.TaskModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class FinishedTaskFragment extends Fragment implements RecyclerViewClickListener {

    SharedPreferenceClass sharedPreferenceClass;
    String token;
    FinishedTaskAdapter taskListAdapter;
    RecyclerView recyclerView;
    TextView empty_tv;
    ProgressBar progressBar;
    ArrayList<TaskModel> arrayList;

    public FinishedTaskFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_finished_task, container, false);

        sharedPreferenceClass = new SharedPreferenceClass(getContext());
        token = sharedPreferenceClass.getValue_string("token");


        recyclerView = view.findViewById(R.id.recycler_view);
        empty_tv = view.findViewById(R.id.empty_tv);
        progressBar = view.findViewById(R.id.progress_bar);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        getTasks();

        return view;
    }

    /**
     * Lấy ra tất cả danh sách Task đã đánh dấu là hoàn thành
     * */
    private void getTasks() {
        arrayList = new ArrayList<>();
        progressBar.setVisibility(View.VISIBLE);
        String url = Constants.BASE_URL + "/api/task/finished";

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
                                        jsonObject.getString("description")
                                );
                                arrayList.add(taskModel);
                            }

                            taskListAdapter = new FinishedTaskAdapter(getActivity(), arrayList, FinishedTaskFragment.this);
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
                    e.printStackTrace();
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
     * Xóa Task theo Id
     * */
    public void deleteTask(final String id, final int position) {
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
     * Show hộp thoại xóa Task theo Id
     * */
    public void showDeleteDialog(final String id, final int position) {
        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle("Delete Task..")
                .setPositiveButton("Xóa", null)
                .setNegativeButton("Hủy bỏ", null)
                .create();


        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteTask(id, position);
                        dialog.dismiss();
                    }
                });
            }
        });

        dialog.show();
    }

    /**
     * Cập nhật trạng thái của 1 Task là chưa hoàn thành
     * Gọi API Get để thay đổi field finished thành false
     * */
    private void updateToUnFinishTask(final String id,final int position) {
        String url = Constants.BASE_URL + "/api/task/"+id;
        HashMap<String, String> body = new HashMap<>();
        body.put("finished", "false");

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

    /**
     * Show hộp thoại đánh dấu 1 Task là đã hoàn thành
     * Có chức năng tắt hộp thoại
     * */
    public void showFinishedTaskDialog(final String id, final int position) {
//        final androidx.appcompat.app.AlertDialog alertDialog = new androidx.appcompat.app.AlertDialog.Builder(getActivity())
//                .setTitle("Đánh dấu Task này là chưa hoàn thành?")
//                .setPositiveButton("Có", null)
//                .setNegativeButton("Không", null)
//                .create();
//
//        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
//            @Override
//            public void onShow(DialogInterface dialog) {
//                Button button = ((androidx.appcompat.app.AlertDialog)alertDialog).getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE);
//                button.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        updateToFinishTask(id, position);
//                        alertDialog.dismiss();
//                    }
//                });
//            }
//        });
//
//        alertDialog.show();
        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle("Đánh dấu Task này là chưa hoàn thành?")
                .setPositiveButton("Có", null)
                .setNegativeButton("Không", null)
                .create();


        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateToUnFinishTask(id, position);
                        dialog.dismiss();
                    }
                });
            }
        });

        dialog.show();
    }

    @Override
    public void onItemClick(int position) {

    }

    @Override
    public void onLongItemClick(int position) {

    }

    @Override
    public void onEditButtonClick(int position) {

    }

    @Override
    public void onDeleteButtonClick(int position) {
        Toast.makeText(getActivity(), "Position " + arrayList.get(position).getId() , Toast.LENGTH_SHORT).show();
        showDeleteDialog(arrayList.get(position).getId(), position);
    }

    @Override
    public void onDoneButtonClick(int position) {
        Toast.makeText(getActivity(), "Position "+ position, Toast.LENGTH_SHORT).show();
        showFinishedTaskDialog(arrayList.get(position).getId(), position);
    }
    @Override
    public void onClockButtonClick(int position, String title, String description) {

    }
}