package com.suraj.weatherinfo;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.suraj.weatherinfo.MainActivity.MyInterface;
import com.suraj.weatherinfo.pojo.WeatherInfo;

public class WeatherInfoFragment extends Fragment 
implements OnClickListener, OnItemClickListener,MyInterface {

	private Button buttonGo;
	private EditText cityNameEditText;
	private ListView weatherListView;
	private ArrayList<WeatherInfo> arrayWeatherInfos;
	private GetWeatherInfoAsyncTask asyncTask;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_main, container,
				false);
		initialise(rootView);
		return rootView;
	}

	private void initialise(View rootView) {
		buttonGo = (Button)rootView.findViewById(R.id.BTN_go);
		cityNameEditText = (EditText)rootView.findViewById(R.id.EDIT_cityName);
		buttonGo.setOnClickListener(this);
		weatherListView = (ListView)rootView.findViewById(R.id.weatherDetailsListView);
		weatherListView.setOnItemClickListener(this);
		arrayWeatherInfos = new ArrayList<WeatherInfo>();
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.BTN_go:
			String cityName = cityNameEditText.getText().toString().trim();
			if((cityName!=null)&&!(cityName.equals(""))){
				String apiKey = "ff030844d2c29e3573e00daded971899";
				String url = 
						"http://api.openweathermap.org/data/2.5/forecast/daily?q="+cityName+"&cnt=14&APPID="+apiKey;

				if(isNetworkConnected()){
					asyncTask = new GetWeatherInfoAsyncTask(getActivity());
					asyncTask.setListener(this);
					asyncTask.execute(url);
				}else{
					showMessage("You are not connected to internet");
				}
			}
			else{
				showMessage("Please enter the city name and continue");
			}
			break;

		default:
			break;
		}
	}

	private void drawListView(ArrayList<WeatherInfo> arrayWeatherInfos2) {
		WeatherInfoBaseAdapter weatherInfoBaseAdapter = 
				new WeatherInfoBaseAdapter(getActivity(), arrayWeatherInfos2);
		weatherListView.setAdapter(weatherInfoBaseAdapter);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {

	}

	@Override
	public void setweatherData(ArrayList<WeatherInfo> weatherInfoArray,final String responseCode) {

		this.arrayWeatherInfos = weatherInfoArray;
		Log.d("arrayWeatherInfos", arrayWeatherInfos.toString());
		getActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if(responseCode.equals("200")){
					drawListView(arrayWeatherInfos);}
				else{
					showMessage("You have entered wrong city!!!");
				}
			}
		});
	}

	public boolean isNetworkConnected(){
		ConnectivityManager conMgr = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mobile_info = conMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		NetworkInfo wifi_info = conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return ((mobile_info!=null && mobile_info.getState() == NetworkInfo.State.CONNECTED)
				|| (wifi_info!=null && wifi_info.getState() == NetworkInfo.State.CONNECTED));
	}

	private void showMessage(String message){
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Error!!!");
		builder.setMessage(message);
		builder.setCancelable(false);
		builder.setPositiveButton("OK", null);
		builder.show();
	}
}
