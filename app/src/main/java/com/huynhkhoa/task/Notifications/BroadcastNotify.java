package com.huynhkhoa.task.Notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.huynhkhoa.task.R;

public class BroadcastNotify extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "lemubitNotify")
                .setSmallIcon(R.drawable.ic_clock)
                .setContentTitle("Task chưa hoàn thành!")
                .setContentText("Bạn còn Task chưa thực hiện xong! Hãy làm ngay thôi!!!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);


        notificationManager.notify(101, builder.build());
    }
}
