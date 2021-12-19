package com.boris.expert.csvmagic.utils

class StringDArray {

    private var data = arrayOfNulls<String>(0)
    private var used = 0
    fun add(str: String?) {
        if (used >= data.size) {
            val new_size = used + 1
            val new_data = arrayOfNulls<String>(new_size)
            System.arraycopy(data, 0, new_data, 0, used)
            data = new_data
        }
        data[used++] = str
    }

    fun length(): Int {
        return used
    }

    fun get_araay(): Array<String?> {
        return data
    }

}