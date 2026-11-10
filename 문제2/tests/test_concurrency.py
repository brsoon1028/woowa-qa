"""동시성 (TC-12 ~ TC-13) 과 버그 모드 확인.

동시 요청은 스레드로 만든다. 각 스레드는 Barrier에서 출발 신호를 기다렸다가
지정된 간격(수십 ms)을 두고 요청을 보낸다.
"""

import threading
from collections import Counter

import pytest

from tests.helpers import (
    CONCURRENCY_REPEAT,
    PRODUCT_ID,
    RACE_DELAY_SEC,
    REQUEST_GAP_SEC,
    set_stock,
)


def send_orders_concurrently(client, customers, gap_sec):
    """고객 수만큼 주문을 동시에 보내고 응답 상태코드 목록을 돌려준다."""
    barrier = threading.Barrier(len(customers))
    status_codes = [None] * len(customers)

    def worker(index, customer_id):
        barrier.wait()  # 모든 스레드가 준비되면 동시에 출발
        threading.Event().wait(index * gap_sec)  # 고객별 도착 간격
        response = client.create_order(PRODUCT_ID, quantity=1, customer_id=customer_id)
        status_codes[index] = response.status_code

    threads = [
        threading.Thread(target=worker, args=(i, c)) for i, c in enumerate(customers)
    ]
    for t in threads:
        t.start()
    for t in threads:
        t.join()
    return status_codes


def run_last_one_scenario(server):
    """재고 1개에 고객 A, B가 수십 ms 간격으로 주문하는 시나리오 1회."""
    set_stock(server.client, 1)
    server.store.orders.clear()
    codes = send_orders_concurrently(server.client, ["C001", "C002"], REQUEST_GAP_SEC)
    stock = server.client.get_stock(PRODUCT_ID).json()
    return Counter(codes), stock, len(server.store.orders)


@pytest.mark.parametrize("round_no", range(1, CONCURRENCY_REPEAT + 1))
def test_tc12_마지막_1개_동시_주문_선착순(server_factory, round_no):
    server = server_factory(race_delay=RACE_DELAY_SEC)

    codes, stock, order_count = run_last_one_scenario(server)

    # 기대 응답
    assert codes == Counter({201: 1, 409: 1}), f"{round_no}회차 응답 {dict(codes)}"
    # 후속 검증
    assert stock["quantity"] == 0, f"{round_no}회차 재고 {stock['quantity']} (음수면 초과 판매)"
    assert stock["status"] == "SOLD_OUT"
    assert order_count == 1, f"{round_no}회차 생성된 주문 {order_count}건"


def test_tc13_다수_고객_동시_주문_시_초과_판매_방지(server_factory):
    server = server_factory(race_delay=RACE_DELAY_SEC / 10)
    set_stock(server.client, 5)
    customers = [f"C{i:03d}" for i in range(1, 21)]

    codes = Counter(send_orders_concurrently(server.client, customers, gap_sec=0))

    # 기대 응답
    assert codes[201] == 5, f"성공 {codes[201]}건"
    assert codes[409] == 15, f"품절 {codes[409]}건"
    # 후속 검증
    stock = server.client.get_stock(PRODUCT_ID).json()
    assert stock["quantity"] == 0
    assert len(server.store.orders) == 5


@pytest.mark.bugmode
def test_버그모드_잠금이_없으면_TC12가_초과_판매를_잡아낸다(server_factory):
    """테스트가 실제로 버그를 잡을 수 있는지 확인한다.

    잠금을 끈 서버에서 같은 시나리오를 돌리면 두 주문이 모두 성공해 초과 판매가 생겨야 한다.
    이 테스트가 통과한다는 것은 TC-12의 검증이 이 버그를 실패로 판정한다는 뜻이다.
    """
    server = server_factory(use_lock=False, race_delay=RACE_DELAY_SEC)

    codes, stock, order_count = run_last_one_scenario(server)

    assert codes[201] == 2, "잠금이 없는데도 초과 판매가 재현되지 않음"
    assert stock["quantity"] < 0 or order_count > 1
