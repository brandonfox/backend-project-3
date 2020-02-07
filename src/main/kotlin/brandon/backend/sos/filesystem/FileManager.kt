package brandon.backend.sos.filesystem

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.time.LocalDateTime
import java.time.ZoneOffset

abstract class FileManager {

    companion object{
        private val logger: Logger = LoggerFactory.getLogger(this::class.java);

        val filePath: String = System.getenv("FILE_STORE_PATH") ?: "./sosbackend"

        val objectMapper: ObjectMapper = ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)

        fun getEpochTimestamp(): Long{
            return LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
        }
        fun getFile(path: String): File{
            return File("$filePath/$path")
        }
        init {
            logger.info("File path set to $filePath")
            val fileDir = File(filePath)
            if(!fileDir.exists() && !fileDir.mkdir()) throw IOException("Could not create file directory in $filePath")
            logger.info("File directory successfully loaded")
        }
    }
}