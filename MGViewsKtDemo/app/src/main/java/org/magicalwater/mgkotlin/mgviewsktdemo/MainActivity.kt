package org.magicalwater.mgkotlin.mgviewsktdemo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import org.magicalwater.mgkotlin.mgviewskt.view.MGObservableScrollView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        settingScrollView()
    }

    private fun settingScrollView() {
        mScrollView.mDistanceDetect = 50
        mScrollView.scrollDelegate = object : MGObservableScrollView.ObservableScrollDelegate {
            override fun onScrollDistanceDetect(direction: MGObservableScrollView.ScrollDirection) {
                super.onScrollDistanceDetect(direction)
            }
        }
    }
}
