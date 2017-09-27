package cz.swsamuraj.gradle.fortify

import org.gradle.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import static org.junit.Assert.assertTrue

public class FortifyPluginTest {

    @Rule
    public TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile

    @Before
    public void setup() {
        buildFile = testProjectDir.newFile('build.gradle')
        buildFile << """
            plugins {
                id 'java'
                id 'cz.swsamuraj.fortify' version '0.1.0'
            }
        """
    }

    @Test
    public void fortifyPlugin() {
        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.getRoot())
                .withArguments('fortify')
                .build()

        assertTrue(result.getOutput().contains('Fortify'))
    }
}