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
import org.springframework.web.context.request.async.DeferredResult
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

    private fun getRangeContentSize(range: String): Int{
        val unit = range.substringBefore('=')
        if(unit != "bytes") throw Exception("Range unit must be bytes")
        val start = range.substringAfter('=').substringBefore('-').toInt()
        val end = range.substringAfter('-').toInt()
        return end - start
    }

    @PutMapping(params = ["partNumber"])
    fun uploadPart(@PathVariable bucketName: String,
                   @PathVariable objectName: String,
                   @RequestParam partNumber: Int,
                   @RequestHeader("Content-Length") contentLength: Long,
                   @RequestHeader("Content-MD5") md5: String,
                   @RequestParam("file") file: MultipartFile): DeferredResult<ResponseEntity<Any>> {
        val deferredResult = DeferredResult<ResponseEntity<Any>>()
        return try{
            if(partNumber !in 10001 downTo 0) throw Exception("Invalid part number")
            logger.info("Got request to put part $partNumber into $bucketName/$objectName: Content size ${contentLength/1000}KB, MD5 $md5")
            downloadManager.putPart(bucketName,objectName,partNumber,file,md5,contentLength,deferredResult)
            return deferredResult
        }
        catch(e: IOException){
            val retObj = object{
                val md5 = md5
                val length = contentLength
                val partNumber = partNumber
                val error = e.message
            }
            deferredResult.setResult(ResponseEntity(retObj,HttpStatus.BAD_REQUEST))
            deferredResult
        }
    }

    @GetMapping
    fun downloadObject(@PathVariable objectName: String, @PathVariable bucketName: String, @RequestHeader(required = false) range:String? = null,  response: HttpServletResponse): DeferredResult<ResponseEntity<Any>>{
        logger.info("Got request to download file $bucketName/$objectName")
        val future = DeferredResult<ResponseEntity<Any>>()
        return try {
            response.setHeader("Content-Disposition", "attachment; filename=$objectName")
            if(range != null)
                response.setContentLength(getRangeContentSize(range))
            else
                response.setContentLengthLong(objectManager.getObjectFileSize(bucketName, objectName))
            response.contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE
            val outputStream = response.outputStream
            downloadManager.downloadToStream(bucketName, objectName, outputStream, future,range)
            future
        }
        catch(e: Exception){
            future.setResult(ResponseEntity(HttpStatus.NOT_FOUND))
            future
        }
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