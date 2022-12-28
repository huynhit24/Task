package com.huynhkhoa.task.Notifications;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.huynhkhoa.task.R;

public class BroadcastNotify extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "TaskNotify")
                .setSmallIcon(R.drawable.ic_clock)
                .setContentTitle(intent.getStringExtra("task_title"))
                .setSubText("Task chưa hoàn thành!")
                .setContentText(intent.getStringExtra("task_description"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

//        PendingIntent contentAppActivityIntent =
//                PendingIntent.getActivity(
//                        context,  // calling from Activity
//                        0,
//                        intent,
//                        PendingIntent.FLAG_UPDATE_CURRENT);
//
//        builder.setContentIntent(contentAppActivityIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(101, builder.build());


    }
}
