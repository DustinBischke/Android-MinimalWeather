package ca.bischke.apps.minimalweather;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
{
    private final String TAG = "MinimalWeather";
    private final String url = "https://api.openweathermap.org/data/2.5/weather?q=";
    private final String apiKey = "45f6bbc6afba649813f1027b77c00b4f";
    private final int locationCode = 32;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        displayWeatherData();
        setDateTime();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == locationCode && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            {
                displayWeatherData();
            }
        }
    }

    private void displayWeatherData()
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            try
            {
                List<Address> addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

                if (addressList != null && addressList.size() > 0)
                {
                    Address address = addressList.get(0);

                    TextView textCity = findViewById(R.id.textCity);
                    textCity.setText(address.getLocality());

                    TextView textCountry = findViewById(R.id.textCountry);
                    textCountry.setText(address.getCountryName());

                    String encodedCity = URLEncoder.encode(address.getLocality(), "UTF-8");
                    String countryCode = address.getCountryCode();

                    JsonTask jsonTask = new JsonTask();
                    jsonTask.execute(url + encodedCity + "," + countryCode + "&appid=" + apiKey);
                }
            }
            catch (IOException e)
            {
                Log.e(TAG, e.getMessage());
            }
        }
        else
        {
            // Request Permissions
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, locationCode);
        }
    }

    // TODO DateTime of displayed city
    private void setDateTime()
    {
        TextView textDate = findViewById(R.id.textDate);
        TextView textTime = findViewById(R.id.textTime);

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMMM dd", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());

        Date d = new Date();
        String date = dateFormat.format(d);
        String time = timeFormat.format(d);

        textDate.setText(date);
        textTime.setText(time);
    }

    private int kelvinToCelsius(double kelvin)
    {
        double celsius = kelvin - 273.15;
        return (int) Math.round(celsius);
    }

    private int getWindChill(int temperature, double windSpeed)
    {
        if (temperature < 10)
        {
            double windChill = 13.12 + 0.6215 * temperature - 11.37 * Math.pow(windSpeed, 0.16)
                    + 0.3965 * temperature * Math.pow(windSpeed, 0.16);
            return (int) Math.round(windChill);
        }

        return temperature;
    }

    private void setCondition(int condition)
    {
        TextView textCondition = findViewById(R.id.textCondition);
        ImageView imageIcon = findViewById(R.id.imageIcon);

        if (condition >= 200 && condition < 300)
        {
            textCondition.setText(R.string.thunderstorm);
            imageIcon.setImageResource(R.drawable.ic_thunderstorm);
        }
        else if (condition >= 300 && condition < 400)
        {
            textCondition.setText(R.string.drizzle);
            imageIcon.setImageResource(R.drawable.ic_drizzle);
        }
        else if (condition >= 500 && condition < 600)
        {
            textCondition.setText(R.string.rain);
            imageIcon.setImageResource(R.drawable.ic_rain);
        }
        else if (condition >= 600 && condition < 700)
        {
            textCondition.setText(R.string.snow);
            imageIcon.setImageResource(R.drawable.ic_snow);
        }
        else if (condition >= 700 && condition < 800)
        {
            // TODO
        }
        else if (condition == 800)
        {
            textCondition.setText(R.string.clear);
            imageIcon.setImageResource(R.drawable.ic_clear);
        }
        else if (condition == 801 || condition == 802)
        {
            textCondition.setText(R.string.partly_cloudy);
            imageIcon.setImageResource(R.drawable.ic_partly_cloudy);
        }
        else if (condition == 803)
        {
            textCondition.setText(R.string.mostly_cloudy);
            imageIcon.setImageResource(R.drawable.ic_mostly_cloudy);
        }
        else if (condition == 804)
        {
            textCondition.setText(R.string.cloudy);
            imageIcon.setImageResource(R.drawable.ic_cloudy);
        }
    }

    private class JsonTask extends AsyncTask<String, Void, String>
    {
        @Override
        protected String doInBackground(String... strings)
        {
            String result = "";

            try
            {
                URL url = new URL(strings[0]);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                InputStream inputStream = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

                int data = inputStreamReader.read();

                while (data != -1)
                {
                    char current = (char) data;
                    result += current;

                    data = inputStreamReader.read();
                }
            }
            catch (MalformedURLException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(String string)
        {
            super.onPostExecute(string);

            try
            {
                JSONObject jsonObject = new JSONObject(string);

                String weather = jsonObject.getString("weather");
                JSONArray jsonArray = new JSONArray(weather);

                for (int i = 0; i < jsonArray.length(); i++)
                {
                    JSONObject jsonPart = jsonArray.getJSONObject(i);
                    setCondition(jsonPart.getInt("id"));
                }

                JSONObject main = jsonObject.getJSONObject("main");
                Double kelvin = main.getDouble("temp");
                int temperature = kelvinToCelsius(kelvin);

                TextView textTemperature = findViewById(R.id.textTemperature);
                textTemperature.setText(String.valueOf(temperature));

                JSONObject wind = jsonObject.getJSONObject("wind");
                Double speed = wind.getDouble("speed");

                TextView textFeelsLike = findViewById(R.id.textFeelsLike);
                textFeelsLike.setText("Feels Like " + String.valueOf(getWindChill(temperature, speed)) + "°");
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
    }
}
