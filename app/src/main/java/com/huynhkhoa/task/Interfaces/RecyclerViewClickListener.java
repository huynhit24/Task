package com.huynhkhoa.task.Interfaces;

public interface RecyclerViewClickListener {
    /**
     * Khai báo các event Click sẽ được triển khai sử dụng
     * */

    void onItemClick(int position);
    void onLongItemClick(int position);
    void onEditButtonClick(int position);
    void onDeleteButtonClick(int position);
    void onDoneButtonClick(int position);
    void onClockButtonClick(int position, String title, String description);
}
