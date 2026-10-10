-keep class app.plain.launcher.enforce.GateAccessibilityService { *; }
-keepclassmembers class * extends android.app.Activity {
    public void *(android.view.View);
}
