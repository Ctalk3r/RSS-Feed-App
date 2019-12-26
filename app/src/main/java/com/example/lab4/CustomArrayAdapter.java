package com.example.lab4;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

public class CustomArrayAdapter extends ArrayAdapter {

    private final Context context;
    private Button button;
    private ArrayList<RssFeedModel> rssFeedModels;

    public CustomArrayAdapter(Context context, ArrayList<RssFeedModel> rssFeedModels, Button button)
    {
        super(context, R.layout.note_layout, rssFeedModels);

        this.context = context;
        this.rssFeedModels = rssFeedModels;
        this.button = button;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.note_layout, parent, false);

        TextView title = view.findViewById(R.id.note_title);
        title.setText(rssFeedModels.get(position).title);
        TextView body = view.findViewById(R.id.note_body);
        if (rssFeedModels.get(position).description != null && rssFeedModels.get(position).description.length() >= 200)
            body.setText(rssFeedModels.get(position).description.substring(0, 400));
        else
            body.setText(rssFeedModels.get(position).description);

        // TextView date = view.findViewById(R.id.note_date);
        // date.setText(rssFeedModels.get(position).link);
        ImageView image = view.findViewById(R.id.note_image);
        if (rssFeedModels.get(position).image != null) {
            if (button.getVisibility() == View.VISIBLE) {
                String url = rssFeedModels.get(position).image;
                String fileName = url.substring(url.lastIndexOf('/') + 1, url.length() );
                String path = context.getCacheDir().toString() + File.separator + "images" + File.separator + fileName;
                image.setImageDrawable(Drawable.createFromPath(path));
            } else {
                try {
                    Picasso.get()
                            .load(rssFeedModels.get(position).image)
                            .resize(100, 100).into(image);
                }
                catch (Exception e) {

                }
            }
        }
        return view;
    }


    public void removeNote(int position)
    {
        rssFeedModels.remove(position);
    }

    public long getNoteId(int pos)
    {
        return rssFeedModels.get(pos).id;
    }

    public void setArray(ArrayList<RssFeedModel> array)
    {
        rssFeedModels.clear();
        rssFeedModels.addAll(array);
        notifyDataSetChanged();
    }
}
