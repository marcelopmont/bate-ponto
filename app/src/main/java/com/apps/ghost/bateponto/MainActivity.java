package com.apps.ghost.bateponto;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.home_balance)
    TextView balance;
    @BindView(R.id.home_clock_in)
    Button clockIn;
    @BindView(R.id.home_start_time)
    TextView startTime;
    @BindView(R.id.home_end_time)
    TextView endTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setupView();
    }

    private void setupView() {

        clockIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


    }
}
