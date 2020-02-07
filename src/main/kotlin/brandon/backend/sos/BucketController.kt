package brandon.backend.sos

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.io.File
import java.io.IOException
import java.time.LocalDateTime
import java.time.ZoneOffset

@RestController
class BucketController {

    private final val logger = LoggerFactory.getLogger(this.javaClass);

    private final val filePath: String = System.getenv("FILE_STORE_PATH") ?: "./sosbackend"

    private final val objectMapper = ObjectMapper()

    init {
        logger.info("File path set to $filePath")
        val fileDir = File(filePath)
        if(!fileDir.exists() && !fileDir.mkdir()) throw IOException("Could not create file directory in $filePath")
        logger.info("File directory successfully loaded")
    }

    fun getEpochTimestamp(): Long{
        return LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
    }

    @RequestMapping("/{bucketName}",params = ["create"])
    fun createBucket(@PathVariable bucketName: String): ResponseEntity<String>{
        val bucket = File("$filePath/$bucketName")
        if(bucket.exists()) return ResponseEntity("Bucket already exists",HttpStatus.BAD_REQUEST)
        else if(!bucket.mkdir()) return ResponseEntity("Error occurred while creating bucket",HttpStatus.BAD_REQUEST)
        else{
            val time = getEpochTimestamp()
            val obj = object {
                val created = time
                val modified = time
                val name = bucketName
            }
            objectMapper.writeValue(File("$filePath/$bucketName/metadata.json"),obj)
            return ResponseEntity(objectMapper.writeValueAsString(obj),HttpStatus.OK)
        }
    }

    @RequestMapping("/{bucketName}",params = ["delete"])
    fun deleteBucket(@PathVariable bucketName: String){
        println("Deleting $bucketName");

    }

    @RequestMapping("/{bucketName}",params = ["list"])
    fun listBucket(@PathVariable bucketName: String){
        println("Listing objects in $bucketName");

    }

}