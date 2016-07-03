package davidos.com.weatherhere.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Parcelable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Arrays;

import butterknife.ButterKnife;
import butterknife.InjectView;
import davidos.com.weatherhere.R;
import davidos.com.weatherhere.adapters.DayAdapter;
import davidos.com.weatherhere.weather.Day;

public class DailyForecastActivity extends Activity {

    private Day[] mDays;

    @InjectView(android.R.id.list) ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_forecast);
        ButterKnife.inject(this);

        Intent intent = getIntent();
        Parcelable[] parcelables= intent.getParcelableArrayExtra(MainActivity.DAILY_FORECAST);
        mDays = Arrays.copyOf(parcelables, parcelables.length, Day[].class);

        DayAdapter adapter = new DayAdapter(this, mDays);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String dayOfTheWeek = mDays[i].getDayOfTheWeek();
                String conditions = mDays[i].getSummary();
                String highTemp = mDays[i].getTemperatureMax() + "";
                String message = String.format("On %s the high will be %s and it will be %s", dayOfTheWeek, highTemp, conditions);
                Toast.makeText(DailyForecastActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
