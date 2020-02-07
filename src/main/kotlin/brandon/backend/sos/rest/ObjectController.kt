package brandon.backend.sos.rest

import brandon.backend.sos.filesystem.ObjectFileManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.io.IOException

@RestController
class ObjectController @Autowired constructor(
        val objectManager: ObjectFileManager
) {

    @PostMapping("/{bucketName}/{objectName}",params = ["create"])
    fun createUploadTicket(@PathVariable bucketName: String, @PathVariable objectName: String): ResponseEntity<String>{
        return try{
            objectManager.createUploadTicket(bucketName,objectName)
            ResponseEntity(HttpStatus.OK)
        }
        catch (e: IOException){
            ResponseEntity(e.message,HttpStatus.OK)
        }
    }
    @PutMapping("/{bucketName}/{objectName}",params = ["partNumber"])
    fun uploadPart(@PathVariable bucketName: String,
                   @PathVariable objectName: String,
                   @RequestParam partNumber: Int,
                   @RequestHeader("Content-Length") contentLength: Int,
                   @RequestHeader("Content-MD5") md5: String): ResponseEntity<String>{
        if(partNumber in 10001 downTo 0) return ResponseEntity("Invalid part number",HttpStatus.BAD_REQUEST)
        return try{
            ResponseEntity(HttpStatus.OK)
        }
        catch(e: IOException){
            ResponseEntity(e.message,HttpStatus.BAD_REQUEST)
        }
    }

}