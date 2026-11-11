package app.morphe.patches.reddit.customclients.sync.syncforreddit.misc.sports

import app.morphe.patcher.Fingerprint

internal object SportsCountdownFingerprint : Fingerprint(
    definingClass = "/SportsButton;",
    name = "h",
    parameters = emptyList(),
    returnType = "V",
)
