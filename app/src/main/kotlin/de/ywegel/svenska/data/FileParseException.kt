package de.ywegel.svenska.data

/**
 * Distinguishable failure modes of [FileRepository.parseFile]
 */
sealed class FileParseException(message: String? = null, cause: Throwable? = null) : Exception(message, cause) {
    class FileNotFound : FileParseException(message = "Could not open an input stream for the picked file")

    data class InvalidFormat(val originalCause: Throwable) :
        FileParseException(message = "The picked file is not a valid word importer json file", cause = originalCause)

    data class Unexpected(val originalCause: Throwable) :
        FileParseException(message = originalCause.message, cause = originalCause)
}
