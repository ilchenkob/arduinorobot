package com.vitalyilchenko.robocontrol

import android.os.Bundle
import android.app.Activity
import android.content.pm.ActivityInfo
import android.widget.SeekBar

import kotlinx.android.synthetic.main.activity_control.*

class ControlActivity : Activity(), SeekBar.OnSeekBarChangeListener {

    private var leftBar: SeekBar? = null
    private var rightBar: SeekBar? = null

    private var _lastComamnd: String = ""
    private var _leftValue: Int = 0
    private var _rightValue: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_control)

        leftBar = findViewById<SeekBar>(R.id.left_bar)
        leftBar?.setOnSeekBarChangeListener(this)
        rightBar = findViewById<SeekBar>(R.id.right_bar)
        rightBar?.setOnSeekBarChangeListener(this)

        BluetoothService.write("199199")
    }

    override fun onProgressChanged(bar: SeekBar?, progress: Int, fromUser: Boolean) {
//        var value = progress - 254;
//        if (value < 30 && value > -30)
//            value = 0
//
//        if (bar == leftBar) {
//            _leftValue = value
//        }
//        else if (bar == rightBar) {
//            _rightValue = value
//        }
//
//        var command = ""
//        if (_leftValue < 0) {
//            value = _leftValue * (-1)
//            command = "0$value"
//        }
//        else {
//            command = "1$_leftValue"
//        }
//
//        if (_rightValue < 0) {
//            value = _rightValue * (-1)
//            command = "0$value"
//        }
//        else {
//            command = "1$_rightValue"
//        }
//
//        BluetoothService.write(command)
    }

    override fun onStopTrackingTouch(p0: SeekBar?) {
    }

    override fun onStartTrackingTouch(p0: SeekBar?) {
    }
}
