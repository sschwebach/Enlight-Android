package edu.wisc.engr.enlight;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class PatternSpinnerAdapter<T> extends ArrayAdapter<T> {
	Context context;
	ArrayList<Pattern> patterns;
	public PatternSpinnerAdapter(Context context, int resource, ArrayList<Pattern> p){
		super(context, resource);
		this.context = context;
		this.patterns = p;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		View toReturn;
		TextView text;
		if (convertView == null){
			LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			toReturn = inflater.inflate(R.layout.layout_spinner_item, parent, false);
		}else{
			toReturn = convertView;
		}
		text = (TextView) toReturn.findViewById(R.id.text_spinner_item);
		text.setText(patterns.get(position).toString());
		return toReturn;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		View toReturn;   
		TextView text;
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			toReturn = inflater.inflate(R.layout.layout_spinner_item, parent, false);
		}else{
			toReturn = convertView;
		}
		text = (TextView) toReturn.findViewById(R.id.text_spinner_item);
		text.setText(patterns.get(position).toString());
		toReturn.setBackgroundColor(context.getResources().getColor(R.color.badgergold));
		toReturn.setPadding(0, (int) MainActivity.convertDpToPixel(5, context), (int) MainActivity.convertDpToPixel(5, context), (int) MainActivity.convertDpToPixel(5, context));
		return toReturn;
	}

	@Override
	public int getCount(){
		return patterns.size();
	}
}
