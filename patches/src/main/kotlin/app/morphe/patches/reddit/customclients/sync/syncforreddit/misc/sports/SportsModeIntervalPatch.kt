package app.morphe.patches.reddit.customclients.sync.syncforreddit.misc.sports

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patches.all.misc.resources.addResourcesPatch
import app.morphe.patches.reddit.customclients.AppCompatibility
import app.morphe.patches.reddit.customclients.ExtensionPatches
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patches.reddit.customclients.sync.syncforreddit.misc.settings.syncMorpheSettingsPatch
import app.morphe.util.ResourceGroup
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

private val sportsIntervalResources = resourcePatch(default = false) {
    dependsOn(syncMorpheSettingsPatch, addResourcesPatch("syncforreddit",
        ResourceGroup("xml", "morphe_sync_sports.xml")))
    execute {
        document("res/xml/morphe_settings.xml").use { target ->
            document("res/xml/morphe_sync_sports.xml").use { source ->
                val entry = source.getElementsByTagName("ListPreference").item(0)
                target.documentElement.appendChild(target.importNode(entry, true))
            }
        }
    }
}

@Suppress("unused")
val sportsModeIntervalPatch = bytecodePatch(
    name = "Sports mode interval",
    description = "Choose a 5, 10, or 15 second comments refresh countdown in Morphe settings.",
) {
    dependsOn(ExtensionPatches.Sync, sportsIntervalResources)
    compatibleWith(*AppCompatibility.SyncForReddit)
    dependsOn(addResourcesPatch("syncforreddit", ResourceGroup("values", "morphe_sync_sports.xml"), ResourceGroup("drawable", "morphe_sports_settings.xml")))

    execute {
        SportsCountdownFingerprint.method.apply {
            val index = SportsCountdownFingerprint.instructionMatches.single().index
            val register = getInstruction<OneRegisterInstruction>(index).registerA
            addInstructions(index + 1, """
                invoke-static {v$register}, Lapp/morphe/extension/syncforreddit/SportsModeInterval;->getInterval(I)I
                move-result v$register
            """)
            addInstructions(0, """
                invoke-static/range {p0 .. p0}, Lapp/morphe/extension/syncforreddit/SportsModeInterval;->attachSettings(Landroid/view/View;)V
            """)
        }
    }
}
