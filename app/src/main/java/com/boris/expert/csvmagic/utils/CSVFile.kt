package com.boris.expert.csvmagic.utils

import android.content.Context
import android.os.Build
import java.io.*
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.regex.Pattern


object CSVFile {

    fun readFile(context: Context, selectedFile: File): MutableList<Array<String?>> {
        val resultList = mutableListOf<Array<String?>>()
        val text = StringBuilder()
        try {
            val br = BufferedReader(FileReader(selectedFile))
            var line = br.readLine()
            while (line != null) {
                text.append(line)
                //text.append('\n')
                line = br.readLine()
            }
            br.close()
        } catch (e: IOException) {
            //You'll need to add proper error handling here
        }
        val reader1: BufferedReader

        if (!isPureAscii(text.toString())) {
            val reader: BufferedReader = if (text.toString().contains("�".toRegex())) {
                val inputStreamReader =
                    InputStreamReader(selectedFile.inputStream(), "UTF_16")

                BufferedReader(inputStreamReader)
            } else {
                val inputStreamReader =
                    InputStreamReader(selectedFile.inputStream())

                BufferedReader(inputStreamReader)
            }


            val dir = File(context.filesDir, "tempcsv")
            dir.mkdirs()
            val file = File(dir, "tempCsvFile_${System.currentTimeMillis()}.csv")
            val out = OutputStreamWriter(
                FileOutputStream(file),
                StandardCharsets.UTF_8
            )
            val cbuf = CharArray(2048)
            var len = reader.read(cbuf, 0, cbuf.size)
            while (len != -1) {
                out.write(cbuf, 0, len)
                len = reader.read(cbuf, 0, cbuf.size)
            }
            reader1 = BufferedReader(
                InputStreamReader(
                    file.inputStream(),
                    StandardCharsets.UTF_8
                )
            )
        } else {
            reader1 = BufferedReader(
                InputStreamReader(
                    selectedFile.inputStream()
                )
            )
        }


        val Separator = ','
        val Delimiter = '"'
        val LF = '\n'
        val CR = '\r'
        var quote_open = false
        var index = 0
        var line = reader1.readLine()
        while (line != null) {
            if (line.contains("\t".toRegex())) {
                line = line.replace(",", "|")
                line = line.replace("\t".toRegex(), ",")
            }
//            else {
//                if (index == 0) {
//                    index +=1
//                    val row: Array<String?> = line.split(",".toRegex()).toTypedArray()
//                    resultList.add(row)
//                    continue
//                }
//                //line = line.replace(",", "|")
//            }
//            val row: Array<String?> = line.split(",".toRegex()).toTypedArray()
//            resultList.add(row)

//            if (reader1.readLine() == null) {
//                break
//            } else {
//                line = reader1.readLine()
//            }

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

//        try {
//            var csvLine = reader1.readLine()
//            while (csvLine != null) {
//                if (csvLine.contains("\t".toRegex())) {
//                    csvLine = csvLine.replace("\t".toRegex(), ",")
//                }
//                //csvLine = StringEscapeUtils.unescapeCsv(csvLine)
//                val row: Array<String?> = csvLine.split(",".toRegex()).toTypedArray()
//                resultList.add(row)
//                if (reader1.readLine() == null) {
//                    break
//                } else {
//                    csvLine = reader1.readLine()
//                }
//            }
//        } catch (ex: IOException) {
//            throw RuntimeException("Error in reading CSV file: $ex")
//        } finally {
//            try {
//                inputStream.close()
//            } catch (e: IOException) {
//                throw RuntimeException("Error while closing input stream: $e")
//            }
//        }

        return resultList
    }

    private fun checkTextLanguage(s: String): Boolean {
        var isCyrillicCharacter = false
        for (c: Char in s.toCharArray()) {
            if (isCyrillicCharacter(c)) {
                isCyrillicCharacter = true
                break
            } else {
                isCyrillicCharacter = false
            }
        }
        return isCyrillicCharacter
    }

    fun isPureAscii(v: String?): Boolean {
        return Charset.forName("US-ASCII").newEncoder().canEncode(v)
        // or "ISO-8859-1" for ISO Latin 1
        // or StandardCharsets.US_ASCII with JDK1.7+
    }

    private fun isCyrillicCharacter(c: Char): Boolean {
        val isPriorToKitkat = Build.VERSION.SDK_INT < 19
        val block: Character.UnicodeBlock = Character.UnicodeBlock.of(c)!!
        return block == Character.UnicodeBlock.CYRILLIC || block == Character.UnicodeBlock.CYRILLIC_SUPPLEMENTARY || if (isPriorToKitkat) false else block == Character.UnicodeBlock.CYRILLIC_EXTENDED_A || block == Character.UnicodeBlock.CYRILLIC_EXTENDED_B
    }


}