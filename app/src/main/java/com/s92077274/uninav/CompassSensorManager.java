package com.s92077274.uninav;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

/**
 * Manages accelerometer and magnetometer sensors to provide device heading (azimuth).
 * Handles the rotation animation of a specified ImageView to act as a compass.
 */
public class CompassSensorManager implements SensorEventListener {

    private static final String TAG = "CompassSensorManager";
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;

    private float[] gravityValues;      // Accelerometer readings
    private float[] geomagneticValues;  // Magnetometer readings
    private float lastAzimuthDegrees = 0f; // Last reported azimuth for smooth animation

    private ImageView compassImageView; // ImageView to rotate as a compass
    private OnHeadingChangeListener headingListener; // Optional callback for raw heading data

    /**
     * Interface for listeners to receive device heading (azimuth) changes in degrees.
     */
    public interface OnHeadingChangeListener {
        void onHeadingChanged(float degrees);
    }

    /**
     * Constructor for CompassSensorManager.
     * @param context The application context.
     * @param imageView The ImageView to be rotated as a compass.
     */
    public CompassSensorManager(Context context, ImageView imageView) {
        this.compassImageView = imageView;
        initSensors(context);
    }

    /**
     * Constructor for CompassSensorManager with an optional custom heading listener.
     * @param context The application context.
     * @param imageView The ImageView to be rotated as a compass.
     * @param listener An optional listener to receive raw heading data.
     */
    public CompassSensorManager(Context context, ImageView imageView, OnHeadingChangeListener listener) {
        this(context, imageView); // Calls the primary constructor
        this.headingListener = listener;
    }

    /**
     * Initializes the SensorManager and retrieves accelerometer and magnetometer sensors.
     * Logs warnings if essential sensors are not found.
     * @param context The application context.
     */
    private void initSensors(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            if (accelerometer == null || magnetometer == null) {
                Log.w(TAG, "Accelerometer or Magnetometer not available. Compass functionality may be limited.");
            } else {
                Log.d(TAG, "Accelerometer and Magnetometer sensors initialized.");
            }
        } else {
            Log.e(TAG, "SensorManager not found. Device sensors may not be available.");
        }
    }

    /**
     * Starts listening for sensor updates. Call in onResume() to activate sensors.
     */
    public void start() {
        if (sensorManager != null && accelerometer != null && magnetometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
            Log.d(TAG, "Sensor listeners registered.");
        } else {
            Log.w(TAG, "Cannot start sensors: SensorManager or required sensors are null.");
        }
    }

    /**
     * Stops listening for sensor updates. Call in onPause() to conserve battery.
     */
    public void stop() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
            Log.d(TAG, "Sensor listeners unregistered.");
        }
    }

    /**
     * Called when sensor values change.
     * Processes accelerometer and magnetometer data to calculate azimuth and rotates
     * the compass ImageView. Notifies listeners if registered.
     * @param event The SensorEvent containing new sensor data.
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            gravityValues = event.values.clone();
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagneticValues = event.values.clone();
        }

        if (gravityValues != null && geomagneticValues != null) {
            float[] rotationMatrix = new float[9];
            if (SensorManager.getRotationMatrix(rotationMatrix, null, gravityValues, geomagneticValues)) {
                float[] orientation = new float[3];
                SensorManager.getOrientation(rotationMatrix, orientation);

                float azimuthInDeg = (float) Math.toDegrees(orientation[0]);
                azimuthInDeg = (azimuthInDeg + 360) % 360; // Normalize to 0-360 degrees

                // Rotate ImageView for compass animation
                if (compassImageView != null) {
                    RotateAnimation ra = new RotateAnimation(
                            lastAzimuthDegrees, // Start angle
                            -azimuthInDeg,      // End angle (negative for compass rotation)
                            RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                            RotateAnimation.RELATIVE_TO_SELF, 0.5f);
                    ra.setDuration(250); // Animation duration
                    ra.setFillAfter(true); // Retain new orientation
                    compassImageView.startAnimation(ra);
                }
                lastAzimuthDegrees = -azimuthInDeg; // Update for next animation

                // Notify listener with positive heading
                if (headingListener != null) {
                    headingListener.onHeadingChanged(azimuthInDeg);
                }
            }
        }
    }

    /**
     * Called when the accuracy of a sensor changes.
     * Currently, this method only logs the change.
     * @param sensor The sensor whose accuracy has changed.
     * @param accuracy The new accuracy of the sensor.
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(TAG, "Accuracy changed for " + sensor.getName() + ": " + accuracy);
    }

    /**
     * Returns the last calculated azimuth (heading) in degrees, normalized to 0-360.
     * @return The last known heading in degrees (0-360).
     */
    public float getLastHeadingDegrees() {
        return (lastAzimuthDegrees + 360) % 360; // Ensure positive degrees
    }
}
