package brandon.backend.sos.rest

import brandon.backend.sos.filesystem.BucketFileManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/{bucketName}")
class BucketController @Autowired constructor(
        val bucketManager: BucketFileManager
): ErrorController{

    val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @RequestMapping("/error")
    fun errorMapping(): String{
        return "Oops you weren't supposed to do that"
    }

    @PostMapping(params = ["create"])
    fun createBucket(@PathVariable bucketName: String): ResponseEntity<String>{
        return try{
            val json = bucketManager.createBucket(bucketName)
            logger.info("Created new bucket: $bucketName")
            ResponseEntity(json,HttpStatus.OK)
        }catch(e: Exception){
            ResponseEntity(e.message,HttpStatus.BAD_REQUEST)
        }
    }

    @DeleteMapping(params = ["delete"])
    fun deleteBucket(@PathVariable bucketName: String): ResponseEntity<String>{
        return try{
            bucketManager.deleteBucket(bucketName)
            logger.info("Deleted bucket $bucketName")
            ResponseEntity(HttpStatus.OK)
        }catch(e:Exception){
            ResponseEntity(e.message,HttpStatus.BAD_REQUEST)
        }

    }

    @GetMapping(params = ["list"])
    fun listBucket(@PathVariable bucketName: String): ResponseEntity<String>{
        return try {
            ResponseEntity(bucketManager.getBucketMetadata(bucketName), HttpStatus.OK)
        }catch(e: Exception) {
            ResponseEntity(e.message, HttpStatus.BAD_REQUEST)
        }
    }

    override fun getErrorPath(): String {
        return "/error"
    }


}