package app.morphe.patches.reddit.customclients.sync.syncforreddit.misc.sports

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patches.all.misc.resources.addResourcesPatch
import app.morphe.patches.reddit.customclients.AppCompatibility
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.patch.ResourcePatchContext
import app.morphe.util.ResourceGroup
import app.morphe.util.copyXmlNode
import app.morphe.util.inputStreamFromBundledResource
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

private val sportsIntervalResources = resourcePatch(default = false) {
    dependsOn(addResourcesPatch("syncforreddit",
        ResourceGroup("xml", "morphe_sync_sports.xml")))
    execute {
        mergeSportsResources("strings.xml")
        mergeSportsResources("arrays.xml")
    }
    finalize {
        require(this["res/xml/morphe_settings.xml"].isFile) {
            "Include original Patcheddit's Sync Morphe settings patch alongside Sports mode interval."
        }
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
    description = "Adds 5, 10, or 15 second Sports mode intervals. Requires original Patcheddit's Sync Morphe settings patch.",
) {
    dependsOn(sportsIntervalResources)
    extendWith("extensions/sports.mpe")
    compatibleWith(*AppCompatibility.SyncForReddit)
    dependsOn(addResourcesPatch("syncforreddit", ResourceGroup("drawable", "morphe_sports_settings.xml")))

    execute {
        SportsCountdownFingerprint.method.apply {
            val index = SportsCountdownFingerprint.instructionMatches.single().index
            val register = getInstruction<OneRegisterInstruction>(index).registerA
            addInstructions(index + 1, """
                invoke-static {v$register}, Lapp/systemad/sports/SportsModeInterval;->getInterval(I)I
                move-result v$register
            """)
            addInstructions(0, """
                invoke-static/range {p0 .. p0}, Lapp/systemad/sports/SportsModeInterval;->attachSettings(Landroid/view/View;)V
            """)
        }
    }
}

private fun ResourcePatchContext.mergeSportsResources(fileName: String) {
    val input = checkNotNull(inputStreamFromBundledResource("sports", "values/$fileName")) {
        "Sports mode resource file was not bundled: $fileName"
    }
    input.use {
        "resources".copyXmlNode(document(it), document("res/values/$fileName")).use { }
    }
}
