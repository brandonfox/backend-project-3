package brandon.backend.sos.rest

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import java.io.IOException

@RestController
class ObjectController {

    @PostMapping("/{bucketName}/{objectName}",params = ["create"])
    fun createUploadTicket(@PathVariable bucketName: String, @PathVariable objectName: String): ResponseEntity<String>{
        return try{
            createUploadTicket(bucketName,objectName)
            ResponseEntity(HttpStatus.OK)
        }
        catch (e: IOException){
            ResponseEntity(e.message,HttpStatus.OK)
        }
    }

}