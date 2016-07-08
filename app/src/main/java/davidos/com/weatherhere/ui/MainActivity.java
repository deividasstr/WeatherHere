package davidos.com.weatherhere.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import davidos.com.weatherhere.BuildConfig;
import davidos.com.weatherhere.R;
import davidos.com.weatherhere.alerts.ConnectionToDataAlertDialogFragment;
import davidos.com.weatherhere.alerts.ExitingNoPermissionAlertDialogFragment;
import davidos.com.weatherhere.alerts.NoDataAlertDialogFragment;
import davidos.com.weatherhere.weather.Current;
import davidos.com.weatherhere.weather.Day;
import davidos.com.weatherhere.weather.Forecast;
import davidos.com.weatherhere.weather.Hour;
import davidos.com.weatherhere.weather.LocationProvider;

public class MainActivity extends AppCompatActivity implements LocationProvider.LocationCallback {

    private static final int REQUEST_CHECK_SETTINGS = 2;
    private final int REQUEST_PERMISSION_LOCALIZATIONS = 1;
    public static final String TAG = MainActivity.class.getSimpleName();
    public static final String DAILY_FORECAST = "DAILY_FORECAST";
    public static final String HOURLY_FORECAST = "HOURLY_FORECAST";

    private Forecast mForecast;
    private LocationProvider mLocationProvider;

    private double mLatitude = 0;
    private double mLongitude = 0;

    @BindView(R.id.timeLabel)
    TextView mTimeLabel;
    @BindView(R.id.temperatureLabel)
    TextView mTemperatureLabel;
    @BindView(R.id.humidityValue)
    TextView mHumidityValue;
    @BindView(R.id.precipValue)
    TextView mPrecipValue;
    @BindView(R.id.summaryLabel)
    TextView mSummaryLabel;
    @BindView(R.id.iconImageView)
    ImageView mIconImageView;
    @BindView(R.id.refreshImageView)
    ImageView mRefreshImageView;
    @BindView(R.id.progressBar)
    ProgressBar mProgressBar;
    @BindView(R.id.locationLabel) TextView mLocationLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mLocationProvider = new LocationProvider(this, this);

        mProgressBar.setVisibility(View.INVISIBLE);

        if (mLongitude != 0 && mLatitude != 0){
            getForecast();
        }

