package brandon.backend.sos.filesystem

import brandon.backend.sos.database.entities.FileObject
import brandon.backend.sos.database.repositories.BucketRepo
import brandon.backend.sos.database.repositories.ObjectRepo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class ObjectFileManager @Autowired constructor(
        val bucketManager: BucketFileManager,
        val bucketRepo: BucketRepo,
        val objectRepo: ObjectRepo
) : FileManager() {

    fun createUploadTicket(bucketName: String, objectName: String){
        if(!bucketManager.bucketExists(bucketName)) throw IOException("Bucket $bucketName does not exist")
        if(objectRepo.existsByBucketNameAndName(bucketName,objectName)){
            println("Detected that object already exists")
            throw IOException("Object $objectName already exists in bucket $bucketName")
        }
        val objectFile = getFile("$bucketName/$objectName")
        if(!objectFile.mkdir()) throw IOException("Something went wrong when creating object $objectName in bucket $bucketName")
        val time = getEpochTimestamp()
        val bucket = bucketRepo.findById(bucketName).get()
        val obj = FileObject(objectName,"N/A",time,time,bucket,false)
        objectRepo.save(obj)
        //TODO Move this to the complete function
//        bucket.objects = bucket.objects.plus(obj)
//        bucketRepo.saveAndFlush(bucket)
    }
}