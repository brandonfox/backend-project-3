package brandon.backend.sos.filesystem

import brandon.backend.sos.database.entities.FileObject
import brandon.backend.sos.database.repositories.ObjectRepo
import javassist.NotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class ObjectFileManager @Autowired constructor(
        val objectRepo: ObjectRepo
) : FileManager() {

    companion object{
        fun objectNameIsValid(objectName: String): Boolean {
            return objectName.matches("^[a-zA-Z0-9_\\-][a-zA-Z0-9_\\-.]*[a-zA-Z0-9_\\-]$".toRegex())
        }
    }

    fun objectExists(bucketName: String,objectName: String) : Boolean{
        return objectRepo.existsByBucketNameAndName(bucketName,objectName)
    }

    fun getObject(bucketName: String, objectName: String): FileObject {
        val obj = objectRepo.getByBucketNameAndName(bucketName, objectName)
        return if(obj.isPresent) obj.get() else throw IOException("$bucketName/$objectName not found/does not exist")
    }

    fun getObjectFileSize(bucketName: String,objectName: String): Long{
        val obj = objectRepo.getByBucketNameAndName(bucketName, objectName)
        return if(!obj.isPresent) throw NotFoundException("$bucketName/$objectName not found") else obj.get().length
    }

    fun deleteObject(bucketName: String,objectName: String){
        val obj = objectRepo.getByBucketNameAndName(bucketName, objectName)
        logger.info("Deleting object $bucketName/$objectName")
        if(obj.isPresent) objectRepo.delete(obj.get())
    }

}