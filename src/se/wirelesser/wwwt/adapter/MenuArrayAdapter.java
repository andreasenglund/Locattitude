package se.wirelesser.wwwt.adapter;
 
import java.util.ArrayList;
import java.util.List;

import se.wirelesser.wwwt.MyApplicationHelper;
import se.wirelesser.wwwt.QuestionResponseListActivity;
import se.wirelesser.wwwt.R;
 
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
 
public class MenuArrayAdapter extends ArrayAdapter<String> {
	private final Context context;
	private final List<String> values;
 
	public MenuArrayAdapter(Context context, List<String> list) {
		super(context, R.layout.activity_main, list);
		this.context = context;
		this.values = list;
	}
 
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
		View rowView = inflater.inflate(R.layout.activity_main, parent, false);
		TextView textView = (TextView) rowView.findViewById(R.id.label);
		//ImageView imageView = (ImageView) rowView.findViewById(R.id.logo);
		textView.setText(values.get(position));

		//imageView.setImageResource(R.drawable.image1);
 
		return rowView;
	}
}