package me.okonecny.whodoes.components

import androidx.compose.ui.unit.Density
import java.awt.GraphicsEnvironment

/**
 * Workaround for detecting density.
 */
fun detectDensity(originalDensity: Density): Density {
    val graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment()
    val fallbackDensity = Density(
        graphicsEnvironment.defaultScreenDevice.defaultConfiguration.defaultTransform.scaleX.toFloat(),
        originalDensity.fontScale
    )
    if (graphicsEnvironment.isHeadlessInstance) return fallbackDensity

    val sunGraphicsEnvironmentClass = Class.forName("sun.java2d.SunGraphicsEnvironment")
    if (!sunGraphicsEnvironmentClass.isInstance(graphicsEnvironment)) return fallbackDensity

    try {
        val isUIScaleEnabled = sunGraphicsEnvironmentClass.getDeclaredMethod("isUIScaleEnabled")
        isUIScaleEnabled.isAccessible = true
        val isEnabled = isUIScaleEnabled(graphicsEnvironment) as Boolean
        if (!isEnabled) return fallbackDensity

        val getDebugScale = sunGraphicsEnvironmentClass.getDeclaredMethod("getDebugScale")
        getDebugScale.isAccessible = true

        val pixelDensity = (getDebugScale(graphicsEnvironment) as Double).toFloat()
        return Density(pixelDensity, originalDensity.fontScale)
    } catch (e: NoSuchMethodException) {
        return fallbackDensity
    }
}