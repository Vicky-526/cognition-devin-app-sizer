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

import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.JsonNode
import tools.jackson.databind.annotation.JsonDeserialize
import tools.jackson.databind.deser.std.StdDeserializer
import tools.jackson.module.kotlin.contains
import java.io.File

private const val DEFAULT_LARGE_FILE = 10240L // 10kb

@JsonDeserialize(using = ProjectInputConfigDeserializer::class)
data class ProjectInputConfig(
    val version: String,
    val projectName: String,
    val largeFileThreshold: Long = DEFAULT_LARGE_FILE,
    val librariesDirectory: File,
    val modulesDirectory: File,
    val projectRoot: File,
    val r8MappingFile: File? = null,
    val ownerMappingFile: File? = null,
    val libraryOwnerMappingFile: File? = null,
) {
    val modulesDirIsProjectRoot: Boolean
        get() = modulesDirectory.path.startsWith(projectRoot.path)

}

class ProjectInputConfigDeserializer(vc: Class<*>? = null) :
    StdDeserializer<ProjectInputConfig>(vc ?: ProjectInputConfig::class.java) {
    override fun deserialize(jsonParser: JsonParser, ctxt: DeserializationContext): ProjectInputConfig =
        ctxt.readTree(jsonParser).run {
            ProjectInputConfig(
                version = get("version").asString(),
                largeFileThreshold = if (contains("large-file-threshold")) get("large-file-threshold").asLong() else DEFAULT_LARGE_FILE,
                projectName = get("project-name").asString(),
                librariesDirectory = File(get("libraries-directory").asString()),
                modulesDirectory = File(get("modules-directory").asString()),
                r8MappingFile = if (contains("r8-mapping-file")) File(get("r8-mapping-file").asString()) else null,
                ownerMappingFile = if (contains("owner-mapping-file")) File(get("owner-mapping-file").asString()) else null,
                libraryOwnerMappingFile = if (contains("library-owner-mapping-file")) File(get("library-owner-mapping-file").asString()) else null,
                projectRoot = if (contains("project-root-dir")) File(get("project-root-dir").asString()) else File(get("modules-directory").asString()),
            )
        }
}