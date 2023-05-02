package me.okonecny.whodoes

import androidx.compose.ui.unit.Density
import java.awt.GraphicsEnvironment

/**
 * Workaround for detecting density.
 */
val DetectedDensity: Density by lazy {
    val graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment()
    val fallbackDensity = Density(
        graphicsEnvironment.defaultScreenDevice.defaultConfiguration.defaultTransform.scaleX.toFloat()
    )
    if (graphicsEnvironment.isHeadlessInstance) return@lazy fallbackDensity

    val sunGraphicsEnvironmentClass = Class.forName("sun.java2d.SunGraphicsEnvironment")
    if (!sunGraphicsEnvironmentClass.isInstance(graphicsEnvironment)) return@lazy fallbackDensity

    try {
        val isUIScaleEnabled = sunGraphicsEnvironmentClass.getDeclaredMethod("isUIScaleEnabled")
        isUIScaleEnabled.isAccessible = true
        val isEnabled = isUIScaleEnabled(graphicsEnvironment) as Boolean
        if (!isEnabled) return@lazy fallbackDensity

        val getDebugScale = sunGraphicsEnvironmentClass.getDeclaredMethod("getDebugScale")
        getDebugScale.isAccessible = true

        val pixelDensity = (getDebugScale(graphicsEnvironment) as Double).toFloat()
        return@lazy Density(pixelDensity, 1f)
    } catch (e: NoSuchMethodException) {
        return@lazy fallbackDensity
    }
}