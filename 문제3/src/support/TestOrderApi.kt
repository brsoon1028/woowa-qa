package com.example.delivery.e2e.support

/**
 * 사전조건 세팅과 정리를 UI가 아닌 테스트용 API로 처리한다. (가정한 인터페이스)
 * UI로 준비 작업까지 하면 테스트가 길어지고 실패 지점이 늘어나기 때문이다.
 */
interface TestOrderApi {
    /** 테스트 계정 로그인 토큰을 발급해 앱에 주입한다. */
    fun loginAsTestUser()

    /** 기본 배송지를 테스트 주소로 설정한다. */
    fun setDefaultAddress()

    /** 장바구니를 비운다. 이전 테스트가 남긴 상품 때문에 금액이 달라지는 것을 막는다. */
    fun clearCart()

    /** 테스트 가게를 영업 중 상태로 만든다. */
    fun openStore(storeId: String)

    /** 주문을 취소해 테스트 데이터를 정리한다. */
    fun cancelOrder(orderNumber: String)
}
