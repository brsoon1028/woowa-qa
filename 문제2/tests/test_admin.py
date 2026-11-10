"""운영자 재고 수정 (TC-11)"""

from tests.helpers import assert_stock


def test_tc11_운영자_수정값_즉시_반영(api, product):
    product_id = product(5)

    response = api.update_stock(product_id, quantity=10)

    assert response.status_code == 200
    assert_stock(api, product_id, quantity=10, status="AVAILABLE")
