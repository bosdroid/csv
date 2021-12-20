package com.boris.expert.csvmagic.utils

import android.content.Context
import java.io.*

object CSVFile {

    fun readFile(context: Context, inputStream: InputStream): MutableList<Array<String?>> {
        val resultList = mutableListOf<Array<String?>>()
        val reader = BufferedReader(InputStreamReader(inputStream, "UTF-16"))
        val dir = File(context.filesDir, "tempcsv")
        dir.mkdirs()
        val file = File(dir, "tempCsvFile.csv")
        val out = OutputStreamWriter(FileOutputStream(file), "UTF-8")
        val cbuf = CharArray(2048)
        var len: Int
        while (reader.read(cbuf, 0, cbuf.size).also { len = it } != -1) {
            out.write(cbuf, 0, len)
        }
        val reader1 = BufferedReader(InputStreamReader(file.inputStream(), "UTF-8"))
        val Separator = ','
        val Delimiter = '"'
        val LF = '\n'
        val CR = '\r'
        var quote_open = false

        var line = reader1.readLine()
        while (line != null) {
            if (line.contains("\t".toRegex())){
                line = line.replace("\t".toRegex(), ",")
            }

            val a = StringDArray()
            var token = ""
            line += Separator
            for (c:Char in line.toCharArray()){
                when(c){
                    LF, CR -> {
                        quote_open = false
                        a.add(token)
                        token = ""
                    }
                    Delimiter -> {
                        quote_open = !quote_open
                    }
                    Separator -> {
                        if (!quote_open) {
                            a.add(token)
                            token = ""
                        } else {
                            token += c
                        }
                    }
                    else->{
                        token+=c
                    }
                }
            }

            if (a.length() > 0) {
                if (resultList.size > 0) {
                    val header_row = resultList.get(0)
                    if (a.length() >= header_row.size) {
                        val row: Array<String?> = a.get_araay()
                        resultList.add(row)
                    }
                } else {
                    val row: Array<String?> = a.get_araay()
                    resultList.add(row) //header row
                }
            }

//        try {
//            var csvLine = reader.readLine()
//            while (csvLine != null) {
//                val tempLine = csvLine.replace("\t".toRegex(), ",")
//                val row = tempLine.split(",".toRegex()).toTypedArray()
//                resultList.add(row)
                if (reader1.readLine() == null) {
                    break
                } else {
                    line = reader1.readLine()
                }
//            }
//        } catch (ex: IOException) {
//            throw RuntimeException("Error in reading CSV file: $ex")
//        } finally {
//            try {
//                inputStream.close()
//            } catch (e: IOException) {
//                throw RuntimeException("Error while closing input stream: $e")
//            }
        }
        return resultList
    }

}