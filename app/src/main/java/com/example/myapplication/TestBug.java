package com.example.myapplication;

import android.widget.Toast;

public class TestBug {
    public void test(MainActivity mainActivity) {
        Toast.makeText(mainActivity, "This is bug file", Toast.LENGTH_LONG).show();
    }
}
