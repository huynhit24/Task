package com.huynhkhoa.task.Interfaces;

public interface EventsClickListener {
    /**
     * Khai báo các event Click sẽ được triển khai sử dụng
     * */

    void onItemClick(int position);
    void onLongItemClick(int position);
    void onEditButtonClick(int position);
    void onDeleteButtonClick(int position);
    void onDoneButtonClick(int position);
}
