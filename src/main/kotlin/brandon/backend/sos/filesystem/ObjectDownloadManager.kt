package brandon.backend.sos.filesystem

import brandon.backend.sos.database.entities.FilePart
import brandon.backend.sos.database.repositories.FilePartRepo
import brandon.backend.sos.filesystem.IO.StoredFileInputStream
import brandon.backend.sos.md5.Md5Hasher.getMd5Digest
import brandon.backend.sos.md5.Md5Hasher.getMd5Hash
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.IOException

@Component
class ObjectDownloadManager @Autowired constructor(
        val objectManager: ObjectFileManager,
        val filePartRepo: FilePartRepo,
        val fileTicketManager: UploadTicketManager
) : FileManager(){

    companion object{
        fun getUploadFile(bucketName: String, objectName: String,path: String = ""): File {
            val dot = objectName.lastIndexOf('.')
            return if(dot < 0) File("$filePath/$bucketName/.$objectName$path")
            else File("$filePath/$bucketName/.${objectName.substring(0,dot)}-${objectName.substring(dot)}$path")
        }
    }
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


    fun putPart(bucketName: String, objectName: String, partNo: Int, data: MultipartFile, md5: String, size: Long): String{
        val ticket = fileTicketManager.getUploadTicket(bucketName, objectName)
        val md = getMd5Digest()
        val partName = getFilePartId(bucketName,objectName,partNo)
        val file = getUploadFile(bucketName,objectName,"/$partName")
        if(file.exists()) file.delete()
        val input = data.inputStream.buffered()
        while(input.available() > 0){
            val bytes = input.readBytes()
            md.update(bytes)
            file.writeBytes(bytes)
        }
        val chksum = getMd5Hash(md)
        logger.info("Computed checksum for $bucketName/$objectName: $chksum, Given checksum: $md5")
        if(chksum != md5) throw IOException("Invalid Checksum")
        val fileParts = FilePart(partName,ticket,md5,size)
        filePartRepo.save(fileParts)
        if(ticket.noParts < partNo){
            ticket.noParts = partNo
            fileTicketManager.saveAndFlush(ticket)
        }
        logger.info("Completed part $partNo upload for $bucketName/$objectName")
        return chksum
    }
}