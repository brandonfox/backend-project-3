package brandon.backend.sos.rest

import brandon.backend.sos.filesystem.BucketFileManager
import brandon.backend.sos.filesystem.BucketFileManager.getBucketMetadata
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RestController
import java.io.IOException

@RestController
class BucketController : ErrorController{

    val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @RequestMapping("/error")
    fun errorMapping(): String{
        return "Oops you weren't supposed to do that"
    }

    @PostMapping("/{bucketName}",params = ["create"])
    fun createBucket(@PathVariable bucketName: String): ResponseEntity<String>{
        return try{
            val json = BucketFileManager.createBucket(bucketName)
            logger.info("Created new bucket: $bucketName")
            ResponseEntity(json,HttpStatus.OK)
        }catch(e: IOException){
            ResponseEntity(e.message,HttpStatus.BAD_REQUEST)
        }
    }

    @DeleteMapping("/{bucketName}",params = ["delete"])
    fun deleteBucket(@PathVariable bucketName: String): ResponseEntity<String>{
        return try{
            BucketFileManager.deleteBucket(bucketName)
            logger.info("Deleted bucket $bucketName")
            ResponseEntity(HttpStatus.OK)
        }catch(e:IOException){
            ResponseEntity(e.message,HttpStatus.BAD_REQUEST)
        }

    }

    @GetMapping("/{bucketName}",params = ["list"])
    fun listBucket(@PathVariable bucketName: String): ResponseEntity<String>{
        return try {
            ResponseEntity(getBucketMetadata(bucketName), HttpStatus.OK)
        }catch(e: IOException) {
            ResponseEntity("Could not find data for $bucketName", HttpStatus.BAD_REQUEST)
        }
    }

    override fun getErrorPath(): String {
        return "/error"
    }


}