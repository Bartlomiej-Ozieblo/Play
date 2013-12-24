package com.gagaryn.play.drawer_menu;


import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.gagaryn.play.MainActivity;
import com.gagaryn.play.R;

public class MenuList extends Fragment {

    public static final int POS_PLAYER = 0;
    public static final int POS_LIBRARY = 1;
    public static final int POS_EQUALIZER = 2;
    public static final int POS_HELP = 3;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.drawer_menu_list, container, false);
        assert rootView != null;

        ListView listView = (ListView) rootView.findViewById(R.id.drawer_menu_list_listview);
        listView.setAdapter(new CustomAdapter(getActivity(), 0));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                MainActivity activity = (MainActivity) getActivity();
                switch (i) {
                    case POS_PLAYER:
                        activity.closeNavDrawer();
                        activity.showPlayerFragment();
                        break;
                    case POS_LIBRARY:
                        activity.closeNavDrawer();
                        activity.showAudioList();
                        break;
                    case POS_EQUALIZER:
                        activity.closeNavDrawer();
                        break;
                    case POS_HELP:
                        activity.closeNavDrawer();
                        String url = "https://github.com/gagaryn007/PlayProjectRepo/";
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(url));
                        startActivity(intent);
                        break;
                }
            }
        });

        return rootView;
    }

    private class CustomAdapter extends ArrayAdapter {

        public CustomAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater layoutInflater =
                    (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = layoutInflater.inflate(R.layout.drawer_menu_item, null, false);
            assert view != null;

            ImageView thumbnail = (ImageView) view.findViewById(R.id.drawer_menu_item_img);
            TextView label = (TextView) view.findViewById(R.id.drawer_menu_item_label);

            Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Raleway Thin.ttf");
            label.setTypeface(typeface);

            switch (position) {
                case POS_PLAYER:
                    thumbnail.setImageResource(R.drawable.av_play);
                    label.setText("Player");
                    break;
                case POS_LIBRARY:
                    thumbnail.setImageResource(R.drawable.collections_collection);
                    label.setText("My library");
                    break;
                case POS_EQUALIZER:
                    thumbnail.setImageResource(R.drawable.action_settings);
                    label.setText("Equalizer");
                    break;
                case POS_HELP:
                    label.setText("Help");
                    thumbnail.setImageResource(R.drawable.action_help);
            }

            return view;
        }
    }
}
