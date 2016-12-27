package com.phonenumber.app.ui;

import android.support.v7.widget.RecyclerView;

import java.util.List;

public abstract class RecyclerUniqueArrayAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerArrayAdapter<T, VH> {

    public RecyclerUniqueArrayAdapter(List<T> objects) {
        super(objects);
    }

    @Override
    public void addAll(List<T> objects) {
        if (objects != null && objects.size() > 0) {
            for (T object : objects) {
                if (!contains(object)) {
                    add(object);
                }
            }
        }
    }
}