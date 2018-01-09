package guepardoapps.lucahome.builders;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
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
import guepardoapps.lucahome.common.service.MeterListService;
import guepardoapps.lucahome.common.service.TemperatureService;
import guepardoapps.lucahome.service.NavigationService;
import guepardoapps.lucahome.views.MeterDataActivity;
import guepardoapps.lucahome.views.PuckJsActivity;
import guepardoapps.lucahome.views.TemperatureActivity;

public class MapContentViewBuilder {
    private static final String TAG = MapContentViewBuilder.class.getSimpleName();

    private Context _context;

    private List<TextView> _mapContentViewList = new ArrayList<>();
    private SerializableList<MapContent> _tempMapContentList;

    /**
     * Initiate UI
     */
    private ImageView _imageView;
    private RelativeLayout _relativeLayoutMapPaint;
    private Point _size;

    public MapContentViewBuilder(@NonNull Context context) {
        _context = context;
    }

    public void Initialize() {
        _imageView = ((AppCompatActivity) _context).findViewById(R.id.skeletonList_backdrop_image_main);
        ViewTreeObserver imageViewViewTreeObserver = _imageView.getViewTreeObserver();
        imageViewViewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {
                Logger.getInstance().Debug(TAG, "_imageView imageViewViewTreeObserver onGlobalLayout");
                calculateReferenceSize();
                _imageView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });

        _relativeLayoutMapPaint = ((AppCompatActivity) _context).findViewById(R.id.skeletonList_backdrop_relativeLayoutPaint_main);
        ViewTreeObserver relativeLayoutViewTreeObserver = _relativeLayoutMapPaint.getViewTreeObserver();
        relativeLayoutViewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {
                Logger.getInstance().Debug(TAG, "_relativeLayoutMapPaint relativeLayoutViewTreeObserver onGlobalLayout");
                calculateReferenceSize();
                _relativeLayoutMapPaint.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
    }

    public void CreateMapContentViewList(@NonNull SerializableList<MapContent> mapContentList) {
        _mapContentViewList.clear();
        _relativeLayoutMapPaint.removeAllViews();

        if (_size == null) {
            Logger.getInstance().Error(TAG, "_size is null!");
            _tempMapContentList = mapContentList;
            return;
        }

        for (int index = 0; index < mapContentList.getSize(); index++) {
            final MapContent mapContent = mapContentList.getValue(index);

            final TextView newTextView = new TextView(_context);

            newTextView.setVisibility(mapContent.IsVisible() ? View.VISIBLE : View.GONE);

            newTextView.setGravity(Gravity.CENTER);
            newTextView.setTextSize(10);
            newTextView.setTextColor(mapContent.GetTextColor());
            newTextView.setText(mapContent.GetShortName());

            newTextView.setBackgroundResource(mapContent.GetDrawable());

            newTextView.setOnClickListener(view -> {
                if (mapContent.GetButtonClick(_context) != null) {
                    mapContent.GetButtonClick(_context).run();
                } else {
                    switch (mapContent.GetDrawingType()) {
                        case Meter:
                            Bundle meterData = new Bundle();
                            meterData.putSerializable(MeterListService.MeterDataIntent, mapContent.GetDrawingTypeId());
                            NavigationService.getInstance().NavigateToActivityWithData(_context, MeterDataActivity.class, meterData);
                            break;

                        case PuckJS:
                            /* TODO
                            Bundle puckJsData = new Bundle();
                            puckJsData.putSerializable(PuckJsListService.PuckJsIntent, mapContent.GetDrawingTypeId());
                            NavigationService.getInstance().NavigateToActivityWithData(_context, PuckJsActivity.class, puckJsData);*/
                            NavigationService.getInstance().NavigateToActivity(_context, PuckJsActivity.class);
                            break;

                        case Temperature:
                            Bundle temperatureData = new Bundle();
                            temperatureData.putSerializable(TemperatureService.TemperatureDataIntent, mapContent.GetDrawingTypeId());
                            NavigationService.getInstance().NavigateToActivityWithData(_context, TemperatureActivity.class, temperatureData);
                            break;

                        default:
                            Toasty.warning(_context, String.format(Locale.getDefault(), "No button action for %s", mapContent.GetShortName()), Toast.LENGTH_LONG).show();
                            break;
                    }
                }
            });

            int positionX = _size.x * mapContent.GetPosition()[0] / 100;
            int positionY = _size.y - (_size.y * mapContent.GetPosition()[1] / 100);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(positionX, positionY, 0, 0);
            newTextView.setLayoutParams(layoutParams);

            newTextView.setTag(mapContent.GetId());

            Logger.getInstance().Information(TAG, String.format(Locale.getDefault(), "Created new TextView: %s", newTextView));

            _mapContentViewList.add(newTextView);
        }

        _tempMapContentList = null;
    }

    public void AddViewsToMap() {
        _relativeLayoutMapPaint.removeAllViews();
        for (TextView mapContentView : _mapContentViewList) {
            _relativeLayoutMapPaint.addView(mapContentView);
        }
    }

    private void calculateReferenceSize() {
        if (_imageView == null || _relativeLayoutMapPaint == null) {
            Logger.getInstance().Warning(TAG, "ImageView or RelativeLayoutMapPaint is null!");
            return;
        }

        int width = _imageView.getWidth() > _relativeLayoutMapPaint.getWidth() ? _imageView.getWidth() : _relativeLayoutMapPaint.getWidth();
        int height = _imageView.getHeight() > _relativeLayoutMapPaint.getHeight() ? _imageView.getHeight() : _relativeLayoutMapPaint.getHeight();
        _size = new Point(width - 75, height - 75);

        Logger.getInstance().Information(TAG, String.format(Locale.getDefault(),
                "_relativeLayoutMapPaint: Width: %d Height: %d; _imageView: Width: %d Height: %d; _size: x: %d y: %d",
                _relativeLayoutMapPaint.getWidth(), _relativeLayoutMapPaint.getHeight(),
                _imageView.getWidth(), _imageView.getHeight(),
                _size.x, _size.y));

        if (_tempMapContentList != null) {
            CreateMapContentViewList(_tempMapContentList);
            AddViewsToMap();
        }
    }
}
