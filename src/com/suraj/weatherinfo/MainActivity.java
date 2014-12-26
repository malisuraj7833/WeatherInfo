package com.suraj.weatherinfo;

import java.util.ArrayList;

import com.suraj.weatherinfo.pojo.WeatherInfo;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
			.add(R.id.container, new WeatherInfoFragment()).commit();
		}
	}
	
	public interface MyInterface{
		void setweatherData(ArrayList<WeatherInfo> weatherInfo, String responseCode);
	}
}
