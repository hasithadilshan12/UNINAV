package com.s92077274.uninav.views;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.s92077274.uninav.models.MapPoint;
import java.util.ArrayList;
import java.util.List;

public class MapOverlayView extends View {
    private List<MapPoint> mapPoints = new ArrayList<>();
    private MapPoint userLocation;
    private Paint markerPaint, routePaint, userLocationPaint, textPaint, textBackgroundPaint;
    private OnMapPointClickListener clickListener;
    private Matrix imageMatrix = new Matrix();
    private RectF imageRect = new RectF();

    // Interface for map point click events
    public interface OnMapPointClickListener {
        void onMapPointClick(MapPoint point);
    }

    // Constructor for the custom view
    public MapOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaints();
    }

    // Initializes paint objects for drawing
    private void initPaints() {
        // Paint for markers
        markerPaint = new Paint();
        markerPaint.setColor(Color.RED);
        markerPaint.setStyle(Paint.Style.FILL);
        markerPaint.setAntiAlias(true);

        // Paint for drawing routes
        routePaint = new Paint();
        routePaint.setColor(Color.parseColor("#1565C0"));
        routePaint.setStrokeWidth(8);
        routePaint.setStyle(Paint.Style.STROKE);
        routePaint.setAntiAlias(true);

        // Paint for user location marker
        userLocationPaint = new Paint();
        userLocationPaint.setColor(Color.parseColor("#4CAF50"));
        userLocationPaint.setStyle(Paint.Style.FILL);
        userLocationPaint.setAntiAlias(true);

        // Paint for text labels
        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(32);
        textPaint.setAntiAlias(true);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);

        // Paint for text label background
        textBackgroundPaint = new Paint();
        textBackgroundPaint.setColor(Color.parseColor("#AA000000")); // Semi-transparent black
        textBackgroundPaint.setStyle(Paint.Style.FILL);
        textBackgroundPaint.setAntiAlias(true);
    }

    // Sets the list of map points to display
    public void setMapPoints(List<MapPoint> points) {
        this.mapPoints = points;
        invalidate(); // Redraw view
    }

    // Sets the user's current location
    public void setUserLocation(MapPoint location) {
        this.userLocation = location;
        invalidate(); // Redraw view
    }

    // Removed as NavigationRoute is no longer used
    // public void setRoute(NavigationRoute route) {
    //     this.currentRoute = route;
    //     invalidate(); // Redraw view
    // }

    // Sets the click listener for map points
    public void setOnMapPointClickListener(OnMapPointClickListener listener) {
        this.clickListener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Removed route drawing logic as currentRoute is no longer used
        // if (currentRoute != null && currentRoute.waypoints.size() > 1) {
        //     drawRoute(canvas);
        // }

        // Draw markers for all map points (excluding user location)
        for (MapPoint point : mapPoints) {
            if (!point.isUserLocation) {
                drawLocationMarker(canvas, point);
            }
        }

        // Draw the user's current location marker
        if (userLocation != null) {
            drawUserLocation(canvas, userLocation);
        }
    }

    private void drawLocationMarker(Canvas canvas, MapPoint point) {
        float x = point.x * getWidth();
        float y = point.y * getHeight();

        // Outer circle of the marker
        canvas.drawCircle(x, y, 20, markerPaint);

        // Inner circle of the marker
        Paint innerPaint = new Paint();
        innerPaint.setColor(Color.WHITE);
        innerPaint.setAntiAlias(true);
        canvas.drawCircle(x, y, 12, innerPaint);

        // Draw text label and its background
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

    // Draws the user's location marker with accuracy circle
    private void drawUserLocation(Canvas canvas, MapPoint point) {
        float x = point.x * getWidth();
        float y = point.y * getHeight();

        // Draw accuracy circle
        Paint accuracyPaint = new Paint();
        accuracyPaint.setColor(Color.parseColor("#334CAF50")); // Semi-transparent green
        accuracyPaint.setAntiAlias(true);
        canvas.drawCircle(x, y, 40, accuracyPaint);

        // Outer circle of user marker
        canvas.drawCircle(x, y, 25, userLocationPaint);

        // Middle circle of user marker
        Paint innerPaint = new Paint();
        innerPaint.setColor(Color.WHITE);
        innerPaint.setAntiAlias(true);
        canvas.drawCircle(x, y, 15, innerPaint);

        // Center dot of user marker
        Paint centerPaint = new Paint();
        centerPaint.setColor(Color.parseColor("#1565C0")); // Blue color
        centerPaint.setAntiAlias(true);
        canvas.drawCircle(x, y, 8, centerPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Handle touch events for clicking on map points
        if (event.getAction() == MotionEvent.ACTION_DOWN && clickListener != null) {
            float touchX = event.getX() / getWidth();
            float touchY = event.getY() / getHeight();

            MapPoint closest = null;
            float minDistance = Float.MAX_VALUE;

            // Find the closest clickable map point to the touch location
            for (MapPoint point : mapPoints) {
                if (!point.isUserLocation) {
                    float distance = (float) Math.sqrt(
                            Math.pow(point.x - touchX, 2) + Math.pow(point.y - touchY, 2)
                    );
                    // Check if touch is within a certain radius of the marker
                    if (distance < 0.05f && distance < minDistance) { // 0.05f is a normalized radius
                        minDistance = distance;
                        closest = point;
                    }
                }
            }

            // If a map point was clicked, notify the listener
            if (closest != null) {
                clickListener.onMapPointClick(closest);
                return true; // Event consumed
            }
        }
        return super.onTouchEvent(event); // Pass event to superclass if not consumed
    }
}
