"""공통 fixture.

- 테스트마다 새 스텁 서버를 띄워 테스트 간 데이터가 섞이지 않게 한다.
- product fixture가 TC의 '사전조건(데이터 세팅)'을 담당한다.
"""

import logging
import threading
from dataclasses import dataclass

import pytest
from werkzeug.serving import make_server

from stub_server.app import StockStore, create_app
from tests.api_client import StockApiClient
from tests.helpers import PRODUCT_ID, set_stock

logging.getLogger("werkzeug").setLevel(logging.ERROR)  # 스텁 서버 요청 로그 숨김


@dataclass
class StubServer:
    store: StockStore
    client: StockApiClient


@pytest.fixture
def server_factory():
    """옵션을 바꿔 스텁 서버를 띄운다. 테스트가 끝나면 모두 종료한다."""
    servers = []

    def _factory(use_lock=True, race_delay=0.0):
        store = StockStore(product_ids=[PRODUCT_ID], use_lock=use_lock, race_delay=race_delay)
        server = make_server("127.0.0.1", 0, create_app(store), threaded=True)
        threading.Thread(target=server.serve_forever, daemon=True).start()
        servers.append(server)
        client = StockApiClient(f"http://127.0.0.1:{server.server_port}")
        return StubServer(store=store, client=client)

    yield _factory
    for server in servers:
        server.shutdown()


@pytest.fixture
def api(server_factory):
    """기본 스텁 서버(잠금 사용)에 연결된 API 클라이언트."""
    return server_factory().client


@pytest.fixture
def product(api):
    """사전조건: 재고 N개 상품. 예) product_id = product(5)"""
    return lambda quantity: set_stock(api, quantity)
