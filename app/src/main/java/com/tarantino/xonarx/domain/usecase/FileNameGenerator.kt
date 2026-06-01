package com.tarantino.xonarx.domain.usecase

import java.io.File

class FileNameGenerator {
    fun generateUniqueFileName(directory: File, originalName: String): String {
        var file = File(directory, originalName)
        var fileName = originalName

        var counter = 1
        val nameWithoutExt = originalName.substringBeforeLast(".")
        val ext = if (originalName.contains(".")) "." + originalName.substringAfterLast(".") else ""
        
        while (file.exists()) {
            fileName = "${nameWithoutExt} ($counter)$ext"
            file = File(directory, fileName)
            counter++
        }
        
        return fileName
    }
}
