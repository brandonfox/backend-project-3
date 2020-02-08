package brandon.backend.sos.rest

import brandon.backend.sos.filesystem.FileManager
import brandon.backend.sos.filesystem.ObjectFileManager
import org.apache.tomcat.util.http.fileupload.IOUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.io.InputStream
import java.lang.Exception
import javax.servlet.http.HttpServletResponse

@RestController
class ObjectController @Autowired constructor(
        val objectManager: ObjectFileManager
) {

    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

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
                   @RequestHeader("Content-Length") contentLength: Long,
                   @RequestHeader("Content-MD5") md5: String,
                   @RequestParam("file") file: MultipartFile): ResponseEntity<Any>{
        if(partNumber  !in 10001 downTo 0) return ResponseEntity("Invalid part number",HttpStatus.BAD_REQUEST)
        return try{
            logger.info("Got request to put part $partNumber into $bucketName/$objectName: Content size ${contentLength/1000}KB, MD5 $md5")
            objectManager.putPart(bucketName,objectName,partNumber,file,md5,contentLength)
            val retObj = object{
                val md5 = md5
                val length = contentLength
                val partNumber = partNumber
            }
            ResponseEntity(retObj,HttpStatus.OK)
        }
        catch(e: IOException){
            val retObj = object{
                val md5 = md5
                val length = contentLength
                val partNumber = partNumber
                val error = e.message
            }
            ResponseEntity(retObj,HttpStatus.BAD_REQUEST)
        }
    }

    @PostMapping("/{bucketName}/{objectName}",params = ["complete"])
    fun completeTicket(@PathVariable objectName: String, @PathVariable bucketName: String): ResponseEntity<Any>{
        return try{
            val obj = objectManager.completeTicket(bucketName, objectName)
            ResponseEntity(obj,HttpStatus.OK)
        }catch(e:Exception){
            ResponseEntity(e.message,HttpStatus.BAD_REQUEST)
        }
    }

    @GetMapping("/{bucketName}/{objectName}")
    fun downloadObject(response: HttpServletResponse, @PathVariable objectName: String, @PathVariable bucketName: String){
        response.addHeader("Content-disposition","attachment;filename=$objectName")
        response.contentType = "Any"
        response.setContentLengthLong(objectManager.getObjectFileSize(bucketName, objectName))
        IOUtils.copy(objectManager.downloadObject(bucketName, objectName),response.outputStream)
        response.flushBuffer()
    }

}