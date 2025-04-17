package pl.edu.pw.ia.manager.infrastructure

import pl.edu.pw.ia.heartbeat.infrastructure.getLogger
import java.io.File
import java.io.InputStream

class CommandLineExtension

private val logger = getLogger(CommandLineExtension::class.java)

data class IoResult(
    val stdOut: String,
    val stdErr: String,
)

fun String.runCommand(
    workingDir: File = File("."),
    environment: Map<String, String>? = null,
    input: InputStream? = null,
    checked: Boolean = true,
): Result<IoResult> {
    val command = "\\s".toRegex().split(this)
    return command.runCommand(workingDir, environment, input, checked)
}

fun List<String>.runCommand(
    workingDir: File = File("."),
    environment: Map<String, String>? = null,
    input: InputStream? = null,
    checked: Boolean = true,
): Result<IoResult> = runCatching {
    val builder = ProcessBuilder(this)
        .directory(workingDir)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
    if (environment != null) {
        builder.environment().putAll(environment)
    }

    val process = builder.start()

    input?.transferTo(process.outputStream)

    val stdOutReader = process.inputStream.bufferedReader()
    val stdErrReader = process.errorStream.bufferedReader()

    val returnCode = process.waitFor()

    if (returnCode != 0) {
        error("Command failed with code $returnCode")
    }

    return@runCatching IoResult(
        stdOut = stdOutReader.readText(),
        stdErr = stdErrReader.readText(),
    )
}
    .onFailure { exc ->
        logger.error("Execution for command: ${this.joinToString(separator = " ")} failed", exc)
        if (checked) {
            throw exc
        }
    }
