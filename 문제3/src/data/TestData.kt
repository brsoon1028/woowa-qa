package com.example.delivery.e2e.data

/**
 * 테스트 전용 데이터. 화면 문구로 대상을 찾지 않도록 고정된 테스트 가게·메뉴를 쓴다.
 * 테스트 가게는 자동 접수를 꺼 두어 주문 직후 상태가 바로 바뀌지 않게 한다.
 */
object TestData {
    const val APP_PACKAGE = "com.example.delivery"

    data class Store(val id: String, val name: String)
    data class Menu(val id: String, val name: String, val price: Int)

    val STORE = Store(id = "E2E_STORE_CHICKEN", name = "E2E 테스트 치킨")
    val MENU = Menu(id = "E2E_MENU_FRIED", name = "후라이드 치킨", price = 18_000)
    const val TEST_PAYMENT_METHOD = "E2E 테스트 결제"

    /** 주문 직후 허용되는 상태. 접수 대기에서 접수 완료로 넘어가는 중간 시점도 정상으로 본다. */
    val STATUS_AFTER_ORDER = setOf("접수 대기", "접수 완료")
}
