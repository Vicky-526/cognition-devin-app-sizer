/*
 * MIT License
 *
 * Copyright (c) 2024.  Grabtaxi Holdings Pte Ltd (GRAB), All rights reserved.
 *
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE
 */

package com.grab.sizer.config

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class ConfigYmlLoaderTest {
    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun load(yaml: String): Config {
        val file = tempFolder.newFile("config.yml").apply { writeText(yaml.trimIndent()) }
        return ConfigYmlLoader().load(file)
    }

    @Test
    fun loadParsesAllSections() {
        val config = load(
            """
            project-input:
              libraries-directory: "./libs"
              modules-directory: "./modules"
              project-root-dir: "./root"
              r8-mapping-file: "./mapping.txt"
              owner-mapping-file: "./module-owner.yml"
              library-owner-mapping-file: "./library-owner.yml"
              version: "1.0.1"
              large-file-threshold: 10
              project-name: "sample"
            apk-generation:
              bundle-tool: "./bundletool.jar"
              app-bundle-file: "./app.aab"
              device-specs:
                - "./device-1.json"
                - "./device-2.json"
              key-signing:
                keystore-file: "./release.keystore"
                keystore-pw: "store-pw"
                key-alias: "key0"
                key-pw: "key-pw"
            report:
              output-directory: "./out"
              custom-attributes:
                pipelineId: "100"
              influx-db-config:
                db-name: "sizer"
                url: "http://localhost:8086"
                username: "root"
                password: "secret"
                report-table-name: "app_size"
                retention-policy:
                  name: "app_sizer"
                  duration: "360d"
                  shard-duration: "0m"
                  replication-factor: 2
                  is-default: true
            """
        )

        with(config.projectInput) {
            assertEquals("1.0.1", version)
            assertEquals("sample", projectName)
            assertEquals(10L, largeFileThreshold)
            assertEquals(File("./libs"), librariesDirectory)
            assertEquals(File("./modules"), modulesDirectory)
            assertEquals(File("./root"), projectRoot)
            assertEquals(File("./mapping.txt"), r8MappingFile)
            assertEquals(File("./module-owner.yml"), ownerMappingFile)
            assertEquals(File("./library-owner.yml"), libraryOwnerMappingFile)
        }
        with(config.apkGeneration) {
            assertEquals(File("./bundletool.jar"), bundleTool)
            assertEquals(File("./app.aab"), appBundleFile)
            assertEquals(listOf(File("./device-1.json"), File("./device-2.json")), deviceSpecs)
            assertEquals(KeySigning("./release.keystore", "store-pw", "key0", "key-pw"), keySigning)
        }
        with(config.report) {
            assertEquals(File("./out"), outputDirectory)
            assertEquals(mapOf("pipelineId" to "100"), customAttributes)
            val influx = requireNotNull(influxDbConfig)
            assertEquals("sizer", influx.dbName)
            assertEquals("http://localhost:8086", influx.url)
            assertEquals("root", influx.username)
            assertEquals("secret", influx.password)
            assertEquals("app_size", influx.reportTableName)
            assertEquals(RetentionPolicy("app_sizer", "360d", "0m", 2, true), influx.retentionPolicy)
        }
    }

    @Test
    fun loadAppliesDefaultsForOptionalFields() {
        val config = load(
            """
            project-input:
              libraries-directory: "./libs"
              modules-directory: "./modules"
              version: "1.0.1"
              project-name: "sample"
            apk-generation:
              bundle-tool: "./bundletool.jar"
              app-bundle-file: "./app.aab"
              device-specs:
                - "./device-1.json"
            report:
              output-directory: "./out"
            """
        )

        with(config.projectInput) {
            assertEquals(10240L, largeFileThreshold)
            assertEquals(File("./modules"), projectRoot)
            assertNull(r8MappingFile)
            assertNull(ownerMappingFile)
            assertNull(libraryOwnerMappingFile)
        }
        assertNull(config.apkGeneration.keySigning)
        assertNull(config.report.customAttributes)
        assertNull(config.report.influxDbConfig)
    }
}
