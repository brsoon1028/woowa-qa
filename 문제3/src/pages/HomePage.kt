package com.example.delivery.e2e.pages

import androidx.test.uiautomator.By
import com.example.delivery.e2e.data.TestData
import com.example.delivery.e2e.data.TestData.APP_PACKAGE
import com.example.delivery.e2e.support.Timeouts
import com.example.delivery.e2e.support.Ui

/** 홈 화면. 가게를 문구 검색이 아닌 가게 목록의 정확한 이름으로 찾는다. */
class HomePage(private val ui: Ui) {

    private val storeList = "$APP_PACKAGE:id/store_list"
    private val storeName = By.res(APP_PACKAGE, "store_name")

    fun waitLoaded(): HomePage {
        ui.waitFor(By.res(storeList), "홈 > 가게 목록", Timeouts.SCREEN)
        return this
    }

    fun openStore(store: TestData.Store): StorePage {
        ui.scrollTo(storeList, store.name, "홈 > 가게 목록")
        ui.waitFor(storeName.text(store.name), "홈 > 가게 '${store.name}'").click()
        return StorePage(ui).waitLoaded(store)
    }
}
