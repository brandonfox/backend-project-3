package brandon.backend.sos.filesystem

import brandon.backend.sos.database.entities.FilePart
import brandon.backend.sos.database.repositories.FilePartRepo
import brandon.backend.sos.filesystem.io.StoredFileInputStream
import brandon.backend.sos.rest.async.AsyncController
import brandon.backend.sos.rest.async.DownloadRequest
import brandon.backend.sos.rest.async.UploadRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.context.request.async.DeferredResult
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.io.OutputStream

@Component
class ObjectDownloadManager @Autowired constructor(
        val objectManager: ObjectFileManager,
        val filePartRepo: FilePartRepo,
        val fileTicketManager: UploadTicketManager
) : FileManager(){

    private fun getFilePartId(bucketName: String,objectName: String,partNo: Int): String {
        return "$bucketName-$objectName-$partNo.bin"
    }

    private fun getPart(bucketName: String,objectName: String,partNo: Int): FilePart {
        val ticket = fileTicketManager.getUploadTicket(bucketName, objectName)
        val name = getFilePartId(bucketName, objectName, partNo)
        val part = filePartRepo.getByUploadTicketAndPartId(ticket,name)
        return if(part.isPresent) part.get() else throw IOException("$bucketName/$objectName does not contain a part number $partNo")
    }

    fun deletePart(bucketName: String,objectName: String,partNo: Int){
        val part = getPart(bucketName, objectName, partNo)
        logger.info("Deleted part $partNo for $bucketName/$objectName")
        filePartRepo.delete(part)
    }

    fun downloadObject(bucketName: String, objectName: String): StoredFileInputStream {
        val obj = objectManager.getObject(bucketName, objectName)
        val filePaths = sequence {
            for(i in 1..obj.parts){
                val folderPath = getUploadFile(bucketName, objectName).path
                val suffix = getFilePartId(bucketName,objectName,i)
                yield("$folderPath/$suffix")
            }
        }.toList()
        return StoredFileInputStream(filePaths)
    }

    fun downloadToStream(bucketName: String, objectName: String, stream: OutputStream, future: DeferredResult<ResponseEntity<Any>>){
        val inputStream = downloadObject(bucketName, objectName)
        val request = DownloadRequest(inputStream,stream,future)
        AsyncController.addRequest(request)
    }


    fun putPart(bucketName: String, objectName: String, partNo: Int, data: MultipartFile, md5: String, size: Long, result: DeferredResult<ResponseEntity<Any>>){
        val ticket = fileTicketManager.getUploadTicket(bucketName, objectName)
        val partName = getFilePartId(bucketName,objectName,partNo)
        val file = getUploadFile(bucketName,objectName,"/$partName")
        if(file.exists()) file.delete()
        val input = data.inputStream
        val req = UploadRequest(input,file,fileTicketManager,ticket,partName,md5,partNo,size,result)
        AsyncController.addRequest(req)
    }
}