        Log.d(TAG, "Main UI code is running!");
    }

    //Getting data from forecast.io API
    public void getForecast() {
        String apiKey = BuildConfig.FORCASTE_IO_API_KEY;
        String forecastUrl = "https://api.forecast.io/forecast/" + apiKey +
                "/" + mLatitude + "," + mLongitude;

        //Checking for network availability
        if (isNetworkAvailable()) {
            toggleRefresh();

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(forecastUrl)
                    .build();

            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh();
                        }
                    });
                    alertUserAboutConnectionError();
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh();
                        }
                    });

                    try {
                        String jsonData = response.body().string();
                        Log.v(TAG, jsonData);
                        if (response.isSuccessful()) {
                            mForecast = parseForecastDetails(jsonData);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateDisplay();
                                }
                            });
                        } else {
                            alertUserAboutConnectionError();
                        }
                    } catch (IOException | JSONException e) {
                        Log.e(TAG, "Exception caught: ", e);
                    }
                }
            });
        } else {
            Toast.makeText(this, "Network is unavailable",
                    Toast.LENGTH_LONG).show();
        }
    }

    //Changing between refresh bar and icon
    private void toggleRefresh() {
        if (mProgressBar.getVisibility() == View.INVISIBLE) {
            mProgressBar.setVisibility(View.VISIBLE);
            mRefreshImageView.setVisibility(View.INVISIBLE);
        } else {
            mProgressBar.setVisibility(View.INVISIBLE);
            mRefreshImageView.setVisibility(View.VISIBLE);
        }
    }

    //Updating display with new data
    private void updateDisplay() {
        mTemperatureLabel.setText(mForecast.getCurrent().getTemperature() + "");
        mTimeLabel.setText("At " + mForecast.getCurrent().getFormattedTime() + " it will be");
        mHumidityValue.setText(mForecast.getCurrent().getHumidity() + "");
        mPrecipValue.setText(mForecast.getCurrent().getPrecipChance() + "%");
        mSummaryLabel.setText(mForecast.getCurrent().getSummary());
        mLocationLabel.setText(mForecast.getCurrent().getLocationLabel());
        Drawable drawable = getResources().getDrawable(mForecast.getCurrent().getIconId());
        mIconImageView.setImageDrawable(drawable);
    }

    private Forecast parseForecastDetails(String jsonData) throws JSONException {
        Forecast forecast = new Forecast();

        forecast.setCurrent(getCurrentDetails(jsonData));
        forecast.setHour(getHour(jsonData));
        forecast.setDay(getDay(jsonData));

        return forecast;
    }

    //Getting daily weather data
    private Day[] getDay(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        JSONObject daily = forecast.getJSONObject("daily");
        JSONArray data = daily.getJSONArray("data");

        Day[] days = new Day[data.length()];

        for (int i = 0; i < data.length(); i++) {
            JSONObject jsonDay = data.getJSONObject(i);
            Day day = new Day();

            day.setTimeZone(timezone);
            day.setTime(jsonDay.getLong("time"));
            day.setIcon(jsonDay.getString("icon"));
            day.setSummary(jsonDay.getString("summary"));
            day.setTemperatureMax(jsonDay.getDouble("temperatureMax"));
            days[i] = day;
        }

        return days;
    }

    //Getting hourly data
    private Hour[] getHour(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        JSONObject hourly = forecast.getJSONObject("hourly");
        JSONArray data = hourly.getJSONArray("data");

        Hour[] hours = new Hour[data.length()];

        for (int i = 0; i < data.length(); i++) {
            JSONObject jsonHour = data.getJSONObject(i);
            Hour hour = new Hour();

            hour.setIcon(jsonHour.getString("icon"));
            hour.setSummary(jsonHour.getString("summary"));
            hour.setTemperature(jsonHour.getDouble("temperature"));
            hour.setTime(jsonHour.getLong("time"));
            hour.setTimeZone(timezone);

            hours[i] = hour;
        }
        return hours;
    }

    //Getting current weather data
    private Current getCurrentDetails(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        Log.i(TAG, "From JSON: " + timezone);

        JSONObject currently = forecast.getJSONObject("currently");

        Current current = new Current();
        current.setHumidity(currently.getDouble("humidity"));
        current.setTime(currently.getLong("time"));
        current.setIcon(currently.getString("icon"));
        current.setPrecipChance(currently.getDouble("precipProbability"));
        current.setSummary(currently.getString("summary"));
        current.setTemperature(currently.getDouble("temperature"));
        current.setTimeZone(timezone);
        current.setLocationLabel();

        Log.d(TAG, current.getFormattedTime());

        return current;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }
        return isAvailable;
    }

    public void handleNewLocation(Location location) {
        Log.d(TAG, location.toString());
        mLatitude = location.getLatitude();
        mLongitude = location.getLongitude();
        getForecast();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mLocationProvider.disconnect();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mLocationProvider.connect();
    }

    private void alertUserAboutConnectionError() {
        ConnectionToDataAlertDialogFragment dialog = new ConnectionToDataAlertDialogFragment();
        dialog.show(getFragmentManager(), "network_error_dialog");
    }
    private void alertUserAboutPermissionError() {
        String message = getString(R.string.alertNoPermission);
        ExitingNoPermissionAlertDialogFragment dialog = new ExitingNoPermissionAlertDialogFragment();
        Bundle args = new Bundle();
        args.putString("message", message);
        dialog.setArguments(args);
        dialog.show(getFragmentManager(), "permission_error_dialog");
    }
    private void alertUserAboutNoData() {
        NoDataAlertDialogFragment dialog = new NoDataAlertDialogFragment();
        dialog.show(getFragmentManager(), "no_data_dialog");
    }
    private void alertUserAboutLocationSettingsError() {
        String message = getString(R.string.alertLocationSettings);
        ExitingNoPermissionAlertDialogFragment dialog = new ExitingNoPermissionAlertDialogFragment();
        Bundle args = new Bundle();
        args.putString("message", message);
        dialog.setArguments(args);
        dialog.show(getFragmentManager(), "permission_error_dialog");
    }

    @OnClick(R.id.dailyButton)
    public void startDailyActivity(View view) {
        if (mLongitude == 0 && mLatitude == 0){
            alertUserAboutNoData();
            return;
        }
        Intent intent = new Intent(this, DailyForecastActivity.class);
        intent.putExtra(DAILY_FORECAST, mForecast.getDay());
        startActivity(intent);
    }

    @OnClick(R.id.hourlyButton)
    public void startHourlyActivity(View view) {
        if (mLongitude == 0 && mLatitude == 0){
            alertUserAboutNoData();
            return;
        }
        Intent intent = new Intent(this, HourlyForecastActivity.class);
        intent.putExtra(HOURLY_FORECAST, mForecast.getHour());
        startActivity(intent);
    }
    @OnClick (R.id.refreshImageView)
    public void refresh(View view){

            if (mLongitude != 0 && mLatitude != 0){
                getForecast();
            }
        }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION_LOCALIZATIONS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationProvider.disconnect();
                    mLocationProvider.connect();
                } else {
                    alertUserAboutPermissionError();
                }
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made
                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        alertUserAboutLocationSettingsError();
                        break;
                    default:
                        break;
                }
                break;
        }
    }
}