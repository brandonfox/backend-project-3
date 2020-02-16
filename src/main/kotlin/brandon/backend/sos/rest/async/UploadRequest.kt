package brandon.backend.sos.rest.async

import brandon.backend.sos.database.entities.FilePart
import brandon.backend.sos.database.entities.FileUploadTicket
import brandon.backend.sos.filesystem.UploadTicketManager
import brandon.backend.sos.md5.Md5Hasher.getMd5Digest
import brandon.backend.sos.md5.Md5Hasher.getMd5Hash
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.context.request.async.DeferredResult
import java.io.File
import java.io.InputStream

class UploadRequest constructor(
        private val input: InputStream,
        file: File,
        private val fileTicketManager: UploadTicketManager,
        private val ticket: FileUploadTicket,
        private val partName: String,
        private val chksum: String,
        private val partNo: Int,
        private val size: Long,
        private val result: DeferredResult<ResponseEntity<Any>>
): Request() {

    private val md5 = getMd5Digest()

    private val logger = LoggerFactory.getLogger("Upload request: $partName")

    private val fileStream = file.outputStream()

    override fun hasMoreData(): Boolean {
        return input.available() > 0
    }

    override fun readData(): ByteArray {
        input.read(byteArray)
        md5.digest(byteArray)
        return byteArray
    }

    override fun writeData(data: ByteArray) {
        fileStream.write(data)
    }

    override fun completeRequest(status: HttpStatus) {
        val chkmd5 = getMd5Hash(md5)
        if(chksum != chkmd5){
            val retObj = object{
                val md5 = chkmd5
                val length = size
                val partNumber = partNo
                val error = "Invalid checksum"
            }
            result.setResult(ResponseEntity(retObj,HttpStatus.BAD_REQUEST))
        }
        val fileParts = FilePart(partName,ticket,chkmd5,size)
        fileTicketManager.addPart(fileParts,ticket,partNo)

        logger.info("Completed part $partNo upload for $partName")
        val retObj = object{
            val md5 = chkmd5
            val length = size
            val partNumber = partNo
        }
        result.setResult(ResponseEntity(retObj,HttpStatus.OK))
    }
}