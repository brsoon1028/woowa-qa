"""취소 · 재고 복구 (TC-07 ~ TC-10)"""

from tests.helpers import assert_stock


def _order(api, product_id, quantity, customer_id="C001"):
    """사전조건: 주문 완료 상태를 만든다."""
    response = api.create_order(product_id, quantity=quantity, customer_id=customer_id)
    assert response.status_code == 201, "사전조건 주문 생성 실패"
    return response.json()["orderId"]


def test_tc07_취소_시_차감된_재고_즉시_복구(api, product):
    product_id = product(5)
    order_id = _order(api, product_id, quantity=2)  # 재고 3개

    response = api.cancel_order(order_id)

    assert response.status_code == 200
    assert response.json()["status"] == "CANCELLED"
    assert_stock(api, product_id, quantity=5)


def test_tc08_품절_상품_취소_시_판매중_전환(api, product):
    product_id = product(1)
    order_id = _order(api, product_id, quantity=1)  # 품절

    response = api.cancel_order(order_id)

    assert response.status_code == 200
    assert_stock(api, product_id, quantity=1, status="AVAILABLE")


def test_tc09_복구_후_다른_고객_즉시_구매_가능(api, product):
    product_id = product(1)
    order_id = _order(api, product_id, quantity=1, customer_id="C001")  # 품절
    api.cancel_order(order_id)

    response = api.create_order(product_id, quantity=1, customer_id="C002")

    assert response.status_code == 201
    assert_stock(api, product_id, quantity=0, status="SOLD_OUT")


def test_tc10_중복_취소_시_재고_이중_복구_방지(api, product):
    product_id = product(5)
    order_id = _order(api, product_id, quantity=2)
    api.cancel_order(order_id)  # 재고 5개

    response = api.cancel_order(order_id)

    assert response.status_code == 409
    assert response.json()["code"] == "ALREADY_CANCELLED"
    assert_stock(api, product_id, quantity=5)  # 7개면 결함
