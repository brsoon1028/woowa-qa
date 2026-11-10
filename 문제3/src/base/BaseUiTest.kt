package com.example.delivery.e2e.base

import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.example.delivery.e2e.data.TestData
import com.example.delivery.e2e.support.Timeouts
import com.example.delivery.e2e.support.Ui
import org.junit.Before
import org.junit.Rule

/** 모든 E2E 테스트의 공통 준비: 기기 연결, 앱 실행, 알려진 팝업 처리, 실패 증거 저장. */
abstract class BaseUiTest {

    protected val device: UiDevice =
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    protected val ui = Ui(device)

    @get:Rule
    val failureCapture = FailureCaptureRule(device)

    @Before
    fun launchApp() {
        device.pressHome()
        val context = InstrumentationRegistry.getInstrumentation().context
        val intent = context.packageManager.getLaunchIntentForPackage(TestData.APP_PACKAGE)!!
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
        device.wait(Until.hasObject(By.pkg(TestData.APP_PACKAGE).depth(0)), Timeouts.SCREEN)
        dismissKnownPopups()
    }

    /** 이벤트 팝업, 업데이트 안내처럼 날마다 뜰 수도 있는 화면을 닫는다. 없으면 그냥 지나간다. */
    protected fun dismissKnownPopups() {
        val closeButtons = listOf(
            By.res(TestData.APP_PACKAGE, "event_popup_close"),
            By.res(TestData.APP_PACKAGE, "update_notice_later"),
            By.res("com.android.permissioncontroller", "permission_deny_button"),
        )
        closeButtons.forEach { selector ->
            device.findObject(selector)?.click()
        }
    }
}
