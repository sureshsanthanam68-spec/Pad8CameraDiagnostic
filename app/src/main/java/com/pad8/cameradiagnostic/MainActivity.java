
package com.pad8.cameradiagnostic;

import android.app.Activity;
import android.os.Bundle;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraCharacteristics;
import android.content.Context;
import android.graphics.Rect;
import android.util.Range;
import android.util.SizeF;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.Button;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;

import java.util.Arrays;

public class MainActivity extends Activity {

    private String report = "";

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(24, 24, 24, 24);

        Button scan = new Button(this);
        scan.setText("SCAN CAMERA HARDWARE");

        Button share = new Button(this);
        share.setText("SHARE REPORT");

        TextView output = new TextView(this);
        output.setTextSize(15);
        output.setTypeface(Typeface.MONOSPACE);
        output.setTextIsSelectable(true);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(output);

        layout.addView(scan);
        layout.addView(share);
        layout.addView(scroll, new LinearLayout.LayoutParams(
            -1, 0, 1
        ));

        setContentView(layout);

        scan.setOnClickListener(v -> {
            report = scanCameras();
            output.setText(report);
        });

        share.setOnClickListener(v -> {
            if (report.isEmpty()) {
                report = scanCameras();
                output.setText(report);
            }

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, report);
            startActivity(Intent.createChooser(
                intent, "Share Camera Report"
            ));
        });

        report = scanCameras();
        output.setText(report);
    }

    private String scanCameras() {
        StringBuilder sb = new StringBuilder();

        sb.append("PAD 8 CAMERA2 DIAGNOSTIC\n");
        sb.append("========================\n");
        sb.append("Device: ").append(Build.MANUFACTURER)
          .append(" ").append(Build.MODEL).append("\n");
        sb.append("Android: ").append(Build.VERSION.RELEASE)
          .append("\n\n");

        try {
            CameraManager manager =
                (CameraManager) getSystemService(
                    Context.CAMERA_SERVICE
                );

            for (String id : manager.getCameraIdList()) {
                CameraCharacteristics c =
                    manager.getCameraCharacteristics(id);

                sb.append("CAMERA ID: ").append(id)
                  .append("\n");

                Integer facing = c.get(
                    CameraCharacteristics.LENS_FACING
                );
                sb.append("Facing: ")
                  .append(facing != null &&
                          facing == CameraCharacteristics.LENS_FACING_BACK
                          ? "BACK" : facing != null &&
                          facing == CameraCharacteristics.LENS_FACING_FRONT
                          ? "FRONT" : "OTHER")
                  .append("\n");

                Range<Float> zoom = c.get(
                    CameraCharacteristics.CONTROL_ZOOM_RATIO_RANGE
                );
                sb.append("ZOOM RATIO RANGE: ")
                  .append(zoom == null ? "NOT REPORTED" : zoom)
                  .append("\n");

                Float maxZoom = c.get(
                    CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM
                );
                sb.append("MAX DIGITAL ZOOM: ")
                  .append(maxZoom).append("\n");

                Rect active = c.get(
                    CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE
                );
                sb.append("ACTIVE ARRAY: ")
                  .append(active).append("\n");

                Rect preCorrection = c.get(
                    CameraCharacteristics.SENSOR_INFO_PRE_CORRECTION_ACTIVE_ARRAY_SIZE
                );
                sb.append("PRE-CORRECTION ARRAY: ")
                  .append(preCorrection).append("\n");

                float[] focal = c.get(
                    CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS
                );
                sb.append("FOCAL LENGTHS (mm): ")
                  .append(focal == null ? "NOT REPORTED" :
                          Arrays.toString(focal))
                  .append("\n");

                SizeF sensor = c.get(
                    CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE
                );
                sb.append("SENSOR SIZE (mm): ")
                  .append(sensor).append("\n");

                int[] distortion = c.get(
                    CameraCharacteristics.DISTORTION_CORRECTION_AVAILABLE_MODES
                );
                sb.append("DISTORTION MODES: ")
                  .append(distortion == null ? "NOT REPORTED" :
                          Arrays.toString(distortion))
                  .append("\n");

                int[] stabilization = c.get(
                    CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES
                );
                sb.append("VIDEO STABILIZATION MODES: ")
                  .append(stabilization == null ? "NOT REPORTED" :
                          Arrays.toString(stabilization))
                  .append("\n");

                sb.append("------------------------\n\n");
            }

        } catch (Exception e) {
            sb.append("ERROR: ").append(e.toString());
        }

        return sb.toString();
    }
}
