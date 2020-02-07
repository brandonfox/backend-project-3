package brandon.backend.sos

import brandon.backend.sos.filesystem.FileManager
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SosApplication

fun main(args: Array<String>) {
	FileManager.filePath
	runApplication<SosApplication>(*args)
}
