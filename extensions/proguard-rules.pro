-dontobfuscate
-dontoptimize
-keepattributes *
-keep class app.morphe.** {
  *;
}
-keep class com.google.** {
  *;
}
# Proguard can strip away kotlin intrinsics methods that are used by extension Kotlin code. Unclear why.
-keep class kotlin.jvm.internal.Intrinsics {
    public static *;
}
-dontwarn java.lang.reflect.AnnotatedType
-dontwarn javax.lang.model.element.Modifier

# Standalone Sync add-on hooks are invoked from patched app bytecode.
-keep class app.systemad.sports.** { *; }
