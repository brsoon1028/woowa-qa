package com.example.delivery.e2e

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.delivery.e2e.base.BaseUiTest
import com.example.delivery.e2e.data.TestData
import com.example.delivery.e2e.pages.HomePage
import com.example.delivery.e2e.pages.formatPrice
import com.example.delivery.e2e.support.TestOrderApi
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 주문 플로우 E2E.
 * 화면 조작은 Page Object가 담당하고, 이 파일에는 '무엇을 검증하는지'만 남긴다.
 */
@RunWith(AndroidJUnit4::class)
class OrderFlowTest : BaseUiTest() {

    // 실제 구현은 테스트 환경의 API 클라이언트를 주입한다.
    private lateinit var api: TestOrderApi
    private var createdOrderNumber: String? = null

    @Before
    fun prepareTestState() {
        api = TestOrderApiProvider.get()
        api.loginAsTestUser()
        api.setDefaultAddress()
        api.clearCart()
        api.openStore(TestData.STORE.id)
    }

    @After
    fun cleanUp() {
        createdOrderNumber?.let { api.cancelOrder(it) }
    }

    @Test
    fun 주문_완료_시_주문현황에_주문정보와_접수상태가_노출된다() {
        val store = TestData.STORE
        val menu = TestData.MENU

        val order = HomePage(ui).waitLoaded()
            .openStore(store)
            .addMenu(menu)
            .goToCart()
            .assertItem(menu)
            .order()
            .selectPaymentMethod(TestData.TEST_PAYMENT_METHOD)
            .pay()
            .read()
        createdOrderNumber = order.orderNumber

        assertTrue("주문번호 발급", order.orderNumber.isNotBlank())
        assertEquals("주문 가게", store.name, order.storeName)
        assertEquals("주문 메뉴", menu.name, order.menuName)
        assertEquals("결제 금액", formatPrice(menu.price), order.paidAmount)
        assertTrue(
            "주문 직후 상태가 ${TestData.STATUS_AFTER_ORDER} 중 하나여야 함. 실제=${order.status}",
            order.status in TestData.STATUS_AFTER_ORDER
        )
    }
}

/** 테스트 환경별 API 구현을 연결하는 지점. (실행 환경이 없어 선언만 둔다) */
object TestOrderApiProvider {
    fun get(): TestOrderApi = TODO("테스트 환경의 TestOrderApi 구현 연결")
}
