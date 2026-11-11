package app.morphe.extension.syncforreddit;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import app.morphe.extension.shared.Utils;
import app.morphe.extension.shared.settings.PatchSettings;

public final class SportsModeInterval {
    private static final String INTERVAL_KEY = "morphe_sync_sports_interval";
    private static final String BUTTON_TAG = "morphe_sports_settings";

    private SportsModeInterval() {}

    /** Called when a sports countdown starts; the native view owns the button lifecycle. */
    public static void attachSettings(View sportsView) {
        try {
            attachSettingsButton(sportsView);
        } catch (Exception exception) {
            android.util.Log.e("SportsModeInterval", "attachSettings failure", exception);
        }
    }

    private static void attachSettingsButton(View sportsView) {
        Context context = sportsView.getContext();
        int wrapperId = context.getResources().getIdentifier("sports_wrapper", "id", context.getPackageName());
        int refreshId = context.getResources().getIdentifier("sports_refresh", "id", context.getPackageName());
        View wrapperView = sportsView.findViewById(wrapperId);
        if (!(wrapperView instanceof RelativeLayout)) return;
        RelativeLayout wrapper = (RelativeLayout) wrapperView;
        View refresh = wrapper.findViewById(refreshId);
        if (refresh == null || wrapper.findViewWithTag(BUTTON_TAG) != null) return;

        ImageButton settings = new ImageButton(context);
        settings.setId(View.generateViewId());
        settings.setTag(BUTTON_TAG);
        settings.setContentDescription("Sports mode interval");
        int icon = context.getResources().getIdentifier("morphe_sports_settings", "drawable", context.getPackageName());
        settings.setImageResource(icon);
        settings.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        int size = Math.round(48 * context.getResources().getDisplayMetrics().density);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(size, size);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        wrapper.addView(settings, params);

        RelativeLayout.LayoutParams refreshParams = (RelativeLayout.LayoutParams) refresh.getLayoutParams();
        refreshParams.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        refreshParams.addRule(RelativeLayout.LEFT_OF, settings.getId());
        refresh.setLayoutParams(refreshParams);
        settings.setOnClickListener(view -> showIntervalDialog(context));
    }

    private static void showIntervalDialog(Context context) {
        int labelsId = context.getResources().getIdentifier("morphe_sync_sports_labels", "array", context.getPackageName());
        int valuesId = context.getResources().getIdentifier("morphe_sync_sports_values", "array", context.getPackageName());
        String[] labels = context.getResources().getStringArray(labelsId);
        String[] values = context.getResources().getStringArray(valuesId);
        SharedPreferences preferences = PatchSettings.preferences(context);
        String current = preferences.getString(INTERVAL_KEY, "15");
        int selected = 2;
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(current)) selected = i;
        }
        new AlertDialog.Builder(context)
                .setTitle("Sports mode interval")
                .setSingleChoiceItems(labels, selected, (dialog, which) -> {
                    preferences.edit().putString(INTERVAL_KEY, values[which]).apply();
                    dialog.dismiss();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /** Read on each countdown so changes also apply to an already open thread. */
    public static int getInterval(int original) {
        if (!Utils.isContextSet()) return original;
        String value = PatchSettings.preferences(Utils.getContext())
                .getString(INTERVAL_KEY, "15");
        if (value == null) return 15;
        switch (value) {
            case "5":
                return 5;
            case "10":
                return 10;
            default:
                return 15;
        }
    }
}
