package guepardoapps.lucahome.views;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.widget.ListAdapter;
import android.widget.ListView;

import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.common.dto.MainListViewItemDto;
import guepardoapps.lucahome.customadapter.MainListViewAdapter;
import guepardoapps.lucahome.enums.TargetActivity;

public class MainView extends Activity {

    private static final String TAG = MainView.class.getSimpleName();

    private List<MainListViewItemDto> _viewItemList = new ArrayList<>();

    private ListAdapter _listAdapter;
    private ListView _mainListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_basic_list);

        LucaHomeLogger logger = new LucaHomeLogger(TAG);
        logger.Debug(MainView.class.getName() + " onCreate");

        _viewItemList.add(new MainListViewItemDto(R.drawable.socket, TargetActivity.SOCKET.GetName()));
        _viewItemList.add(new MainListViewItemDto(R.drawable.scheduler, TargetActivity.SCHEDULE.GetName()));
        _viewItemList.add(new MainListViewItemDto(R.drawable.timer, TargetActivity.TIMER.GetName()));
        _viewItemList.add(new MainListViewItemDto(R.drawable.ic_launcher, TargetActivity.MEDIA_MIRROR.GetName()));
        _viewItemList.add(new MainListViewItemDto(R.drawable.birthday, TargetActivity.BIRTHDAYS.GetName()));
        _viewItemList.add(new MainListViewItemDto(R.drawable.shopping, TargetActivity.SHOPPING_LIST.GetName()));
        _viewItemList.add(new MainListViewItemDto(R.drawable.menu, TargetActivity.MENU.GetName()));
        //_viewItemList.add(new MainListViewItemDto(R.xml.circle_blue, TargetActivity.GRAVITY.GetName()));

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.basicListWatchViewStub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                _mainListView = (ListView) stub.findViewById(R.id.basicListView);
                _listAdapter = new MainListViewAdapter(MainView.this, _viewItemList);
                _mainListView.setAdapter(_listAdapter);
            }
        });
    }
}