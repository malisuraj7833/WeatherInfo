package com.suraj.weatherinfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.suraj.weatherinfo.MainActivity.MyInterface;
import com.suraj.weatherinfo.pojo.WeatherInfo;

public class GetWeatherInfoAsyncTask extends AsyncTask<String, Void, String>
{
	private Activity context;
	private URL imageurl;
	private HttpURLConnection conn;
	private InputStream is;
	private Bitmap bitmap;

	public GetWeatherInfoAsyncTask(Activity context){
		this.context = context;
	}

	private ProgressDialog dialog;
	private WeatherInfo weatherInfo;
	private ArrayList<WeatherInfo> weatherInfoArrayList;
	private MyInterface listener;

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		dialog = new ProgressDialog(context);
		dialog.setMessage("Getting data...");
		dialog.setCancelable(false);
		dialog.show();
	}

	@Override
	protected String doInBackground(String... params) {
		String url = params[0];
		String weatherResponse = getWeatherInfoData(url);
		processJsonObject(weatherResponse);
		return weatherResponse;
	}

	@Override
	protected void onPostExecute(String result) {
		if(dialog!=null){
			dialog.cancel();
		}
		super.onPostExecute(result);
	}

	private void processJsonObject(String result) {
		JSONObject mainJsonObject;
		weatherInfoArrayList = new ArrayList<WeatherInfo>();
		try {
			mainJsonObject = new JSONObject(result);
			String responseCode = mainJsonObject.optString("cod");
			if(responseCode.equals("200")){
				int numberOfRecords = mainJsonObject.optInt("cnt");
				//city info
				JSONObject cityJsonObject = mainJsonObject.optJSONObject("city");
				String cityName = cityJsonObject.optString("name");
				String countryName = cityJsonObject.optString("country");
				//city coordinates
				JSONObject cityCordinatesJsonObject = cityJsonObject.optJSONObject("coord");
				double longitude = cityCordinatesJsonObject.optDouble("lon");
				double lattitude = cityCordinatesJsonObject.optDouble("lat");

				JSONArray listOfRecordsJsonArray = mainJsonObject.optJSONArray("list");
				int length = listOfRecordsJsonArray.length();
				for(int i=0;i<length;i++){
					weatherInfo = new WeatherInfo();
					weatherInfo.setCityName(cityName);
					weatherInfo.setLongitude(longitude);
					weatherInfo.setLattitude(lattitude);
					//"list" child object
					JSONObject childListObject = listOfRecordsJsonArray.getJSONObject(i);
					long unixTimeStamp = childListObject.optLong("dt");
					double pressure = childListObject.optDouble("pressure");
					int	humidity = childListObject.optInt("humidity");
					int	windSpeed = childListObject.optInt("speed");
					String formattedDate = convertDate(unixTimeStamp);
					//"temperature" object 
					JSONObject tempJsonObject = childListObject.optJSONObject("temp");
					double dayTemp = tempJsonObject.optDouble("day");
					double nightTemp = tempJsonObject.optDouble("night");
					double eveningTemp = tempJsonObject.optDouble("eve");
					double morningTemp = tempJsonObject.optDouble("morn");

					weatherInfo.setPressure(pressure);
					weatherInfo.setHumidity(humidity);
					weatherInfo.setWindSpeed(windSpeed);
					weatherInfo.setDate(formattedDate);
					weatherInfo.setDayTemp(dayTemp);
					weatherInfo.setNightTemp(nightTemp);
					weatherInfo.setEveningTemp(eveningTemp);
					weatherInfo.setMorningTemp(morningTemp);

					//"weather" array from "list" object
					JSONArray weatherJsonArray = childListObject.optJSONArray("weather");
					int weatherId = 0;
					String weatherMainStatus ="";
					String weatherIcon ="";
					String weatherStatusDescription =""; 
					int weatherLenghtArray = weatherJsonArray.length();
					for(int j=0;j<weatherLenghtArray;j++){
						JSONObject childWeatherObject = weatherJsonArray.getJSONObject(j);
						weatherId = childWeatherObject.optInt("id");
						weatherMainStatus = childWeatherObject.optString("main");
						weatherStatusDescription = childWeatherObject.optString("description");
						weatherIcon = childWeatherObject.optString("icon");

						weatherInfo.setWeatherStatus(weatherMainStatus);
						weatherInfo.setWeatherStatusDescription(weatherStatusDescription);
						weatherInfo.setIconName(weatherIcon);
						String imageUrl = "http://openweathermap.org/img/w/"+weatherIcon+".png";
						String imagePath = downloadImage(imageUrl);
						weatherInfo.setWeatherImagePath(imagePath);
						weatherInfoArrayList.add(weatherInfo);
					}
				}
				listener.setweatherData(weatherInfoArrayList,responseCode);
			}else if (responseCode.equals("404")){
				listener.setweatherData(weatherInfoArrayList,responseCode);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private String convertDate(long unixTimeStamp) {
		Date date = new Date(unixTimeStamp*1000L); // *1000 is to convert seconds to milliseconds
		SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy"); // the format of your date
		sdf.setTimeZone(TimeZone.getDefault()); // give a timezone reference for formating (see comment at the bottom
		String formattedDate = sdf.format(date);
		return formattedDate;
	}

	private String getWeatherInfoData(String url) {
		StringBuilder stringBuilder = new StringBuilder();
		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);
		try {
			HttpResponse response = client.execute(httpGet);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if(statusCode==200){
				HttpEntity entity = response.getEntity();
				InputStream stream = entity.getContent();
				BufferedReader bufferedReader = new BufferedReader
						(new InputStreamReader(stream));
				String line;
				while((line=bufferedReader.readLine())!=null){
					stringBuilder.append(line);
				}
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return stringBuilder.toString();
	}

	public void setListener(MyInterface listener) {
		this.listener = listener;
	}

	public String downloadImage(String url) {
		String imagePath="";
		try {
			imageurl = new URL(url);
			Log.d("URL:::", url);
			conn = (java.net.HttpURLConnection) imageurl.openConnection();
			conn.setReadTimeout(10000);
			conn.setConnectTimeout(15000);
			conn.setRequestMethod("POST");
			conn.setDoInput(true);
			conn.connect();
			is = conn.getInputStream();

			String PATH = context.getFilesDir() + "/";
			File dir = new File(PATH);
			if (!dir.exists())
				dir.mkdir();
			String[] stringArray = url.split("/");
			File newfFile = new File(dir, stringArray[stringArray.length - 1]);
			FileOutputStream out = new FileOutputStream(newfFile);
			bitmap = BitmapFactory.decodeStream(is);
			bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
			Log.d("IMAGE PATH::::", PATH + stringArray[stringArray.length - 1]);
			imagePath = ""+PATH + stringArray[stringArray.length - 1];
			is.close();
			out.flush();
			out.close();
			conn.disconnect();
		}
		catch (MalformedURLException e) {
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return imagePath;
	}
}