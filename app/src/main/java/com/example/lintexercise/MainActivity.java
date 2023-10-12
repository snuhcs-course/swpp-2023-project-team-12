package com.example.lintexercise;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    String snake_case_variable = "snake case"; // checkstyle should catch this

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    String wrongPlacedVariable = "wrong place"; // checkstyle should catch this

    private void snake_case_method() {
        // checkstyle should catch this
    }
}