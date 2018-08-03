package com.apps.ghost.bateponto;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.apps.ghost.bateponto.helpers.DatabaseHelper;
import com.apps.ghost.bateponto.models.ClockIn;
import com.apps.ghost.bateponto.models.SummerFriday;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private final String MY_PREFS_NAME = "bate_ponto_shared_preferences";
    private final String START_TIME_KEY = "start_time";
    private final String INITIAL_BALANCE = "initial_balance";

    private final int WORKING_HOURS_HOURS = 9;
    private final int WORKING_HOURS_SUMMER_FRIDAY = 7;
    private final int WORKING_HOURS_MINUTES = 20;

    private final int HOUR_IN_MILLIS = 3600000;
    private final int MINUTE_IN_MILLIS = 60000;

    private final SummerFriday[] SUMMER_FRIDAYS = {
            new SummerFriday(17, 10, 2017),
            new SummerFriday(24, 10, 2017),
            new SummerFriday(1, 11, 2017),
            new SummerFriday(8, 11, 2017),
            new SummerFriday(15, 11, 2017),
            new SummerFriday(22, 11, 2017),
            new SummerFriday(5, 0, 2018),
            new SummerFriday(12, 0, 2018),
            new SummerFriday(19, 0, 2018),
            new SummerFriday(26, 0, 2018),
            new SummerFriday(2, 1, 2018),
            new SummerFriday(9, 1, 2018),
            new SummerFriday(16, 1, 2018)
    };

    private final int EXTRA_MINUTE_FOR_WORLD_CUP = 5;

    private final Date startWorldCupCompesation = new Date(1534129200000L);     /* 13/08/2018 */
    private final Date finalWorldCupCompesation = new Date(1545184800000L);      /* 19/12/2018 */

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_set_balance:
                showBalanceDialog();
                break;
        }
        return true;
    }

    private void setupView() {
        setHoursBalance();
        long startTimeInMillis = getStartTime();
        if (startTimeInMillis != 0) {
            setLayoutValues(new Date(startTimeInMillis));
        }

        clockIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long startTimeInMillis = getStartTime();
                if (startTimeInMillis == 0) {
                    setStartTime();
                } else {
                    setEndTime(startTimeInMillis);
                    cleanLayoutValues();
                    setHoursBalance();
                }
            }
        });
    }

    private long getStartTime() {
        SharedPreferences sharedPreferences = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        return sharedPreferences.getLong(START_TIME_KEY, 0);
    }

    private void setStartTime() {
        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        Date startTime = new Date();
        editor.putLong(START_TIME_KEY, startTime.getTime());
        editor.apply();

        setLayoutValues(startTime);
    }

    private void setEndTime(long startTimeInMillis) {
        Date startTime = new Date(startTimeInMillis);
        Date endTime = new Date();

        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putLong(START_TIME_KEY, 0);
        editor.apply();

        long timeDifference = endTime.getTime() - startTimeInMillis;

        ClockIn clockIn = new ClockIn(startTime, timeDifference);

        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        databaseHelper.addClockIn(clockIn);
    }

    private void setHoursBalance() {
        DatabaseHelper databaseHelper = new DatabaseHelper(this);

        List<ClockIn> clockInList = databaseHelper.getClockInList();

        long estimatedHoursInMillis;

        long balanceDiff = 0;
        for (ClockIn clockIn : clockInList) {
            if (isSummerFriday(clockIn.getStartTime())) {
                estimatedHoursInMillis = WORKING_HOURS_SUMMER_FRIDAY * HOUR_IN_MILLIS
                        + WORKING_HOURS_MINUTES * MINUTE_IN_MILLIS;
            } else {
                estimatedHoursInMillis = WORKING_HOURS_HOURS * HOUR_IN_MILLIS
                        + WORKING_HOURS_MINUTES * MINUTE_IN_MILLIS;
            }

            if (isWorldCupCompesation(clockIn.getStartTime())) {
                estimatedHoursInMillis += EXTRA_MINUTE_FOR_WORLD_CUP * MINUTE_IN_MILLIS;
            }

            balanceDiff += clockIn.getDuration() - estimatedHoursInMillis;
        }

        balanceDiff += getInitialBalance();

        setBalanceLayout(balanceDiff);
    }

    // Layout methods
    private void cleanLayoutValues() {
        this.startTime.setText(getString(R.string.home_empty_hour));
        this.endTime.setText(getString(R.string.home_empty_hour));
    }

    private void setLayoutValues(Date startTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startTime);
        int startHour = calendar.get(Calendar.HOUR_OF_DAY);
        String formattedHour = new DecimalFormat("00").format(startHour);
        int startMinute = calendar.get(Calendar.MINUTE);
        String formattedMinute = new DecimalFormat("00").format(startMinute);

        this.startTime.setText(formattedHour + ":" + formattedMinute);

        if (isSummerFriday(new Date())) {
            calendar.add(Calendar.HOUR_OF_DAY, WORKING_HOURS_SUMMER_FRIDAY);
        } else {
            calendar.add(Calendar.HOUR_OF_DAY, WORKING_HOURS_HOURS);
        }

        if (isWorldCupCompesation(new Date())) {
            calendar.add(Calendar.MINUTE, EXTRA_MINUTE_FOR_WORLD_CUP);
        }

        calendar.add(Calendar.MINUTE, WORKING_HOURS_MINUTES);

        int endHour = calendar.get(Calendar.HOUR_OF_DAY);
        formattedHour = new DecimalFormat("00").format(endHour);
        int endMinute = calendar.get(Calendar.MINUTE);
        formattedMinute = new DecimalFormat("00").format(endMinute);

        this.endTime.setText(formattedHour + ":" + formattedMinute);
    }

    private void setBalanceLayout(long balanceDiffInMillis) {
        boolean isNegative = balanceDiffInMillis < 0;
        if (isNegative) {
            balanceDiffInMillis *= -1;
        }

        long minutes = TimeUnit.MILLISECONDS.toMinutes(balanceDiffInMillis) % 60;
        String formattedMinute = new DecimalFormat("00").format(minutes);

        long hours = TimeUnit.MILLISECONDS.toHours(balanceDiffInMillis);
        String formattedHour = new DecimalFormat("00").format(hours);


        if (isNegative) {
            this.balance.setText("-" + formattedHour + ":" + formattedMinute);
            this.balance.setBackgroundColor(ContextCompat.getColor(this, R.color.colorRed));
        } else {
            this.balance.setText(formattedHour + ":" + formattedMinute);
            this.balance.setBackgroundColor(ContextCompat.getColor(this, R.color.colorGreen));
        }
    }

    private boolean isWorldCupCompesation(Date date) {
        return date.getTime() > startWorldCupCompesation.getTime() && date.getTime() < finalWorldCupCompesation.getTime();
    }

    private boolean isSummerFriday(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        for (SummerFriday summerFriday: SUMMER_FRIDAYS) {
            if (year == summerFriday.getYear()
                    && month == summerFriday.getMonth()
                    && dayOfMonth == summerFriday.getDay()) {
                return true;
            }
        }
        return false;
    }

    private void showBalanceDialog() {
        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_set_balance, null);
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(getString(R.string.dialog_balance_title));
        alertDialog.setCancelable(true);
        alertDialog.setMessage(getString(R.string.dialog_balance_message));

        final EditText inputHour = (EditText) dialogView.findViewById(R.id.dialog_hour);
        final EditText inputMinute = (EditText) dialogView.findViewById(R.id.dialog_minute);
        final CheckBox negativeCheckBox = (CheckBox) dialogView.findViewById(R.id.dialog_negative);

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.dialog_confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                long hour = Long.parseLong(inputHour.getText().toString().isEmpty() ? "0" : inputHour.getText().toString());
                long minute = Long.parseLong(inputMinute.getText().toString().isEmpty() ? "0" : inputMinute.getText().toString());

                hour *= HOUR_IN_MILLIS;
                minute *= MINUTE_IN_MILLIS;

                long balance = hour + minute;

                if (negativeCheckBox.isChecked()) {
                    balance *= -1;
                }

                setInitialBalance(balance);

                DatabaseHelper databaseHelper = new DatabaseHelper(MainActivity.this);
                databaseHelper.resetDatabase();
            }
        });

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });

        alertDialog.setView(dialogView);
        alertDialog.show();
    }

    private void setInitialBalance(long balance) {
        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putLong(INITIAL_BALANCE, balance);
        editor.apply();

        setBalanceLayout(balance);
    }

    private long getInitialBalance() {
        SharedPreferences sharedPreferences = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        return sharedPreferences.getLong(INITIAL_BALANCE, 0);
    }
}
