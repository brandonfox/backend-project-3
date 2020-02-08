package brandon.backend.sos.filesystem

import brandon.backend.sos.database.entities.FileObject
import brandon.backend.sos.database.entities.FilePart
import brandon.backend.sos.database.entities.FileUploadTicket
import brandon.backend.sos.database.repositories.BucketRepo
import brandon.backend.sos.database.repositories.FilePartRepo
import brandon.backend.sos.database.repositories.FileTicketRepo
import brandon.backend.sos.database.repositories.ObjectRepo
import javassist.NotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.IOException
import java.security.MessageDigest
import javax.xml.bind.DatatypeConverter

@Component
class ObjectFileManager @Autowired constructor(
        val bucketManager: BucketFileManager,
        val bucketRepo: BucketRepo,
        val objectRepo: ObjectRepo,
        val filePartRepo: FilePartRepo,
        val fileTicketRepo: FileTicketRepo
) : FileManager() {

    fun objectExists(bucketName: String,objectName: String) : Boolean{
        return objectRepo.existsByBucketNameAndName(bucketName,objectName)
    }

    fun uploadTicketExists(bucketName: String, objectName: String) : Boolean{
        return fileTicketRepo.existsByBucketNameNameAndName(bucketName,objectName)
    }

    fun getUploadTicket(bucketName: String,objectName: String): FileUploadTicket {
        val ticket = fileTicketRepo.getByBucketNameNameAndName(bucketName, objectName)
        if(!ticket.isPresent) throw IOException("No ticket open for $bucketName/$objectName")
        return ticket.get()
    }

    fun getMd5Digest(): MessageDigest{
        return MessageDigest.getInstance("MD5")
    }

    fun getMd5Hash(digest: MessageDigest): String{
        val mdigest = digest.digest()
        return DatatypeConverter.printHexBinary(mdigest).toUpperCase()
    }

    fun getObjectPartSuffix(bucketName: String,objectName: String,partNo: Int): String{
        return "$bucketName-$objectName-$partNo.bin"
    }

    fun getUploadFile(bucketName: String, objectName: String,path: String = ""): File{
        val dot = objectName.lastIndexOf('.')
        return if(dot < 0) File("$filePath/$bucketName/.$objectName$path")
        else File("$filePath/$bucketName/.${objectName.substring(0,dot)}-${objectName.substring(dot)}$path")
    }

    fun createUploadTicket(bucketName: String, objectName: String){
        if(!bucketManager.bucketExists(bucketName)) throw IOException("Bucket $bucketName does not exist")
        if(objectExists(bucketName,objectName)) throw IOException("Object $objectName already exists in bucket $bucketName")
        if(uploadTicketExists(bucketName, objectName)) throw IOException("An upload ticket is already open for $bucketName/$objectName")

        val objectFile = getUploadFile(bucketName, objectName)
        if(!objectFile.mkdir()) throw IOException("Something went wrong when creating object $objectName in bucket $bucketName")
        val bucket = bucketRepo.findById(bucketName).get()
        val ticket = FileUploadTicket(objectName,bucket)
        fileTicketRepo.save(ticket)
    }

    fun putPart(bucketName: String,objectName: String,partNo: Int, data: MultipartFile, md5: String, size: Long): String{
        val ticket = getUploadTicket(bucketName, objectName)
        val md = getMd5Digest()
        val partName = getObjectPartSuffix(bucketName,objectName,partNo)
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
            fileTicketRepo.saveAndFlush(ticket)
        }
        return chksum
    }

    fun getObjectFileSize(bucketName: String,objectName: String): Long{
        val obj = objectRepo.getByBucketNameAndName(bucketName, objectName)
        if(!obj.isPresent) throw NotFoundException("$bucketName/$objectName not found")
        return obj.get().length
    }

    fun completeTicket(bucketName: String,objectName: String): FileObject{
        val ticket = getUploadTicket(bucketName, objectName)
        val parts = filePartRepo.findAllByUploadTicket(ticket)
        if(parts.size != ticket.noParts) throw IOException("Upload has not yet completed: Parts uploaded: ${parts.size}, Expected parts: ${ticket.noParts}")
        val file = getFile("$bucketName/$objectName")
        if(file.exists()) throw IOException("File $filePath/$bucketName/$objectName already exists")
        val md = getMd5Digest()
        var totalSize: Long = 0
        parts.forEach {
            md.digest(it.md5!!.toByteArray())
            totalSize += it.contentSize!!
        }
        val chksum = getMd5Hash(md)
        val etag = "$chksum-${ticket.noParts}"
        val time = getEpochTimestamp()
        //TODO Put in proper length
        val fileObject = FileObject(objectName,etag,time,time,bucketRepo.findById(bucketName).get(),ticket.noParts,totalSize)
        objectRepo.save(fileObject)
        return fileObject
    }

    fun deletePart(bucketName: String,objectName: String,partNo: Int){

    }

    fun downloadObject(bucketName: String, objectName: String): StoredFileInputStream{
        if(!objectExists(bucketName, objectName)) throw NotFoundException("$bucketName/$objectName not found")
        val obj = objectRepo.getByBucketNameAndName(bucketName,objectName).get()
        val filePaths = sequence {
            for(i in 1..obj.parts){
                val folderPath = getUploadFile(bucketName, objectName).path
                val suffix = getObjectPartSuffix(bucketName,objectName,i)
                yield("$folderPath/$suffix")
            }
        }.toList()
        return StoredFileInputStream(filePaths)
    }
}