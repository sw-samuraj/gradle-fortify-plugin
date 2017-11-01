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

import org.gradle.api.DefaultTask
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.StopExecutionException
import org.gradle.api.tasks.TaskAction

class FortifyTask extends DefaultTask {

    String group = 'Fortify'
    String description = 'Security analysis by HP Fortify'

    String fortifyBuildID

    Logger logger = project.logger

    @TaskAction
    def fortify() {
        doChecks()

        exec(['sourceanalyzer', '-b', getFortifyBuildID(), '-clean'])

        def translateCommand = assambleTranslateCommand()

        exec(translateCommand)

        def fortifyBuildFolder = 'build/fortify'
        new File(fortifyBuildFolder).mkdirs()
        def fortifyArtifactFileName = "${getFortifyBuildID()}@${project.version}.mbs"
        def fortifyArtifact = "${fortifyBuildFolder}/${fortifyArtifactFileName}"

        exec(['sourceanalyzer', '-b', getFortifyBuildID(), '-build-label', project.version, '-export-build-session', fortifyArtifact])

        exec(['sourceanalyzer', '-b', getFortifyBuildID(), '-scan', '-f', "${fortifyBuildFolder}/results.fpr"])
    }

    def exec(params) {
        logger.info("[Fortify] ${params.join(' ')}")

        def stdout = new ByteArrayOutputStream()

        project.exec {
            commandLine(params)
            standardOutput = stdout
        }

        stdout.toString()
    }

    def doChecks() {
        if (getFortifyBuildID() == null) {
            logger.warn('[Fortify] Mandatory parameter fortifyBuildID has not been configured.')
            throw new StopExecutionException()
        }
    }

    /**
     * A complete command should look like:
     * <pre>['sourceanalyzer', '-b', getFortifyBuildID(), '-source', project.sourceCompatibility, '-cp', classpath, 'src/**\/*.java', '-exclude', 'src/test/**|/*.java']</pre>
     */
    def assambleTranslateCommand() {
        def translateCommand = ['sourceanalyzer', '-b', getFortifyBuildID(), '-source', project.sourceCompatibility]
        translateCommand = addClasspathParameter(translateCommand)
        translateCommand << 'src/**/*.java'
        translateCommand = addExcludeParameter(translateCommand)

        translateCommand
    }

    def addClasspathParameter(translateCommand) {
        def classpath = project.configurations.compile.asPath

        if (!"".equals(classpath)) {
            translateCommand += ['-cp', classpath]
        }

        translateCommand
    }

    def addExcludeParameter(translateCommand) {
        if (!sourceSets.test.java.isEmpty()) {
            translateCommand += ['-exclude', 'src/test/**/*.java']
        }

        translateCommand
    }

}
