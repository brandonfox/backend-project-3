package brandon.backend.sos.rest

import brandon.backend.sos.filesystem.ObjectFileManager
import brandon.backend.sos.filesystem.UploadTicketManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.*
import org.springframework.web.bind.annotation.*
import java.io.IOException
import java.lang.Exception

@RestController
@RequestMapping("/{bucketName}/{objectName}")
class ObjectController @Autowired constructor(
        val objectManager: ObjectFileManager,
        val ticketManager: UploadTicketManager
) {

    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @PostMapping(params = ["create"])
    fun createUploadTicket(@PathVariable bucketName: String, @PathVariable objectName: String): ResponseEntity<String>{
        return try{
            ticketManager.createUploadTicket(bucketName,objectName)
            ResponseEntity(HttpStatus.OK)
        }
        catch (e: IOException){
            ResponseEntity(e.message,HttpStatus.OK)
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
        //TODO Check to see if bucketname-objectname is valid
        objectManager.deleteObject(bucketName,objectName)
        return ResponseEntity(HttpStatus.OK)
    }

}