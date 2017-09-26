/*
 * Copyright (c) 2017, Vít Kotačka
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package cz.swsamuraj.gradle.fortify

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.logging.LogLevel
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.WarPlugin

class FortifyPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.plugins.apply(JavaPlugin)

        project.extensions.create('jaxws', JaxwsPluginExtension).with {
            if (project.plugins.hasPlugin(WarPlugin)) {
                wsdlDir = "${project.projectDir}/src/main/webapp/WEB-INF/wsdl"
            } else {
                wsdlDir = "${project.projectDir}/src/main/resources/META-INF/wsdl"
            }
            generatedSources = "${project.projectDir}/src/generated/jaxws"
        }

        project.extensions.create('xjc', XjcExtension).with {
            xsdDir = "${project.projectDir}/src/main/resources/META-INF/xsd"
            generatedSources = "${project.projectDir}/src/generated/xjc"
        }

        Task wsImportTask = project.task('wsImport', type: WSImportTask) {
            conventionMapping.wsdlDir = { project.file(project.jaxws.wsdlDir) }
            conventionMapping.generatedSources = { project.file(project.jaxws.generatedSources) }
        }

        wsImportTask.logging.level = LogLevel.QUIET

        project.tasks.getByName('compileJava').dependsOn(wsImportTask)

        Task xjcTask = project.task('xjc', type: XjcTask) {
            conventionMapping.xsdDir = { project.file(project.xjc.xsdDir) }
            conventionMapping.generatedSources = { project.file(project.xjc.generatedSources) }
        }

        project.task('wsClean', type: WSCleanTask) {
            if (wsImportTask.generatedSources.exists()) {
                generatedSources.add(wsImportTask.generatedSources)
            }
            if (xjcTask.generatedSources.exists()) {
                generatedSources.add(xjcTask.generatedSources)
            }
            if (generatedSources.isEmpty()) {
                enabled = false
            }
        }

        project.configurations.create('jaxws') {
            extendsFrom project.configurations.compile
        }

        project.dependencies {
            jaxws 'com.sun.xml.ws:jaxws-tools:2.2.10'
            jaxws 'org.jvnet.jaxb2_commons:jaxb2-basics-annotate:1.0.2'
            jaxws 'org.jvnet.jaxb2_commons:jaxb2-basics-ant:0.9.5'
            jaxws 'javax.validation:validation-api:1.1.0.Final'
        }

        project.sourceSets.main.java {
            srcDir { project.file(project.jaxws.generatedSources) }
            srcDir { project.file(project.xjc.generatedSources) }
        }
    }
}
