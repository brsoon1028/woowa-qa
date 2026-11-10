"""테스트 공통 설정값과 헬퍼. fixture는 conftest.py, 데이터 세팅·검증 함수는 여기에 둔다."""

PRODUCT_ID = "P001"

# 동시성 테스트 설정값
CONCURRENCY_REPEAT = 50  # 동시성 시나리오 반복 실행 횟수
REQUEST_GAP_SEC = 0.02  # 두 요청의 도착 간격 (수십 ms)
RACE_DELAY_SEC = 0.05  # 서버의 재고 확인~차감 사이 처리 시간. 요청 간격보다 길게 잡아 처리가 겹치게 한다.


def set_stock(api, quantity):
    """사전조건 세팅: 운영자 재고 수정 API로 재고를 맞춘다."""
    response = api.update_stock(PRODUCT_ID, quantity)
    assert response.status_code == 200, "사전조건 세팅 실패"
    return PRODUCT_ID


def assert_stock(api, product_id, quantity, status=None):
    """후속 검증: 재고 조회 API로 서버 상태를 확인한다."""
    body = api.get_stock(product_id).json()
    assert body["quantity"] == quantity, f"재고 기대값 {quantity}, 실제 {body['quantity']}"
    if status is not None:
        assert body["status"] == status, f"상태 기대값 {status}, 실제 {body['status']}"
