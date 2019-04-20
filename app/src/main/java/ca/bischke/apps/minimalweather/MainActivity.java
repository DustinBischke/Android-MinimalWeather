package ca.bischke.apps.minimalweather;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
{
    private final String TAG = "MinimalWeather";
    private final String url = "https://api.openweathermap.org/data/2.5/weather?q=";
    private final String apiKey = "45f6bbc6afba649813f1027b77c00b4f";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setDateTime();

        String city = "Carlow";
        String countryCode = "IE";

        JsonTask jsonTask = new JsonTask();
        jsonTask.execute(url + city + "," + countryCode + "&appid=" + apiKey);

        TextView textCity = findViewById(R.id.textCity);
        textCity.setText(city);

        TextView textCountry = findViewById(R.id.textCountry);
        textCountry.setText(getCountryFromCode(countryCode));
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
            double windChill = 13.12 + 0.6215 * temperature - 11.37 * Math.pow(windSpeed, 0.16) + 0.3965 * temperature * Math.pow(windSpeed, 0.16);
            return (int) Math.round(windChill);
        }

        return temperature;
    }

    private String getCountryFromCode(String countryCode)
    {
        Locale locale = new Locale("", countryCode);
        return locale.getDisplayCountry();
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

                    TextView textCondition = findViewById(R.id.textCondition);
                    textCondition.setText(jsonPart.getString("main"));
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
