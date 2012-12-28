package se.wirelesser.wwwt.adapter;
 
import se.wirelesser.wwwt.R;
 
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
 
public class MenuArrayAdapter extends ArrayAdapter<String> {
	private final Context context;
	private final String[] values;
 
	public MenuArrayAdapter(Context context, String[] values) {
		super(context, R.layout.activity_main, values);
		this.context = context;
		this.values = values;
	}
 
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
		View rowView = inflater.inflate(R.layout.activity_main, parent, false);
		TextView textView = (TextView) rowView.findViewById(R.id.label);
		//ImageView imageView = (ImageView) rowView.findViewById(R.id.logo);
		textView.setText(values[position]);

		//imageView.setImageResource(R.drawable.image1);
 
		return rowView;
	}
}