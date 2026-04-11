/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2026 The XtextBuilder authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.mavenplugins.gradle.xtext.plugin.integrationtest

import com.google.common.io.Files
import org.apache.maven.artifact.versioning.ComparableVersion
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.BuildTask
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.io.TempDir

import java.nio.charset.StandardCharsets

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.fail

abstract class AbstractGradleBuildTest {

    GradleRunner gradleRunner
    ProjectUnderTest rootProject

    @TempDir
    private File rootProjectDir

    void setup() {
        rootProject = new ProjectUnderTest()
        rootProject.name = "root"
        rootProject.projectDir = new File(rootProjectDir, rootProject.name)
        rootProject.owner = this
        gradleRunner = GradleRunner.create()
                .withGradleVersion(getGradleVersion().toString())
                .withPluginClasspath()
                .withProjectDir(rootProject.projectDir) //.forwardOutput()
    }

    ProjectUnderTest getRootProject() {
        return rootProject
    }

    BuildResult build(String... tasks) {
        logBuildFile()
        BuildResult result = gradleRunner.withArguments(tasks + defaultArguments).build()
        logResult(result)
        return result
    }

    BuildResult buildAndFail(String... tasks) {
        logBuildFile()
        BuildResult result = gradleRunner.withArguments(tasks + defaultArguments).buildAndFail()
        logResult(result)
        return result
    }

    void logBuildFile() {
        println("====== ${rootProject.buildFile.name} ======:\n${getContentAsString(rootProject.buildFile)}\n====== EOF ======")
    }

    void logResult(BuildResult result) {
        println("====== Build output ======:\n${result.output}\n====== EOF ======")
    }

    private String[] getDefaultArguments() {
        [
            "-Dhttp.connectionTimeout=120000",
            "-Dhttp.socketTimeout=120000",
            "-s",
            isJava8Runtime() ? "--warning-mode=all" : "--warning-mode=fail"
        ]
    }

    boolean isJava8Runtime() {
        return System.getProperty('java.version').startsWith('1.8')
    }

    void setContent(File file, CharSequence content) {
        file.parentFile.mkdirs()
        file.createNewFile()
        Files.asCharSink(file, StandardCharsets.UTF_8).write(content)
    }

    void append(File file, CharSequence content) {
        if(file.exists) {
            file << content.toString()
        } else {
            setContent(file, content)
        }
    }

    String getContentAsString(File file) {
        return Files.asCharSource(file, StandardCharsets.UTF_8).read()
    }

    byte[] getContent(File file) {
        return Files.toByteArray(file)
    }

    void shouldExist(File file) {
        if(!file.exists()) {
            String relativePath = rootProject.projectDir.toURI().relativize(file.path).path
            fail("File '${relativePath}' should exist but it does not.")
        }
    }

    void shouldNotExist(File file) {
        if(file.exists()) {
            String relativePath = rootProject.projectDir.toPath().relativize(file.toPath())
            fail("File '${relativePath}' should not exist but it does.")
        }
    }

    void shouldContain(File file, CharSequence content) {
        assertEquals(content.toString(), getContentAsString(file))
    }

    void shouldBeUpToDate(BuildTask task) {
        shouldBe(task, TaskOutcome.UP_TO_DATE)
    }

    void shouldBe(BuildTask task, TaskOutcome outcome) {
        if(task.outcome != outcome) {
            fail("Expected task '${task.path}' to be ${outcome} but was: <${task.outcome}>")
        }
    }

    void shouldNotBeUpToDate(BuildTask task) {
        shouldNotBe(task, TaskOutcome.UP_TO_DATE)
    }

    void shouldNotBe(BuildTask task, TaskOutcome outcome) {
        if(task.outcome == outcome) {
            fail("Expected task '${task.path}' not to be ${outcome} but it was.")
        }
    }

    ComparableVersion getGradleVersion() {
        return new ComparableVersion(System.getProperty("gradle.version", "9.4.1"))
    }

    void logBuildDirectoryFiles() {
        File buildDir = rootProject.file('build')
        if (buildDir.exists()) {
            println "====== Build Directory Files (Recursive) BEGIN ======"
            buildDir.eachFileRecurse { file ->
                if (file.isFile()) {
                    def relativePath = buildDir.toPath().relativize(file.toPath()).toString()
                    def size = file.size()
                    def lastModified = new Date(file.lastModified())
                    println "  ${relativePath} (${size} bytes, modified: ${lastModified})"
                }
            }
            println "====== Build Directory Files (Recursive) END ======"
        } else {
            println "Build directory does not exist!"
        }
    }

    private void addSubProjectToBuild(ProjectUnderTest project) {
        File settingsFile = rootProject.file("settings.gradle")
        append(settingsFile, "\ninclude '${project.path}'")
    }

    static class ProjectUnderTest {
        AbstractGradleBuildTest owner
        ProjectUnderTest parent
        String name
        File projectDir

        Set<ProjectUnderTest> subProjects = new LinkedHashSet()

        void setBuildFile(CharSequence content) {
            owner.setContent(new File(projectDir, 'build.gradle'), content)
        }

        File getBuildFile() {
            new File(projectDir, 'build.gradle')
        }

        File file(String relativePath) {
            new File(projectDir, relativePath)
        }

        File createFile(String relativePath, CharSequence content) {
            File file = file(relativePath)
            owner.setContent(file, content)
            return file
        }

        ProjectUnderTest createSubProject(String name) {
            ProjectUnderTest newProject = new ProjectUnderTest()
            newProject.name = name
            newProject.projectDir = file(name)
            newProject.parent = this
            newProject.owner = owner
            subProjects += newProject
            owner.addSubProjectToBuild(newProject)
            return newProject
        }

        Set<ProjectUnderTest> getSubProjects() {
            return Collections.unmodifiableSet(subProjects)
        }

        def String getPath() {
            if(parent === null) {
                return ""
            } else {
                return "${parent.path}:${name}"
            }
        }
    }
}
