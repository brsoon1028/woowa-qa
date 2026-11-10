package com.example.delivery.e2e.pages

import androidx.test.uiautomator.By
import com.example.delivery.e2e.data.TestData.APP_PACKAGE
import com.example.delivery.e2e.support.Timeouts
import com.example.delivery.e2e.support.Ui

/** 주문서(결제) 화면. 테스트 결제수단을 명시적으로 고르고 결제한다. */
class CheckoutPage(private val ui: Ui) {

    private val paymentMethod = By.res(APP_PACKAGE, "payment_method_item")
    private val payButton = By.res(APP_PACKAGE, "checkout_pay_button")
    private val loading = By.res(APP_PACKAGE, "payment_loading")

    fun waitLoaded(): CheckoutPage {
        ui.waitFor(payButton, "주문서 > 결제하기 버튼", Timeouts.SCREEN)
        return this
    }

    fun selectPaymentMethod(name: String): CheckoutPage {
        ui.waitFor(paymentMethod.text(name), "주문서 > 결제수단 '$name'").click()
        return this
    }

    /** Thread.sleep 대신 '결제 처리 화면이 사라지고 주문 현황이 뜰 때까지' 기다린다. */
    fun pay(): OrderStatusPage {
        ui.waitFor(payButton, "주문서 > 결제하기 버튼").click()
        if (ui.isVisible(loading)) {
            ui.waitUntilGone(loading, "결제 처리 중 화면", Timeouts.PAYMENT)
        }
        return OrderStatusPage(ui).waitLoaded()
    }
}
