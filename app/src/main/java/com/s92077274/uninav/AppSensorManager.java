package com.s92077274.uninav;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import java.util.Locale;//Helps format strings
/**
 * Manages accelerometer, magnetometer, and light sensors to provide device heading (azimuth)
 * and ambient light readings with advice. Handles the rotation animation of a specified ImageView
 * as a compass and notifies listeners of light level changes.
 */
public class AppSensorManager implements SensorEventListener {

    private static final String TAG = "AppSensorManager";
    private android.hardware.SensorManager androidSensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private Sensor lightSensor;

    private float[] gravityValues;
    private float[] geomagneticValues;
    private float lastAzimuthDegrees = 0f;
    private float lastLightLux = -1f;

    private ImageView compassImageView;
    private OnHeadingChangeListener headingListener;
    private OnLightChangeListener lightListener;

    private boolean isLightSensorAvailable = false;
    private boolean isCompassSensorsAvailable = false;


     //Interface for listeners to receive device heading (azimuth) changes in degrees.

    public interface OnHeadingChangeListener {
        void onHeadingChanged(float degrees);
    }

    //Interface for listeners to receive ambient light level changes in lux, along with advice.

    public interface OnLightChangeListener {
        void onLightChanged(float lux, String advice);
    }

    /**
     * Constructor for AppSensorManager focusing on compass functionality.
     * @param context The application context.
     * @param compassImageView The ImageView to be rotated as a compass.
     */
    public AppSensorManager(Context context, ImageView compassImageView) {
        this.compassImageView = compassImageView;
        initSensors(context);
    }

    /**
     * Constructor for AppSensorManager with an optional custom heading listener.
     * @param context The application context.
     * @param compassImageView The ImageView to be rotated as a compass.
     * @param headingListener An optional listener to receive raw heading data.
     */
    public AppSensorManager(Context context, ImageView compassImageView, OnHeadingChangeListener headingListener) {
        this(context, compassImageView);
        this.headingListener = headingListener;
    }

    /**
     * Constructor for AppSensorManager with an optional light change listener.
     * @param context The application context.
     * @param lightListener An optional listener to receive light data.
     */
    public AppSensorManager(Context context, OnLightChangeListener lightListener) {
        this.lightListener = lightListener;
        initSensors(context);
    }

    /**
     * Constructor for AppSensorManager with both heading and light change listeners.
     * @param context The application context.
     * @param compassImageView The ImageView to be rotated as a compass. (Can be null if only light sensor is needed)
     * @param headingListener An optional listener to receive raw heading data.
     * @param lightListener An optional listener to receive light data.
     */
    public AppSensorManager(Context context, ImageView compassImageView, OnHeadingChangeListener headingListener, OnLightChangeListener lightListener) {
        this(context, compassImageView, headingListener);
        this.lightListener = lightListener;
    }

