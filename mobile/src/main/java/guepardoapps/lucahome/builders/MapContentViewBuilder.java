package guepardoapps.lucahome.builders;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import es.dmoral.toasty.Toasty;
import guepardoapps.lucahome.R;
import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.classes.MapContent;

public class MapContentViewBuilder {
    private static final String TAG = MapContentViewBuilder.class.getSimpleName();
    private Logger _logger;

    private Context _context;

    private List<TextView> _mapContentViewList = new ArrayList<>();
    private SerializableList<MapContent> _tempMapContentList;

    /**
     * Initiate UI
     */
    private RelativeLayout _relativeLayoutMapPaint;
    private Point _size;

    public MapContentViewBuilder(@NonNull Context context) {
        _logger = new Logger(TAG);
        _context = context;
    }

    public void Initialize() {
        _relativeLayoutMapPaint = (RelativeLayout) ((AppCompatActivity) _context).findViewById(R.id.skeletonList_backdrop_relativeLayoutPaint_main);

        ViewTreeObserver viewTreeObserver = _relativeLayoutMapPaint.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {
                init();
                _relativeLayoutMapPaint.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }

            private void init() {
                _size = new Point(_relativeLayoutMapPaint.getWidth(), _relativeLayoutMapPaint.getHeight());
                _logger.Debug("Size is: " + _size.toString());

                if (_tempMapContentList != null) {
                    CreateMapContentViewList(_tempMapContentList);
                    AddViewsToMap();
                }
            }
        });
    }

    public List<TextView> CreateMapContentViewList(@NonNull SerializableList<MapContent> mapContentList) {
        Clear();

        if (_size == null) {
            _logger.Error("_size is null!");
            _tempMapContentList = mapContentList;
            return _mapContentViewList;
        }

        for (int index = 0; index < mapContentList.getSize(); index++) {
            final MapContent mapContent = mapContentList.getValue(index);

            final TextView newTextView = new TextView(_context);

            newTextView.setVisibility(mapContent.IsVisible() ? View.VISIBLE : View.GONE);

            newTextView.setGravity(Gravity.CENTER);
            newTextView.setTextSize(10);
            newTextView.setTextColor(Color.WHITE);
            newTextView.setText(mapContent.ButtonText());

            newTextView.setBackgroundResource(mapContent.Drawable());

            newTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    _logger.Debug(String.format(Locale.getDefault(), "onClick %s", mapContent.ButtonText()));
                    if (mapContent.GetButtonClick(_context) != null) {
                        mapContent.GetButtonClick(_context).run();
                    } else {
                        Toasty.warning(_context, String.format(Locale.getDefault(), "No button action for %s", mapContent.ButtonText()), Toast.LENGTH_LONG).show();
                    }
                }
            });

            int positionX = _size.x - (_size.x * mapContent.GetPosition()[1] / 100) - 50;
            int positionY = (_size.y * mapContent.GetPosition()[0] / 100) - 25;
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(positionX, positionY, 0, 0);
            newTextView.setLayoutParams(layoutParams);

            newTextView.setTag(mapContent.GetId());

            _mapContentViewList.add(newTextView);
        }

        _tempMapContentList = null;

        return _mapContentViewList;
    }

    public void UpdateMapContentEntry(@NonNull MapContent updateMapContent) {
        _logger.Debug(String.format(Locale.getDefault(), "UpdateMapContentEntry: %s", updateMapContent));

        if (_size == null) {
            _logger.Error("_size is null!");
            return;
        }

        for (TextView mapContentView : _mapContentViewList) {
            if ((int) mapContentView.getTag() == updateMapContent.GetId()) {
                mapContentView.setVisibility(updateMapContent.IsVisible() ? View.VISIBLE : View.GONE);
                mapContentView.setText(updateMapContent.ButtonText());
                mapContentView.setBackgroundResource(updateMapContent.Drawable());

                int positionX = _size.x - (_size.x * updateMapContent.GetPosition()[1] / 100) - 50;
                int positionY = (_size.y * updateMapContent.GetPosition()[0] / 100) - 50;
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(positionX, positionY, 0, 0);
                mapContentView.setLayoutParams(layoutParams);
            }
        }
    }

    public void AddViewsToMap() {
        _relativeLayoutMapPaint.removeAllViews();
        for (TextView mapContentView : _mapContentViewList) {
            _relativeLayoutMapPaint.addView(mapContentView);
        }
    }

    public void Clear() {
        _mapContentViewList.clear();
        _relativeLayoutMapPaint.removeAllViews();
    }
}
