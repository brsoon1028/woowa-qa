package com.example.delivery.e2e.pages

import androidx.test.uiautomator.By
import com.example.delivery.e2e.data.TestData.APP_PACKAGE
import com.example.delivery.e2e.support.Timeouts
import com.example.delivery.e2e.support.Ui

/** 주문 현황 화면. 화면에 보이는 주문 정보를 읽어서 테스트가 검증할 수 있게 돌려준다. */
class OrderStatusPage(private val ui: Ui) {

    private val orderNumber = By.res(APP_PACKAGE, "order_number")
    private val storeName = By.res(APP_PACKAGE, "order_store_name")
    private val menuName = By.res(APP_PACKAGE, "order_menu_name")
    private val paidAmount = By.res(APP_PACKAGE, "order_paid_amount")
    private val status = By.res(APP_PACKAGE, "order_status")

    data class OrderInfo(
        val orderNumber: String,
        val storeName: String,
        val menuName: String,
        val paidAmount: String,
        val status: String,
    )

    fun waitLoaded(): OrderStatusPage {
        ui.waitFor(orderNumber, "주문 현황 > 주문번호", Timeouts.PAYMENT)
        return this
    }

    fun read(): OrderInfo = OrderInfo(
        orderNumber = ui.waitFor(orderNumber, "주문 현황 > 주문번호").text,
        storeName = ui.waitFor(storeName, "주문 현황 > 가게명").text,
        menuName = ui.waitFor(menuName, "주문 현황 > 메뉴명").text,
        paidAmount = ui.waitFor(paidAmount, "주문 현황 > 결제금액").text,
        status = ui.waitFor(status, "주문 현황 > 주문 상태").text,
    )
}
