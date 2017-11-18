package com.vitalyilchenko.robocontrol

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.vitalyilchenko.robocontrol.Models.BluetoothItem
import java.security.InvalidAlgorithmParameterException

/**
 * Created by vitalyilchenko on 11/14/17.
 */
class DeviceListAdapter(context: Context?)
    : ArrayAdapter<BluetoothItem>(context, android.R.layout.simple_list_item_2) {

    private val items: MutableList<BluetoothItem> = ArrayList()

    fun setItems(data: List<BluetoothItem>) {
        items.clear()
        data.forEach { item ->
            items.add(item)
        }
        notifyDataSetChanged()
    }

    override fun getCount() = items.size

    override fun getItem(position: Int): BluetoothItem {
        if (items.size <= position)
            throw InvalidAlgorithmParameterException("Position should be less then items count")

        return items.elementAt(position)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var item = getItem(position)
        var view: View
        if (convertView != null)
            view = convertView!!
        else
            view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, null)

        view.findViewById<TextView>(android.R.id.text1)?.text = item.name
        view.findViewById<TextView>(android.R.id.text2)?.text = item.state

        return view
    }
}