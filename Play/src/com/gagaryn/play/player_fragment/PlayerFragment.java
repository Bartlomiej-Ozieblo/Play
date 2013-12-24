package com.gagaryn.play.player_fragment;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.gagaryn.play.CustomApplication;
import com.gagaryn.play.MainActivity;
import com.gagaryn.play.OnAudioStateChangeListener;
import com.gagaryn.play.R;
import com.gagaryn.play.commands.Command;
import com.gagaryn.play.commands.CommandNext;
import com.gagaryn.play.commands.CommandPause;
import com.gagaryn.play.commands.CommandPlay;
import com.gagaryn.play.commands.CommandPrevious;
import com.gagaryn.play.commands.CommandShuffle;
import com.gagaryn.play.containers.Song;
import com.gagaryn.play.utils.MusicUtils;
import com.gagaryn.play.utils.TimeUtils;

import java.util.Timer;

public class PlayerFragment extends Fragment implements OnAudioStateChangeListener, HoloCircularProgressBar.OnSizeSetupReady {

    private SparseArray<Command> commandSparseArray = new SparseArray<Command>();

    private ImageButton playButton;
    private SeekBar seekBar;
    private HoloCircularProgressBar circularProgressBar;
    private CDAlbumThumbnailView thumbnailView;
    private TextView currentProgressLabel;
    private TextView endProgressLabel;
    private TextView titleLabel;
    private TextView artistLabel;
    private Timer timer = new Timer();

    private SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            ((MainActivity) getActivity()).clearUpdateCallback();
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.clearUpdateCallback();
            mainActivity.seekTo(seekBar.getProgress());
            mainActivity.startUpdateCallback();
        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            MainActivity mainActivity = (MainActivity) getActivity();
            int id = view.getId();
            if (id == Command.COMMAND_PLAY) {
                if (mainActivity.isPlaying()) {
                    commandSparseArray.get(Command.COMMAND_PAUSE).execute(mainActivity);
                    playButton.setImageResource(R.drawable.av_play);
                } else {
                    commandSparseArray.get(id).execute(mainActivity);
                    playButton.setImageResource(R.drawable.av_pause);
                }
            } else {
                commandSparseArray.get(id).execute(mainActivity);
            }
        }
    };

    public PlayerFragment() {
        commandSparseArray.put(Command.COMMAND_PLAY, new CommandPlay());
        commandSparseArray.put(Command.COMMAND_PAUSE, new CommandPause());
        commandSparseArray.put(Command.COMMAND_PREVIOUS, new CommandPrevious());
        commandSparseArray.put(Command.COMMAND_NEXT, new CommandNext());
        commandSparseArray.put(Command.COMMAND_SHUFFLE, new CommandShuffle());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.player_fragment, container, false);

        if (savedInstanceState == null) {
            assert rootView != null;
            seekBar = (SeekBar) rootView.findViewById(R.id.player_seekbar);
            circularProgressBar = (HoloCircularProgressBar) rootView.findViewById(R.id.player_circular_progress_bar);
            thumbnailView = (CDAlbumThumbnailView) rootView.findViewById(R.id.player_thumbnail_view);
            currentProgressLabel = (TextView) rootView.findViewById(R.id.player_current_progress_label);
            endProgressLabel = (TextView) rootView.findViewById(R.id.player_end_progress_label);
            titleLabel = (TextView) rootView.findViewById(R.id.player_title_label);
            artistLabel = (TextView) rootView.findViewById(R.id.player_artist_label);

            playButton = (ImageButton) rootView.findViewById(R.id.player_play_button);
            ImageButton nextButton = (ImageButton) rootView.findViewById(R.id.player_next_button);
            ImageButton backButton = (ImageButton) rootView.findViewById(R.id.player_back_button);
            ImageButton shuffleButton = (ImageButton) rootView.findViewById(R.id.player_shuffle_button);
//            ImageButton repeatButton = (ImageButton) rootView.findViewById(R.id.player_repeat_button);
            if (((MainActivity) getActivity()).isPlaying()) {
                playButton.setImageResource(R.drawable.av_pause);
            } else {
                playButton.setImageResource(
                        R.drawable.av_play);
            }

            playButton.setOnClickListener(onClickListener);
            nextButton.setOnClickListener(onClickListener);
            backButton.setOnClickListener(onClickListener);
            shuffleButton.setOnClickListener(onClickListener);

            seekBar.setMax(1000);
            seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
            circularProgressBar.setProgressBackgroundColor(0x00000000);
            circularProgressBar.setProgressColor(getResources().getColor(android.R.color.holo_blue_bright));
            circularProgressBar.setMarkerEnabled(false);
            circularProgressBar.setThumbEnabled(false);
            circularProgressBar.setProgress(0);
            circularProgressBar.setSetupReadyListener(this);

            AssetManager assets = getActivity().getAssets();
            Typeface typeface = Typeface.createFromAsset(assets, "fonts/Raleway Thin.ttf");
            artistLabel.setTypeface(typeface);
            titleLabel.setTypeface(typeface);
            currentProgressLabel.setTypeface(typeface);
            endProgressLabel.setTypeface(typeface);
        }

        Song song = ((MainActivity) getActivity()).getCurrentSong();
        if (song != null) {
            setLabels(song);
        }

        if (((MainActivity) getActivity()).isPlaying()) {
            onAudioPlay();
        }

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        ((CustomApplication) getActivity().getApplication()).setCurrent(CustomApplication.PLAYER);
        MainActivity activity = (MainActivity) getActivity();
        activity.getActionBar().setDisplayHomeAsUpEnabled(true);
        activity.setSwitcherIcon(R.drawable.collections_view_as_list);
    }

    @Override
    public void onSongChange(Song song, int historicalPosition, int currentPosition) {
        String albumArt = MusicUtils.getAlbumArtFromSong(getActivity().getContentResolver(), song);
        if (albumArt != null) {
            setThumbnail(albumArt);
        }

        setLabels(song);
        circularProgressBar.setProgress(0);
    }

    @Override
    public void onAudioPlay() {
        ((MainActivity) getActivity()).startUpdateCallback();
    }

    @Override
    public void onAudioPause() {
    }

    @Override
    public void onProgressChanged(long progress, long duration) {
        int percentage = TimeUtils.getProgressPercentage(progress, duration);
        String formatProgress = TimeUtils.millisecondToFormat(progress);
        currentProgressLabel.setText(formatProgress);
        seekBar.setProgress(percentage);
        circularProgressBar.setProgress(TimeUtils.getProgressFloat(progress, duration));
    }

    @Override
    public void onReady(float radius) {
        Song song = ((MainActivity) getActivity()).getCurrentSong();
        if (song != null) {
            String albumArt = MusicUtils.getAlbumArtFromSong(getActivity().getContentResolver(), song);
            if (albumArt != null) {
                setThumbnail(albumArt);
            }
        }
    }

    public void setThumbnail(String albumArt) {
        thumbnailView.setData(albumArt, circularProgressBar.getRadius(), getActivity().getActionBar().getHeight());
    }

    private void setLabels(Song song) {
        titleLabel.setText(song.getTitle());
        artistLabel.setText(song.getArtist());
        currentProgressLabel.setText("0:00");
        String durationFormat = TimeUtils.millisecondToFormat(song.getDuration());
        endProgressLabel.setText(durationFormat);
        seekBar.setProgress(0);
    }
}
