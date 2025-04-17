package pl.edu.pw.ia.manager.infrastructure

import java.io.File
import java.io.InputStream

data class IoResult(
    val stdOut: InputStream,
    val stdErr: InputStream,
)

fun String.runCommand(
    workingDir: File = File("."),
    environment: Map<String, String>? = null,
    input: InputStream? = null,
): Result<IoResult> {
    val command = "\\s".toRegex().split(this)
    return command.runCommand(workingDir, environment, input)
}

fun List<String>.runCommand(
    workingDir: File = File("."),
    environment: Map<String, String>? = null,
    input: InputStream? = null,
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

    val returnCode = process.waitFor()

    if (returnCode != 0) {
        error("Command failed with code $returnCode")
    }

    return@runCatching IoResult(
        stdOut = process.inputStream,
        stdErr = process.errorStream,
    )
}
