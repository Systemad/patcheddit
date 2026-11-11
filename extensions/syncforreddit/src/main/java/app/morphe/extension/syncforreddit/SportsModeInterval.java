package app.morphe.extension.syncforreddit;

import app.morphe.extension.shared.Utils;
import app.morphe.extension.shared.settings.PatchSettings;

public final class SportsModeInterval {
    private SportsModeInterval() {}

    /** Read on each countdown so changes also apply to an already open thread. */
    public static int getInterval(int original) {
        if (!Utils.isContextSet()) return original;
        String value = PatchSettings.preferences(Utils.getContext())
                .getString("morphe_sync_sports_interval", "15");
        if ("5".equals(value)) return 5;
        if ("10".equals(value)) return 10;
        return 15;
    }
}
