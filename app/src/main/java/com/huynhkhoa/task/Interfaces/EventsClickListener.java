package com.huynhkhoa.task.Interfaces;

public interface EventsClickListener {
    void onItemClick(int position);

    void onLongItemClick(int position);

    void onEditButtonClick(int position);
    void onDeleteButtonClick(int position);
    void onDoneButtonClick(int position);
}
