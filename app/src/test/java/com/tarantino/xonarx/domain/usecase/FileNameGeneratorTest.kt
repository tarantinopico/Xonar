package com.tarantino.xonarx.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempDirectory

class FileNameGeneratorTest {

    private val generator = FileNameGenerator()

    @Test
    fun `generateUniqueFileName returns original if not exists`() {
        val dir = createTempDirectory().toFile()
        val result = generator.generateUniqueFileName(dir, "test.txt")
        assertEquals("test.txt", result)
    }

    @Test
    fun `generateUniqueFileName appends counter if exists`() {
        val dir = createTempDirectory().toFile()
        File(dir, "test.txt").createNewFile()
        
        val result = generator.generateUniqueFileName(dir, "test.txt")
        assertEquals("test (1).txt", result)
    }

    @Test
    fun `generateUniqueFileName appends counter multiple times`() {
        val dir = createTempDirectory().toFile()
        File(dir, "test.txt").createNewFile()
        File(dir, "test (1).txt").createNewFile()
        File(dir, "test (2).txt").createNewFile()
        
        val result = generator.generateUniqueFileName(dir, "test.txt")
        assertEquals("test (3).txt", result)
    }

    @Test
    fun `generateUniqueFileName handles extensionless files`() {
        val dir = createTempDirectory().toFile()
        File(dir, "test").createNewFile()
        
        val result = generator.generateUniqueFileName(dir, "test")
        assertEquals("test (1)", result)
    }
}
