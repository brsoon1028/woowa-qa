package com.example.delivery.e2e.base

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.io.File

/**
 * 테스트가 실패하면 스크린샷과 UI 계층 덤프를 저장한다.
 * CI에서 실패했을 때 재현 없이 "어떤 화면에서 무엇이 보였는지" 확인하기 위함이다.
 */
class FailureCaptureRule(private val device: UiDevice) : TestWatcher() {

    override fun failed(e: Throwable, description: Description) {
        val dir = File(
            InstrumentationRegistry.getInstrumentation().targetContext.externalCacheDir,
            "e2e-failures"
        ).apply { mkdirs() }
        val name = "${description.className}_${description.methodName}_${System.currentTimeMillis()}"

        device.takeScreenshot(File(dir, "$name.png"))
        device.dumpWindowHierarchy(File(dir, "$name.xml"))
    }
}
