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
import org.gradle.api.tasks.TaskAction

class FortifyTask extends DefaultTask {

    String group = 'Fortify'
    String description = 'Security analysis by HP Fortify'

    String fortifyBuildID

    Logger logger = project.logger

    @TaskAction
    def fortify() {
        logger.info "Running command: sourceanalyzer -b ${fortifyBuildID} -clean"

        exec {
            commandLine 'sourceanalyzer', '-b', fortifyBuildID, '-clean'
        }

        def classpath = configurations.compile.asPath

        logger.info "Running command: sourceanalyzer -b ${fortifyBuildID} -source " +
                "${sourceCompatibility} -cp ${classpath} src/**/*.java -exclude src/test/**/*.java"

        exec {
            commandLine 'sourceanalyzer', '-b', fortifyBuildID, '-source', sourceCompatibility,
                    '-cp', classpath, 'src/**/*.java', '-exclude', 'src/test/**/*.java'
        }

        def fortifyBuildFolder = 'build/fortify'
        new File(fortifyBuildFolder).mkdirs()
        def fortifyArtifactFileName = "${fortifyBuildID}@${project.version}.mbs"
        def fortifyArtifact = "${fortifyBuildFolder}/${fortifyArtifactFileName}"

        logger.info "Running command: sourceanalyzer -b ${fortifyBuildID} -build-label ${project.version} -export-build-session ${fortifyArtifact}"

        exec {
            commandLine 'sourceanalyzer', '-b', fortifyBuildID, '-build-label', project.version, '-export-build-session', "${fortifyArtifact}"
        }

        logger.info "Running command: sourceanalyzer -b ${fortifyBuildID} -scan -f ${fortifyBuildFolder}/results.fpr"

        exec {
            commandLine 'sourceanalyzer', '-b', fortifyBuildID, '-scan', '-f', "${fortifyBuildFolder}/results.fpr"
        }
    }

}