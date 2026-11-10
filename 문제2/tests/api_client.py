"""테스트에서 사용하는 API 호출 헬퍼.

테스트 코드에는 URL이나 요청 형식을 직접 쓰지 않고 이 클래스의 메서드만 사용한다.
API 경로가 바뀌어도 이 파일만 고치면 된다.
"""

import requests


class StockApiClient:
    def __init__(self, base_url):
        self.base_url = base_url
        self.session = requests.Session()

    def get_stock(self, product_id):
        return self.session.get(f"{self.base_url}/v1/products/{product_id}/stock")

    def create_order(self, product_id, quantity, customer_id):
        # 동시 요청에서는 세션을 공유하지 않도록 매번 새 연결을 쓴다.
        return requests.post(
            f"{self.base_url}/v1/orders",
            json={"productId": product_id, "quantity": quantity, "customerId": customer_id},
        )

    def cancel_order(self, order_id):
        return self.session.post(f"{self.base_url}/v1/orders/{order_id}/cancel")

    def update_stock(self, product_id, quantity):
        return self.session.put(
            f"{self.base_url}/v1/admin/products/{product_id}/stock",
            json={"quantity": quantity},
        )
