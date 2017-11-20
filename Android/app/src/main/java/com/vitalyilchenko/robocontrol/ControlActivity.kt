package com.vitalyilchenko.robocontrol

import android.os.Bundle
import android.app.Activity
import android.content.pm.ActivityInfo
import android.util.Log
import android.widget.Button
import android.widget.SeekBar

import kotlinx.android.synthetic.main.activity_control.*

class ControlActivity : Activity(), SeekBar.OnSeekBarChangeListener {

    private val zeroArea: Int = 20
    private val maxSpeedValue = 99

    private var leftBar: SeekBar? = null
    private var rightBar: SeekBar? = null

    private var _leftValue: Int = 0
    private var _rightValue: Int = 0
    private var _leftDirection: Int = 0
    private var _rightDirection: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_control)

        var maxValue = 2 * zeroArea + 2 * maxSpeedValue
        var defaultPosition = zeroArea + maxSpeedValue

        leftBar = findViewById<SeekBar>(R.id.left_bar)
        leftBar?.setOnSeekBarChangeListener(this)
        leftBar?.max = maxValue
        leftBar?.progress = defaultPosition

        rightBar = findViewById<SeekBar>(R.id.right_bar)
        rightBar?.setOnSeekBarChangeListener(this)
        rightBar?.max = maxValue
        rightBar?.progress = defaultPosition

        var btnStop = findViewById<Button>(R.id.stop_btn)
        btnStop?.setOnClickListener { v ->
            leftBar?.progress = defaultPosition
            rightBar?.progress = defaultPosition

            BluetoothService.write("100100")
        }
    }

    override fun onBackPressed() {
        BluetoothService.disconnect()
        super.onBackPressed()
    }

    override fun onProgressChanged(bar: SeekBar?, progress: Int, fromUser: Boolean) {
        var defaultPosition = zeroArea + maxSpeedValue

        var value = progress - defaultPosition;
        var direction = 1
        if (value < zeroArea && value > -zeroArea)
            value = 0
        else {
            if (value > 0)
                value -= zeroArea
            else {
                direction = 0
                value = (-1 * value - zeroArea)
            }
        }

        if (bar == leftBar) {
            _leftValue = value
            _leftDirection = direction
        }
        else if (bar == rightBar) {
            _rightValue = value
            _rightDirection = direction
        }

        var leftCommand = "$_leftDirection"
        leftCommand += if (_leftValue < 10) "0$_leftValue" else "$_leftValue"

        var rightCommand = "$_rightDirection"
        rightCommand += if (_rightValue < 10) "0$_rightValue" else "$_rightValue"

        var command = leftCommand + rightCommand
        BluetoothService.write(command)
    }

    override fun onStopTrackingTouch(p0: SeekBar?) {
    }

    override fun onStartTrackingTouch(p0: SeekBar?) {
    }
}
