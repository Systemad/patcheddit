package app.morphe.patches.reddit.customclients.sync.syncforreddit.misc.sports

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import com.android.tools.smali.dexlib2.Opcode

internal object SportsCountdownFingerprint : Fingerprint(
    definingClass = "/SportsButton;",
    parameters = emptyList(),
    returnType = "V",
    filters = listOf(fieldAccess(
        definingClass = "Lcom/laurencedawson/reddit_sync/singleton/SettingsSingleton\$Settings;",
        name = "commentAutoRefreshValue",
        opcode = Opcode.IGET,
    )),
)
