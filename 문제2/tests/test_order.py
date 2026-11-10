"""주문 · 품절 전환 (TC-03 ~ TC-06)"""

from tests.helpers import assert_stock


def test_tc03_주문_시_수량만큼_즉시_차감(api, product):
    product_id = product(5)

    response = api.create_order(product_id, quantity=2, customer_id="C001")

    assert response.status_code == 201
    assert response.json()["status"] == "PAID"
    assert_stock(api, product_id, quantity=3, status="AVAILABLE")


def test_tc04_재고_소진_시_품절_전환(api, product):
    product_id = product(2)

    response = api.create_order(product_id, quantity=2, customer_id="C001")

    assert response.status_code == 201
    assert_stock(api, product_id, quantity=0, status="SOLD_OUT")


def test_tc05_품절_상품_주문_거부(api, product):
    product_id = product(0)

    response = api.create_order(product_id, quantity=1, customer_id="C001")

    assert response.status_code == 409
    assert response.json()["code"] == "OUT_OF_STOCK"
    assert_stock(api, product_id, quantity=0)


def test_tc06_재고_초과_주문_전량_거부(api, product):
    product_id = product(2)

    response = api.create_order(product_id, quantity=3, customer_id="C001")

    assert response.status_code == 409
    assert response.json()["code"] == "OUT_OF_STOCK"
    assert_stock(api, product_id, quantity=2)  # 부분 차감 없음
