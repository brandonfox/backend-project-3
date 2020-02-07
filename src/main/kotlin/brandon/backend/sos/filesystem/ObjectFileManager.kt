package brandon.backend.sos.filesystem

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class ObjectFileManager @Autowired constructor(
        val bucketManager: BucketFileManager
) : FileManager() {

    fun createUploadTicket(bucketName: String, objectName: String){
        if(!bucketManager.bucketExists(bucketName)) throw IOException("Bucket $bucketName does not exist")
        val objectFile = getFile("$bucketName/$objectName")
        if(!objectFile.mkdir()) throw IOException("Something went wrong when creating object $objectName in bucket $bucketName")

    }
}