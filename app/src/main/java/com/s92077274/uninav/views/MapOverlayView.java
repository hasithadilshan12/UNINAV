package com.s92077274.uninav.views;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.s92077274.uninav.models.MapPoint;
import com.s92077274.uninav.models.NavigationRoute;
import java.util.ArrayList;
import java.util.List;

public class MapOverlayView extends View {
    private List<MapPoint> mapPoints = new ArrayList<>();
    private NavigationRoute currentRoute;
    private MapPoint userLocation;
    private Paint markerPaint, routePaint, userLocationPaint, textPaint, textBackgroundPaint;
    private OnMapPointClickListener clickListener;
    private Matrix imageMatrix = new Matrix();
    private RectF imageRect = new RectF();

    public interface OnMapPointClickListener {
        void onMapPointClick(MapPoint point);
    }

    public MapOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaints();
    }

    private void initPaints() {

        markerPaint = new Paint();
        markerPaint.setColor(Color.RED);
        markerPaint.setStyle(Paint.Style.FILL);
        markerPaint.setAntiAlias(true);


        routePaint = new Paint();
        routePaint.setColor(Color.parseColor("#1565C0"));
        routePaint.setStrokeWidth(8);
        routePaint.setStyle(Paint.Style.STROKE);
        routePaint.setAntiAlias(true);


        userLocationPaint = new Paint();
        userLocationPaint.setColor(Color.parseColor("#4CAF50"));
        userLocationPaint.setStyle(Paint.Style.FILL);
        userLocationPaint.setAntiAlias(true);


        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(32);
        textPaint.setAntiAlias(true);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);


        textBackgroundPaint = new Paint();
        textBackgroundPaint.setColor(Color.parseColor("#AA000000"));
        textBackgroundPaint.setStyle(Paint.Style.FILL);
        textBackgroundPaint.setAntiAlias(true);
    }

    public void setMapPoints(List<MapPoint> points) {
        this.mapPoints = points;
        invalidate();
    }

    public void setUserLocation(MapPoint location) {
        this.userLocation = location;
        invalidate();
    }

    public void setRoute(NavigationRoute route) {
        this.currentRoute = route;
        invalidate();
    }

    public void setOnMapPointClickListener(OnMapPointClickListener listener) {
        this.clickListener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        if (currentRoute != null && currentRoute.waypoints.size() > 1) {
            drawRoute(canvas);
        }


        for (MapPoint point : mapPoints) {
            if (!point.isUserLocation) {
                drawLocationMarker(canvas, point);
            }
        }


        if (userLocation != null) {
            drawUserLocation(canvas, userLocation);
        }
    }

    private void drawRoute(Canvas canvas) {
        if (currentRoute.waypoints.size() < 2) return;

        Path path = new Path();
        MapPoint first = currentRoute.waypoints.get(0);
        float startX = first.x * getWidth();
        float startY = first.y * getHeight();
        path.moveTo(startX, startY);

        for (int i = 1; i < currentRoute.waypoints.size(); i++) {
            MapPoint point = currentRoute.waypoints.get(i);
            float x = point.x * getWidth();
            float y = point.y * getHeight();
            path.lineTo(x, y);
        }

        canvas.drawPath(path, routePaint);
    }

    private void drawLocationMarker(Canvas canvas, MapPoint point) {
        float x = point.x * getWidth();
        float y = point.y * getHeight();


        canvas.drawCircle(x, y, 20, markerPaint);


        Paint innerPaint = new Paint();
        innerPaint.setColor(Color.WHITE);
        innerPaint.setAntiAlias(true);
        canvas.drawCircle(x, y, 12, innerPaint);


        String label = point.name;
        Rect textBounds = new Rect();
        textPaint.getTextBounds(label, 0, label.length(), textBounds);

        float textX = x - textBounds.width() / 2f;
        float textY = y - 40;

        RectF bgRect = new RectF(
                textX - 10,
                textY - textBounds.height() - 5,
                textX + textBounds.width() + 10,
                textY + 5
        );

        canvas.drawRoundRect(bgRect, 8, 8, textBackgroundPaint);
        canvas.drawText(label, textX, textY, textPaint);
    }

    private void drawUserLocation(Canvas canvas, MapPoint point) {
        float x = point.x * getWidth();
        float y = point.y * getHeight();


        Paint accuracyPaint = new Paint();
        accuracyPaint.setColor(Color.parseColor("#334CAF50"));
        accuracyPaint.setAntiAlias(true);
        canvas.drawCircle(x, y, 40, accuracyPaint);


        canvas.drawCircle(x, y, 25, userLocationPaint);


        Paint innerPaint = new Paint();
        innerPaint.setColor(Color.WHITE);
        innerPaint.setAntiAlias(true);
        canvas.drawCircle(x, y, 15, innerPaint);


        Paint centerPaint = new Paint();
        centerPaint.setColor(Color.parseColor("#1565C0"));
        centerPaint.setAntiAlias(true);
        canvas.drawCircle(x, y, 8, centerPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && clickListener != null) {
            float touchX = event.getX() / getWidth();
            float touchY = event.getY() / getHeight();


            MapPoint closest = null;
            float minDistance = Float.MAX_VALUE;

            for (MapPoint point : mapPoints) {
                if (!point.isUserLocation) {
                    float distance = (float) Math.sqrt(
                            Math.pow(point.x - touchX, 2) + Math.pow(point.y - touchY, 2)
                    );
                    if (distance < 0.05f && distance < minDistance) {
                        minDistance = distance;
                        closest = point;
                    }
                }
            }

            if (closest != null) {
                clickListener.onMapPointClick(closest);
                return true;
            }
        }
        return super.onTouchEvent(event);
    }
}


