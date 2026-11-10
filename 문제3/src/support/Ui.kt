package com.example.delivery.e2e.support

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until

/**
 * 타임아웃을 한곳에서 관리한다.
 * CI처럼 느린 환경에서는 계측 인자 timeoutScale(예: 2)로 전체를 늘린다.
 */
object Timeouts {
    private val scale: Long =
        InstrumentationRegistry.getArguments().getString("timeoutScale")?.toLongOrNull() ?: 1L

    val SHORT = 3_000L * scale     // 같은 화면 안의 요소
    val SCREEN = 10_000L * scale   // 화면 전환
    val PAYMENT = 30_000L * scale  // 결제 처리 후 주문 현황 진입
}

/** 요소 대기와 검색을 담당한다. 못 찾으면 null 대신 원인이 담긴 메시지로 실패한다. */
class Ui(val device: UiDevice) {

    fun waitFor(selector: BySelector, name: String, timeout: Long = Timeouts.SHORT): UiObject2 =
        device.wait(Until.findObject(selector), timeout)
            ?: throw AssertionError("[$name] 요소를 ${timeout}ms 안에 찾지 못함. selector=$selector")

    fun waitUntilGone(selector: BySelector, name: String, timeout: Long = Timeouts.SCREEN) {
        val gone = device.wait(Until.gone(selector), timeout)
        if (gone != true) throw AssertionError("[$name] ${timeout}ms가 지나도 사라지지 않음")
    }

    fun isVisible(selector: BySelector): Boolean = device.hasObject(selector)

    /** 스크롤 가능한 목록에서 텍스트가 정확히 일치하는 항목을 찾을 때까지 스크롤한다. */
    fun scrollTo(listResId: String, itemText: String, name: String) {
        val list = UiScrollable(UiSelector().resourceId(listResId))
        val found = list.scrollIntoView(UiSelector().text(itemText))
        if (!found) throw AssertionError("[$name] 목록에서 '$itemText'를 찾지 못함")
    }
}
