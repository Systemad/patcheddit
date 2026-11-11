group = "app.morphe"

val httpLibraryResources by configurations.creating

patches {
    about {
        name = "Systemad Sync Add-ons"
        description = "Sync add-ons for use alongside original Patcheddit"
        source = "git@github.com:Systemad/patcheddit.git"
        author = "wchill"
        contact = "w@chill.dev"
        website = "https://github.com/Systemad/patcheddit"
        license = "Additional conditions under GPL section 7 apply: attribution and project name restrictions. See LICENSE file."
    }
}

dependencies {
    httpLibraryResources(libs.okhttp.transport) {
        isTransitive = false
    }
    // Used by JsonGenerator.
    implementation(libs.gson)

    // Required due to smali, or build fails. Can be removed once smali is bumped.
    implementation(libs.guava)

    implementation(libs.morphe.patches.library)

    // Android API stubs defined here.
    compileOnly(project(":patches:stub"))

    testImplementation(kotlin("test-junit"))
}

val prepareHttpLibraryResources by tasks.registering(Sync::class) {
    from({ httpLibraryResources.map { zipTree(it) } }) {
        include("okhttp3/internal/publicsuffix/PublicSuffixDatabase.gz")
        include("okhttp3/internal/publicsuffix/NOTICE")
        into("http")
    }
    into(layout.buildDirectory.dir("generated/http-resources"))
    doLast {
        check(destinationDir.resolve("http/okhttp3/internal/publicsuffix/PublicSuffixDatabase.gz").isFile) {
            "OkHttp public suffix database was not found"
        }
    }
}

sourceSets.main {
    resources.srcDir(prepareHttpLibraryResources)
}

tasks {
    test {
        useJUnit()
        val boostApk = providers.environmentVariable("BOOST_APK").orNull
        inputs.property("boostApk", boostApk.orEmpty())
        if (boostApk != null) {
            val apk = rootProject.file(boostApk)
            inputs.file(apk).withPropertyName("boostApkFixture")
            environment("BOOST_APK", apk.absolutePath)
        }
    }

    register<JavaExec>("checkStringResources") {
        description = "Checks resource strings for invalid formatting"

        dependsOn(compileKotlin)

        classpath = sourceSets["main"].runtimeClasspath
        mainClass.set("app.morphe.util.resource.CheckStringResourcesKt")
    }

    register<JavaExec>("generatePatchesList") {
        description = "Build patch with patch list"

        dependsOn(build)

        classpath = sourceSets["main"].runtimeClasspath
        mainClass.set("app.morphe.util.PatchListGeneratorKt")
    }
    // Used by gradle-semantic-release-plugin.
    publish {
        dependsOn("generatePatchesList")
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs = listOf("-Xcontext-parameters")
    }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Systemad/patcheddit")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
// Keep upstream sources available, but publish only this fork's add-on patches.
tasks.jar {
    eachFile {
        if (path.startsWith("extensions/") && path.endsWith(".mpe") && path != "extensions/sports.mpe") exclude()
        if (path.startsWith("app/morphe/patches/") &&
            !path.startsWith("app/morphe/patches/reddit/customclients/sync/syncforreddit/misc/sports/") &&
            !path.startsWith("app/morphe/patches/reddit/customclients/AppCompatibility") &&
            !path.startsWith("app/morphe/patches/all/misc/resources/AddResourcesPatch")) exclude()
    }
}


// Generate discovery metadata from the shipped bundle, not all retained source classes.
tasks.named<JavaExec>("generatePatchesList") {
    classpath = files(tasks.jar.flatMap { it.archiveFile }) + configurations.runtimeClasspath.get()
}
