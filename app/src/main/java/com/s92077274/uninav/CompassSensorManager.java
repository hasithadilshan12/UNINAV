package com.s92077274.uninav; // Or com.s92077274.uninav.utils; if you create a new package

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.animation.RotateAnimation;
import android.widget.ImageView; // Added for direct image manipulation

/**
 * Manages accelerometer and magnetometer sensors to provide device heading (azimuth).
 * It also handles the rotation animation of an ImageView to act as a compass.
 */
public class CompassSensorManager implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;

    private float[] gravityValues;
    private float[] geomagneticValues;
    private float lastAzimuthDegrees = 0f; // Store the last reported azimuth for animation

    private ImageView compassImageView; // The ImageView to rotate
    private OnHeadingChangeListener headingListener; // Optional: Callback for raw heading data

    /**
     * Interface for listeners who want to receive raw heading (azimuth) changes.
     */
    public interface OnHeadingChangeListener {
        void onHeadingChanged(float degrees);
    }

    /**
     * Constructor for CompassSensorManager.
     * @param context The application context.
     * @param imageView The ImageView that will be rotated to act as a compass.
     */
    public CompassSensorManager(Context context, ImageView imageView) {
        this.compassImageView = imageView;
        initSensors(context);
    }

    /**
     * Constructor for CompassSensorManager with a custom heading listener.
     * @param context The application context.
     * @param imageView The ImageView that will be rotated to act as a compass.
     * @param listener An optional listener to receive raw heading data.
     */
    public CompassSensorManager(Context context, ImageView imageView, OnHeadingChangeListener listener) {
        this(context, imageView); // Call the other constructor
        this.headingListener = listener;
    }

    // Initializes sensor manager and retrieves accelerometer and magnetometer
    private void initSensors(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            if (accelerometer == null || magnetometer == null) {
                Log.w("CompassSensorManager", "Accelerometer or Magnetometer not available. Compass may not function.");
            } else {
                Log.d("CompassSensorManager", "Accelerometer and Magnetometer sensors initialized.");
            }
        } else {
            Log.e("CompassSensorManager", "SensorManager not found. Device sensors may not be available.");
        }
    }

    /**
     * Starts listening for sensor updates. Should be called in onResume().
     */
    public void start() {
        if (sensorManager != null && accelerometer != null && magnetometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
            Log.d("CompassSensorManager", "Sensor listeners registered.");
        } else {
            Log.w("CompassSensorManager", "Cannot start sensors: SensorManager or required sensors are null.");
        }
    }

    /**
     * Stops listening for sensor updates. Should be called in onPause() to save battery.
     */
    public void stop() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
            Log.d("CompassSensorManager", "Sensor listeners unregistered.");
        }
    }

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
            // No need for inclinationMatrix if only azimuth is required
            if (SensorManager.getRotationMatrix(rotationMatrix, null, gravityValues, geomagneticValues)) {
                float[] orientation = new float[3];
                SensorManager.getOrientation(rotationMatrix, orientation);

                float azimuthInRad = orientation[0]; // Azimuth in radians
                float azimuthInDeg = (float) Math.toDegrees(azimuthInRad);
                azimuthInDeg = (azimuthInDeg + 360) % 360; // Normalize to 0-360 degrees

                // Rotate the ImageView to act as a compass
                if (compassImageView != null) {
                    RotateAnimation ra = new RotateAnimation(
                            lastAzimuthDegrees, // from degree
                            -azimuthInDeg,      // to degree (negative because compass rotates opposite to map bearing)
                            RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                            RotateAnimation.RELATIVE_TO_SELF, 0.5f);
                    ra.setDuration(250); // Animation duration
                    ra.setFillAfter(true); // Keep the new orientation
                    compassImageView.startAnimation(ra);
                }
                lastAzimuthDegrees = -azimuthInDeg; // Update for the next animation cycle

                // Notify the listener if one is set
                if (headingListener != null) {
                    headingListener.onHeadingChanged(azimuthInDeg); // Pass positive degrees
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Optional: Implement logic here if you need to react to sensor accuracy changes
        Log.d("CompassSensorManager", "Accuracy changed for " + sensor.getName() + ": " + accuracy);
    }

    /**
     * Returns the last calculated azimuth in degrees (0-360).
     * @return The last known heading in degrees.
     */
    public float getLastHeadingDegrees() {
        return (lastAzimuthDegrees + 360) % 360; // Return positive degrees
    }
}

