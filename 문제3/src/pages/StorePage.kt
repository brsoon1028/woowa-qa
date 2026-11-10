package com.example.delivery.e2e.pages

import androidx.test.uiautomator.By
import com.example.delivery.e2e.data.TestData
import com.example.delivery.e2e.data.TestData.APP_PACKAGE
import com.example.delivery.e2e.support.Timeouts
import com.example.delivery.e2e.support.Ui
import org.junit.Assert.assertEquals

/** 가게 상세 화면. 들어온 가게가 맞는지 확인한 뒤 메뉴를 담는다. */
class StorePage(private val ui: Ui) {

    private val title = By.res(APP_PACKAGE, "store_title")
    private val menuList = "$APP_PACKAGE:id/menu_list"
    private val menuName = By.res(APP_PACKAGE, "menu_name")
    private val addToCart = By.res(APP_PACKAGE, "add_to_cart")
    private val goToCart = By.res(APP_PACKAGE, "go_to_cart")

    fun waitLoaded(store: TestData.Store): StorePage {
        val actual = ui.waitFor(title, "가게 상세 > 가게명", Timeouts.SCREEN).text
        assertEquals("가게 상세 진입 가게", store.name, actual)
        return this
    }

    fun addMenu(menu: TestData.Menu): StorePage {
        ui.scrollTo(menuList, menu.name, "가게 상세 > 메뉴 목록")
        ui.waitFor(menuName.text(menu.name), "가게 상세 > 메뉴 '${menu.name}'").click()
        ui.waitFor(addToCart, "메뉴 상세 > 담기 버튼").click()
        return this
    }

    fun goToCart(): CartPage {
        ui.waitFor(goToCart, "가게 상세 > 장바구니 이동 버튼").click()
        return CartPage(ui).waitLoaded()
    }
}
