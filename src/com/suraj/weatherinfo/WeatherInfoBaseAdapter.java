package com.suraj.weatherinfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.suraj.weatherinfo.pojo.WeatherInfo;

public class WeatherInfoBaseAdapter extends BaseAdapter {


	ArrayList<WeatherInfo> arrayList = new ArrayList<WeatherInfo>();
	Context context;

	public static class ViewHolder{
		public final TextView weatherStatusTextView;
		public final TextView dateTextView;
		public final TextView temperatureTextView;
		public final TextView humidityValueTextView;
		public final TextView windSpeedValueTextView;
		public final ImageView weatherStatusImage;
		public ViewHolder(View convertView) {
			weatherStatusTextView = (TextView)convertView.findViewById(R.id.weatherStatusTextView);
			dateTextView = (TextView)convertView.findViewById(R.id.dateTextView);
			temperatureTextView = (TextView)convertView.findViewById(R.id.temperatureTextView);
			humidityValueTextView = (TextView)convertView.findViewById(R.id.humidityValueTextView);
			windSpeedValueTextView = (TextView)convertView.findViewById(R.id.windSpeedValueTextView);
			weatherStatusImage = (ImageView) convertView.findViewById(R.id.weatherStatusImage);
		}
	}

	public WeatherInfoBaseAdapter(Context context,ArrayList<WeatherInfo> weatherInfoArray) {
		this.arrayList = weatherInfoArray;
		this.context = context;
	}

	@Override
	public int getCount() {
		return arrayList.size();
	}

	@Override
	public WeatherInfo getItem(int position) {
		return arrayList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.weather_info_list_item, parent, false);
			viewHolder = new ViewHolder(convertView);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		WeatherInfo weatherInfo = getItem(position);
		File file = new File(weatherInfo.getWeatherImagePath());
		if(file.exists()){
			try {
				Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
				bitmap  = Bitmap.createScaledBitmap(bitmap, 200, 200, true);
				viewHolder.weatherStatusImage.setImageBitmap(bitmap);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		viewHolder.weatherStatusTextView.setText(weatherInfo.getWeatherStatus());
		viewHolder.dateTextView.setText(weatherInfo.getDate());
		int temp = (int) (weatherInfo.getDayTemp()-273);
		viewHolder.temperatureTextView.setText(""+temp);
		viewHolder.humidityValueTextView.setText(""+weatherInfo.getHumidity()+"%");
		viewHolder.windSpeedValueTextView.setText(weatherInfo.getWindSpeed()+"m/s");
		return convertView;
	}
}
