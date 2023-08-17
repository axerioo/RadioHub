package com.k4m1l.radiohub;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.k4m1l.radiohub.player.PlaybackStatus;
import com.k4m1l.radiohub.player.RadioManager;
import com.k4m1l.radiohub.util.Shoutcast;
import com.k4m1l.radiohub.util.ShoutcastHelper;
import com.k4m1l.radiohub.util.ShoutcastListAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.playTrigger)
    ImageButton trigger;

    @BindView(R.id.listview)
    ListView listView;

    @BindView(R.id.name)
    TextView textView;

    @BindView(R.id.sub_player)
    View subPlayer;

    RadioManager radioManager;
    String streamURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        radioManager = RadioManager.with(this);
        listView.setAdapter(new ShoutcastListAdapter(this, ShoutcastHelper.retrieveShoutcasts(this)));
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        radioManager.unbind();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        radioManager.bind();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Subscribe
    public void onEvent(String status){
        switch (status){
            case PlaybackStatus.LOADING:
                Toast.makeText(this, R.string.buffering, Toast.LENGTH_SHORT).show();
                break;

            case PlaybackStatus.ERROR:
                Toast.makeText(this, R.string.no_stream, Toast.LENGTH_SHORT).show();
                break;
        }

        trigger.setImageResource(status.equals(PlaybackStatus.PLAYING)
                ? R.drawable.ic_pause_white_50
                : R.drawable.ic_play_white_50);
    }

    @OnClick(R.id.playTrigger)
    public void onClicked(){
        if(TextUtils.isEmpty(streamURL)) return;
        radioManager.playOrPause(streamURL);
    }

    @OnItemClick(R.id.listview)
    public void onItemClick(AdapterView<?> parent, View view, int position, long id){
        Shoutcast shoutcast = (Shoutcast) parent.getItemAtPosition(position);
        if(shoutcast == null){
            return;
        }

        textView.setText(shoutcast.getName());
        subPlayer.setVisibility(View.VISIBLE);
        streamURL = shoutcast.getUrl();
        radioManager.playOrPause(streamURL);
    }
}
