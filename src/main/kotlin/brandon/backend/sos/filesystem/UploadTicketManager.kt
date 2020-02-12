package brandon.backend.sos.filesystem

import brandon.backend.sos.database.entities.FileObject
import brandon.backend.sos.database.entities.FileUploadTicket
import brandon.backend.sos.database.repositories.BucketRepo
import brandon.backend.sos.database.repositories.FilePartRepo
import brandon.backend.sos.database.repositories.FileTicketRepo
import brandon.backend.sos.database.repositories.ObjectRepo
import brandon.backend.sos.filesystem.ObjectDownloadManager.Companion.getUploadFile
import brandon.backend.sos.md5.Md5Hasher.getMd5Digest
import brandon.backend.sos.md5.Md5Hasher.getMd5Hash
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class UploadTicketManager @Autowired constructor(
        val fileTicketRepo: FileTicketRepo,
        val bucketRepo: BucketRepo,
        val bucketManager: BucketFileManager,
        val filePartRepo: FilePartRepo,
        val objectRepo: ObjectRepo,
        val objectManager: ObjectFileManager
): FileManager(){

    fun uploadTicketExists(bucketName: String, objectName: String) : Boolean{
        return fileTicketRepo.existsByBucketNameNameAndName(bucketName,objectName)
    }

    fun getUploadTicket(bucketName: String,objectName: String): FileUploadTicket {
        val ticket = fileTicketRepo.getByBucketNameNameAndName(bucketName, objectName)
        if(!ticket.isPresent) throw IOException("No ticket open for $bucketName/$objectName")
        return ticket.get()
    }

    fun saveAndFlush(ticket: FileUploadTicket){
        fileTicketRepo.saveAndFlush(ticket)
    }

    fun completeTicket(bucketName: String,objectName: String): FileObject {
        val ticket = getUploadTicket(bucketName, objectName)
        val parts = filePartRepo.findAllByUploadTicket(ticket)
        if(parts.size != ticket.noParts) throw IOException("Upload has not yet completed: Parts uploaded: ${parts.size}, Expected parts: ${ticket.noParts}")
        val file = getFile("$bucketName/$objectName")
        if(file.exists()) throw IOException("File ${FileManager.filePath}/$bucketName/$objectName already exists")
        val md = getMd5Digest()
        var totalSize: Long = 0
        parts.forEach {
            md.digest(it.md5!!.toByteArray())
            totalSize += it.contentSize!!
        }
        val chksum = getMd5Hash(md)
        val etag = "$chksum-${ticket.noParts}"
        val time = getEpochTimestamp()
        val fileObject = FileObject(objectName,etag,time,time,bucketRepo.findById(bucketName).get(),ticket.noParts,totalSize)
        objectRepo.save(fileObject)
        logger.info("Completed ticket for $bucketName/$objectName with ${ticket.noParts} parts")
        return fileObject
    }

    fun createUploadTicket(bucketName: String, objectName: String){
        if(!bucketManager.bucketExists(bucketName)) throw IOException("Bucket $bucketName does not exist")
        if(objectManager.objectExists(bucketName,objectName)) throw IOException("Object $objectName already exists in bucket $bucketName")
        if(uploadTicketExists(bucketName, objectName)) throw IOException("An upload ticket is already open for $bucketName/$objectName")
        val objectFile = getUploadFile(bucketName, objectName)
        if(!objectFile.mkdir()) throw IOException("Something went wrong when creating object $objectName in bucket $bucketName")
        val bucket = bucketRepo.findById(bucketName).get()
        val ticket = FileUploadTicket(objectName,bucket)
        logger.info("Created upload ticket for $bucketName/$objectName")
        fileTicketRepo.save(ticket)
    }

}