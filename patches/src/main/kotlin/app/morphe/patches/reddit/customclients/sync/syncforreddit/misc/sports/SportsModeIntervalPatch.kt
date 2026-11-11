package app.morphe.patches.reddit.customclients.sync.syncforreddit.misc.sports

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patches.all.misc.resources.addResourcesPatch
import app.morphe.patches.reddit.customclients.AppCompatibility
import app.morphe.patches.reddit.customclients.ExtensionPatches
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patches.reddit.customclients.sync.syncforreddit.misc.settings.syncMorpheSettingsPatch
import app.morphe.util.ResourceGroup
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

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
    dependsOn(addResourcesPatch("syncforreddit", ResourceGroup("values", "morphe_sync_sports.xml")))

    execute {
        SportsCountdownFingerprint.method.apply {
            val matches = implementation!!.instructions.withIndex().filter { (_, instruction) ->
                val field = (instruction as? ReferenceInstruction)?.reference as? FieldReference
                field?.name == "commentAutoRefreshValue" && field.type == "I"
            }
            require(matches.size == 1) { "Expected one sports countdown interval read." }
            val (index, instruction) = matches.single()
            val register = (instruction as OneRegisterInstruction).registerA
            addInstructions(index + 1, """
                invoke-static {v$register}, Lapp/morphe/extension/syncforreddit/SportsModeInterval;->getInterval(I)I
                move-result v$register
            """)
        }
    }
}
