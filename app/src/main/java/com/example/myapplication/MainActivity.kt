package com.example.myapplication

import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button.setOnClickListener {
            TestBug().test(this);
        }

        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ),
            1
        );

        btn_fix_bug.setOnClickListener {

            fixBug();
        }
    }

    private fun fixBug() {
        try {
            DexUtils.copyFixDex2Data(this)
            DexUtils.loadDex(this)
        }catch (e : Exception){
            e.printStackTrace()
        }
    }
}