    /**
     * Initializes the Android SensorManager and retrieves accelerometer, magnetometer, and light sensors.
     * Logs warnings if essential sensors are not found.
     * @param context The application context.
     */
    private void initSensors(Context context) {
        androidSensorManager = (android.hardware.SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (androidSensorManager != null) {
            accelerometer = androidSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            magnetometer = androidSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            lightSensor = androidSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

            // Check compass sensors availability
            isCompassSensorsAvailable = (accelerometer != null && magnetometer != null);
            if (!isCompassSensorsAvailable) {
                Log.w(TAG, "Accelerometer or Magnetometer not available. Compass functionality disabled.");
            } else {
                Log.d(TAG, "Accelerometer and Magnetometer sensors initialized.");
            }

            // Check light sensor availability
            isLightSensorAvailable = (lightSensor != null);
            if (!isLightSensorAvailable) {
                Log.w(TAG, "Light Sensor not available on this device.");
            } else {
                Log.d(TAG, "Light Sensor initialized.");
            }
        } else {
            Log.e(TAG, "Android SensorManager not found. Device sensors may not be available.");
        }
    }

    /**
     * Starts listening for sensor updates from all managed sensors.
     * Call in onResume() to activate sensors.
     */
    public void start() {
        if (androidSensorManager != null) {
            if (isCompassSensorsAvailable && accelerometer != null && magnetometer != null) {
                androidSensorManager.registerListener(this, accelerometer, android.hardware.SensorManager.SENSOR_DELAY_UI);
                androidSensorManager.registerListener(this, magnetometer, android.hardware.SensorManager.SENSOR_DELAY_UI);
                Log.d(TAG, "Compass sensor listeners registered.");
            }

            if (isLightSensorAvailable && lightSensor != null) {
                androidSensorManager.registerListener(this, lightSensor, android.hardware.SensorManager.SENSOR_DELAY_NORMAL);
                Log.d(TAG, "Light sensor listener registered.");
            }
        } else {
            Log.w(TAG, "Cannot start sensors: Android SensorManager is null.");
        }
    }

    /**
     * Stops listening for sensor updates from all managed sensors.
     * Call in onPause() to conserve battery.
     */
    public void stop() {
        if (androidSensorManager != null) {
            androidSensorManager.unregisterListener(this);
            Log.d(TAG, "All sensor listeners unregistered.");
        }
    }

    /**
     * Called when sensor values change.
     * Processes accelerometer and magnetometer data for azimuth calculation and compass rotation.
     * Processes light sensor data, generates advice, and notifies registered listeners.
     * @param event The SensorEvent containing new sensor data.
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        // Handle Accelerometer and Magnetometer for Compass
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            gravityValues = event.values.clone();
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagneticValues = event.values.clone();
        } else if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            float currentLightLux = event.values[0];
            // Only notify if light value has changed significantly to avoid spamming.
            // Using a threshold of 5 lux for significant change.
            if (lightListener != null && Math.abs(currentLightLux - lastLightLux) > 5) {
                String advice = getLightAdvice(currentLightLux);
                lightListener.onLightChanged(currentLightLux, advice);
                lastLightLux = currentLightLux;
            }
            return; // Exit early for light sensor, as it doesn't affect compass rotation
        }

        // Process compass data only if both accelerometer and magnetometer values are available
        if (gravityValues != null && geomagneticValues != null) {
            float[] rotationMatrix = new float[9];
            if (android.hardware.SensorManager.getRotationMatrix(rotationMatrix, null, gravityValues, geomagneticValues)) {
                float[] orientation = new float[3];
                android.hardware.SensorManager.getOrientation(rotationMatrix, orientation);

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

                // Notify heading listener
                if (headingListener != null) {
                    headingListener.onHeadingChanged(azimuthInDeg);
                }
            }
        }
    }

    /**
     * Generates a textual advice string based on the current ambient light level.
     * @param lux The current light level in lux.
     * @return A string containing advice related to the light level.
     */
    private String getLightAdvice(float lux) {
        if (lux < 10) {
            return "Too dark — increase screen brightness for visibility.";
        } else if (lux < 50) {
            return "Low light — slightly increase brightness.";
        } else if (lux < 200) {
            return "Comfortable lighting — keep current brightness.";
        } else if (lux < 1000) {
            return "Bright environment — reduce brightness to save battery.";
        } else {
            return "Very bright — set brightness to maximum for clear visibility.";
        }
    }

    /**
     * Called when the accuracy of a sensor changes.
     * This method logs the change.
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

    /**
     * Checks if the light sensor is available on this device.
     * @return true if light sensor is available, false otherwise.
     */
    public boolean isLightSensorAvailable() {
        return isLightSensorAvailable;
    }

    /**
     * Checks if the compass sensors (accelerometer and magnetometer) are available on this device.
     * @return true if compass sensors are available, false otherwise.
     */
    public boolean isCompassSensorsAvailable() {
        return isCompassSensorsAvailable;
    }
}