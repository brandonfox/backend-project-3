package brandon.backend.sos.rest

import brandon.backend.sos.filesystem.BucketFileManager
import brandon.backend.sos.filesystem.ObjectFileManager
import brandon.backend.sos.filesystem.UploadTicketManager
import brandon.backend.sos.filesystem.errors.MetadataNotFoundException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.*
import org.springframework.web.bind.annotation.*
import java.io.IOException
import kotlin.Exception

@RestController
@RequestMapping("/{bucketName}/{objectName}")
class ObjectController @Autowired constructor(
        val objectManager: ObjectFileManager,
        val ticketManager: UploadTicketManager
) {

    val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @PostMapping(params = ["create"])
    fun createUploadTicket(@PathVariable bucketName: String, @PathVariable objectName: String): ResponseEntity<String>{
        return try{
            if(!ObjectFileManager.objectNameIsValid(objectName)) throw IOException("Object name is invalid")
            ticketManager.createUploadTicket(bucketName,objectName)
            ResponseEntity(HttpStatus.OK)
        }
        catch (e: Exception){
            ResponseEntity(e.message,HttpStatus.BAD_REQUEST)
        }
    }

    @PostMapping(params = ["complete"])
    fun completeTicket(@PathVariable objectName: String, @PathVariable bucketName: String): ResponseEntity<Any>{
        return try{
            val obj = ticketManager.completeTicket(bucketName, objectName)
            ResponseEntity(obj,HttpStatus.OK)
        }catch(e:Exception){
            ResponseEntity(e.message,HttpStatus.BAD_REQUEST)
        }
    }

    @DeleteMapping(params=["delete"])
    fun deleteObject(@PathVariable bucketName: String, @PathVariable objectName: String): ResponseEntity<String>{
        if(!(BucketFileManager.bucketNameValid(bucketName) && ObjectFileManager.objectNameIsValid(objectName))) return ResponseEntity(HttpStatus.BAD_REQUEST)
        objectManager.deleteObject(bucketName,objectName)
        ticketManager.deleteTicket(bucketName, objectName)
        return ResponseEntity(HttpStatus.OK)
    }

    @PutMapping(params=["metadata","key"])
    fun putMetadataKey(@PathVariable bucketName: String, @PathVariable objectName: String, @RequestParam key: String, @RequestBody metadata: String): ResponseEntity<String>{
        logger.info("Putting metadata: $key for object: $bucketName/$objectName")
        return try {
            objectManager.setMetaData(bucketName, objectName, key, metadata)
            ResponseEntity(HttpStatus.OK)
        }catch(e: Exception){
            ResponseEntity(e.message,HttpStatus.NOT_FOUND)
        }
    }

    @DeleteMapping(params=["metadata","key"])
    fun deleteMetadataKey(@PathVariable bucketName: String, @PathVariable objectName: String, @RequestParam key: String): ResponseEntity<String>{
        logger.info("Deleting metadata: $key for object: $bucketName/$objectName")
        return try{
            objectManager.deleteMetadataByKey(bucketName, objectName, key)
            ResponseEntity(HttpStatus.OK)
        }catch(e: Exception){
            ResponseEntity(e.message,HttpStatus.NOT_FOUND)
        }
    }

    @GetMapping(params=["metadata","key"])
    fun getMetadataByKey(@PathVariable bucketName: String, @PathVariable objectName: String, @RequestParam key: String): ResponseEntity<Any>{
        return try{
            val data = objectManager.getMetadataByKeyJson(bucketName, objectName, key)
            ResponseEntity(data,HttpStatus.OK)
        }catch(e: MetadataNotFoundException){
            ResponseEntity("{}",HttpStatus.OK)
        }
        catch(e: Exception){
            ResponseEntity(e.message,HttpStatus.NOT_FOUND)
        }
    }

    @GetMapping(params=["metadata"])
    fun getAllMetadata(@PathVariable bucketName: String, @PathVariable objectName: String): ResponseEntity<Any> {
        return try{
            val jsonData = objectManager.getAllMetadataJson(bucketName, objectName)
            ResponseEntity(jsonData,HttpStatus.OK)
        }
        catch(e: MetadataNotFoundException){
            ResponseEntity("{}",HttpStatus.OK)
        }
        catch(e: Exception){
            ResponseEntity(e.message,HttpStatus.NOT_FOUND)
        }
    }

}