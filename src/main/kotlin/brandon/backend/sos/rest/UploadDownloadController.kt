package brandon.backend.sos.rest

import brandon.backend.sos.filesystem.ObjectDownloadManager
import brandon.backend.sos.filesystem.ObjectFileManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/{bucketName}/{objectName}")
class UploadDownloadController @Autowired constructor(
        val objectManager: ObjectFileManager,
        val downloadManager: ObjectDownloadManager
) {

    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @PutMapping(params = ["partNumber"])
    fun uploadPart(@PathVariable bucketName: String,
                   @PathVariable objectName: String,
                   @RequestParam partNumber: Int,
                   @RequestHeader("Content-Length") contentLength: Long,
                   @RequestHeader("Content-MD5") md5: String,
                   @RequestParam("file") file: MultipartFile): ResponseEntity<Any> {
        if(partNumber  !in 10001 downTo 0) return ResponseEntity("Invalid part number", HttpStatus.BAD_REQUEST)
        return try{
            logger.info("Got request to put part $partNumber into $bucketName/$objectName: Content size ${contentLength/1000}KB, MD5 $md5")
            downloadManager.putPart(bucketName,objectName,partNumber,file,md5,contentLength)
            val retObj = object{
                val md5 = md5
                val length = contentLength
                val partNumber = partNumber
            }
            ResponseEntity(retObj, HttpStatus.OK)
        }
        catch(e: IOException){
            val retObj = object{
                val md5 = md5
                val length = contentLength
                val partNumber = partNumber
                val error = e.message
            }
            ResponseEntity(retObj, HttpStatus.BAD_REQUEST)
        }
    }

    //TODO Convert download and upload to async methods (Not threaded)
    @GetMapping
    fun downloadObject(@PathVariable objectName: String, @PathVariable bucketName: String, response: HttpServletResponse): ResponseEntity<Any>{
        response.setHeader("Content-Disposition","attachment; filename=$objectName")
        response.setContentLengthLong(objectManager.getObjectFileSize(bucketName, objectName))
        response.contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE
        val outputStream = response.outputStream
        val inputStream = downloadManager.downloadObject(bucketName, objectName)
        while(inputStream.available() > 0){
            val buffer = ByteArray(1024 * 8)
            inputStream.read(buffer)
            outputStream.write(buffer)
        }
        outputStream.flush()
        outputStream.close()
        return ResponseEntity(HttpStatus.OK)
    }

    @DeleteMapping(params = ["{partNumber}"])
    fun deletePart(@PathVariable bucketName: String, @PathVariable objectName: String, @RequestParam partNumber: Int): ResponseEntity<String>{
        return try{
            downloadManager.deletePart(bucketName,objectName,partNumber)
            ResponseEntity(HttpStatus.OK)
        }catch(e: Exception){
            ResponseEntity(e.message,HttpStatus.BAD_REQUEST)
        }
    }

}