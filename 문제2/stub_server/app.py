"""B마트 재고 API 스텁 서버.

API_SPEC.md에 정의한 규칙대로 메모리에서 재고를 실제로 차감·복구한다.
응답을 미리 고정하는 정적 Mock과 달리 상태를 가지므로 동시성 검증이 가능하다.

- use_lock=True : 재고 확인과 차감을 하나의 잠금 구간에서 처리 (정상 서버)
- use_lock=False: 잠금 없이 처리 (동시성 버그를 재현하는 버그 모드)
- race_delay    : 재고 확인과 차감 사이의 처리 지연(초). 실제 서버의 DB 조회·결제 처리 시간을 흉내 낸다.
"""

import threading
import time
import uuid

from flask import Flask, jsonify, request

AVAILABLE = "AVAILABLE"
SOLD_OUT = "SOLD_OUT"


class StockStore:
    """상품 재고와 주문을 메모리에 보관한다."""

    def __init__(self, product_ids=(), use_lock=True, race_delay=0.0):
        self.stock = {pid: 0 for pid in product_ids}  # productId -> quantity
        self.orders = {}  # orderId -> {productId, quantity, status}
        self.use_lock = use_lock
        self.race_delay = race_delay
        self._lock = threading.Lock()

    def critical_section(self):
        """재고 확인~변경 구간을 보호하는 잠금. 버그 모드에서는 아무것도 잠그지 않는다."""
        return self._lock if self.use_lock else _NoLock()


class _NoLock:
    def __enter__(self):
        return self

    def __exit__(self, *args):
        return False


def _status(quantity):
    return SOLD_OUT if quantity == 0 else AVAILABLE


def _error(http_status, code, message):
    return jsonify({"code": code, "message": message}), http_status


def _valid_quantity(value, allow_zero):
    # bool은 int의 하위 타입이라 별도로 제외한다.
    if not isinstance(value, int) or isinstance(value, bool):
        return False
    return value >= 0 if allow_zero else value > 0


def create_app(store):
    app = Flask(__name__)

    @app.get("/v1/products/<product_id>/stock")
    def get_stock(product_id):
        if product_id not in store.stock:
            return _error(404, "PRODUCT_NOT_FOUND", "상품이 없습니다.")
        quantity = store.stock[product_id]
        return jsonify(
            {"productId": product_id, "quantity": quantity, "status": _status(quantity)}
        )

    @app.post("/v1/orders")
    def create_order():
        body = request.get_json(silent=True) or {}
        product_id = body.get("productId")
        quantity = body.get("quantity")

        if not _valid_quantity(quantity, allow_zero=False):
            return _error(400, "INVALID_QUANTITY", "주문 수량이 올바르지 않습니다.")
        if product_id not in store.stock:
            return _error(404, "PRODUCT_NOT_FOUND", "상품이 없습니다.")

        with store.critical_section():
            current = store.stock[product_id]  # 1. 재고 확인
            if current < quantity:
                # 재고 초과 주문은 전량 거부, 재고는 변하지 않는다.
                return _error(409, "OUT_OF_STOCK", "재고가 부족합니다.")
            time.sleep(store.race_delay)  # 2. 결제 처리 등 실제 서버의 처리 시간
            store.stock[product_id] = current - quantity  # 3. 재고 차감
            order_id = uuid.uuid4().hex
            store.orders[order_id] = {
                "productId": product_id,
                "quantity": quantity,
                "status": "PAID",
            }

        return (
            jsonify(
                {
                    "orderId": order_id,
                    "productId": product_id,
                    "quantity": quantity,
                    "status": "PAID",
                }
            ),
            201,
        )

    @app.post("/v1/orders/<order_id>/cancel")
    def cancel_order(order_id):
        with store.critical_section():
            order = store.orders.get(order_id)
            if order is None:
                return _error(404, "ORDER_NOT_FOUND", "주문이 없습니다.")
            if order["status"] == "CANCELLED":
                # 이미 취소된 주문은 재고를 다시 복구하지 않는다.
                return _error(409, "ALREADY_CANCELLED", "이미 취소된 주문입니다.")
            order["status"] = "CANCELLED"
            store.stock[order["productId"]] += order["quantity"]

        return jsonify({"orderId": order_id, "status": "CANCELLED"})

    @app.put("/v1/admin/products/<product_id>/stock")
    def update_stock(product_id):
        body = request.get_json(silent=True) or {}
        quantity = body.get("quantity")

        if not _valid_quantity(quantity, allow_zero=True):
            return _error(400, "INVALID_QUANTITY", "재고 수량이 올바르지 않습니다.")
        if product_id not in store.stock:
            return _error(404, "PRODUCT_NOT_FOUND", "상품이 없습니다.")

        with store.critical_section():
            store.stock[product_id] = quantity

        return jsonify(
            {"productId": product_id, "quantity": quantity, "status": _status(quantity)}
        )

    return app
