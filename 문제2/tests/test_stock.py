"""재고 조회 (TC-01 ~ TC-02)"""


def test_tc01_재고_조회_정상(api, product):
    product_id = product(5)

    response = api.get_stock(product_id)

    assert response.status_code == 200
    assert response.json()["quantity"] == 5
    assert response.json()["status"] == "AVAILABLE"


def test_tc02_장바구니_담기_조회_시_재고_변동_없음(api, product):
    product_id = product(5)

    for _ in range(3):
        response = api.get_stock(product_id)
        assert response.status_code == 200
        assert response.json()["quantity"] == 5
