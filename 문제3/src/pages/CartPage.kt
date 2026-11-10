package com.example.delivery.e2e.pages

import androidx.test.uiautomator.By
import com.example.delivery.e2e.data.TestData
import com.example.delivery.e2e.data.TestData.APP_PACKAGE
import com.example.delivery.e2e.support.Timeouts
import com.example.delivery.e2e.support.Ui
import org.junit.Assert.assertEquals

/** 장바구니 화면. 담긴 메뉴와 금액을 확인하고 주문하기로 넘어간다. */
class CartPage(private val ui: Ui) {

    private val cartItemName = By.res(APP_PACKAGE, "cart_item_name")
    private val totalPrice = By.res(APP_PACKAGE, "cart_total_price")
    private val orderButton = By.res(APP_PACKAGE, "cart_order_button")

    fun waitLoaded(): CartPage {
        ui.waitFor(orderButton, "장바구니 > 주문하기 버튼", Timeouts.SCREEN)
        return this
    }

    fun assertItem(menu: TestData.Menu): CartPage {
        assertEquals("장바구니 메뉴", menu.name, ui.waitFor(cartItemName, "장바구니 > 메뉴명").text)
        assertEquals("장바구니 금액", formatPrice(menu.price), ui.waitFor(totalPrice, "장바구니 > 합계").text)
        return this
    }

    fun order(): CheckoutPage {
        ui.waitFor(orderButton, "장바구니 > 주문하기 버튼").click()
        return CheckoutPage(ui).waitLoaded()
    }
}

fun formatPrice(price: Int): String = "%,d원".format(price)
