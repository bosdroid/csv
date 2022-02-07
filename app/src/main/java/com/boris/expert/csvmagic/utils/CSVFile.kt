package com.boris.expert.csvmagic.utils

import android.content.Context
import org.mozilla.universalchardet.UniversalDetector
import java.io.*
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets


object CSVFile {

    fun readFile(context: Context, selectedFile: File): MutableList<Array<String?>> {
        val resultList = mutableListOf<Array<String?>>()
        val text = StringBuilder()

        val fIn = FileInputStream(selectedFile)
        val buf = ByteArray(4096)
        val detector = UniversalDetector(null)
        var nread: Int
        while (fIn.read(buf).also { nread = it } > 0 && !detector.isDone()) {
            detector.handleData(buf, 0, nread)
        }
        detector.dataEnd()
        val encoding: String = detector.getDetectedCharset()
        var chartsetName: String? = null
        if (encoding.equals("WINDOWS-1252", ignoreCase = true)) {
            chartsetName = "ISO-8859-1"
        }
        if (encoding.equals("UTF-16", ignoreCase = true)) {
            chartsetName = "UTF-16"
        }
        if (encoding.equals("UTF-16LE", ignoreCase = true)) {
            chartsetName = "UTF-16LE"
        }
        if (encoding.equals("UTF-8", ignoreCase = true)) {
            chartsetName = "UTF-8"
        }

        if (encoding.equals("US-ASCII", ignoreCase = true)) {
            chartsetName = "US-ASCII"
        }

        val reader1: BufferedReader
        try {
            val inputStreamReader = InputStreamReader(
                selectedFile.inputStream(),
                chartsetName
            )
            val br = BufferedReader(inputStreamReader)
            var line = br.readLine()
            while (line != null) {
                text.append(line)
                line = br.readLine()
            }
            br.close()
        } catch (e: IOException) {
            //You'll need to add proper error handling here
        }


//        if (!isPureAscii(text.toString())) {
//            val reader: BufferedReader = if (text.toString().contains("�".toRegex())) {
//                val inputStreamReader =
//                    InputStreamReader(selectedFile.inputStream(), "UTF_16")
//
//                BufferedReader(inputStreamReader)
//            } else {
//                val inputStreamReader =
//                    InputStreamReader(selectedFile.inputStream())
//
//                BufferedReader(inputStreamReader)
//            }
//
//
//            val dir = File(context.filesDir, "tempcsv")
//            dir.mkdirs()
//            val file = File(dir, "tempCsvFile_${System.currentTimeMillis()}.csv")
//            val out = OutputStreamWriter(
//                FileOutputStream(file),
//                StandardCharsets.UTF_8
//            )
//            val cbuf = CharArray(2048)
//            var len = reader.read(cbuf, 0, cbuf.size)
//            while (len != -1) {
//                out.write(cbuf, 0, len)
//                len = reader.read(cbuf, 0, cbuf.size)
//            }
//            reader1 = BufferedReader(
//                InputStreamReader(
//                    file.inputStream(),
//                    StandardCharsets.UTF_8
//                )
//            )
//        } else {
            reader1 = BufferedReader(
                InputStreamReader(
                    selectedFile.inputStream(),chartsetName
                )
            )
//        }


        val Separator = ','
        val Delimiter = '"'
        val LF = '\n'
        val CR = '\r'
        var quote_open = false

        var line = reader1.readLine()
        while (line != null) {
            if (line.contains("\t".toRegex())) {
                line = line.replace(",", "|")
                line = line.replace("\t".toRegex(), ",")
            }

            val a = StringDArray()
            var token = ""
            line += Separator

            for (c: Char in line.toCharArray()) {
                when (c) {
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
                    else -> {
                        token += c
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
            line = reader1.readLine()
        }

        return resultList
    }


    fun isPureAscii(v: String?): Boolean {
        return Charset.forName("US-ASCII").newEncoder().canEncode(v)
        // or "ISO-8859-1" for ISO Latin 1
        // or StandardCharsets.US_ASCII with JDK1.7+
    }


}