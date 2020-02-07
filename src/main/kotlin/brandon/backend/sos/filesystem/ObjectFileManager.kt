package brandon.backend.sos.filesystem

import java.io.IOException

object ObjectFileManager : FileManager() {

    fun createUploadTicket(bucketName: String, objectName: String){
        if(!BucketFileManager.bucketExists(bucketName)) throw IOException("Bucket $bucketName does not exist")
        val objectFile = getFile("$bucketName/$objectName")
        if(!objectFile.mkdir()) throw IOException("Something went wrong when creating object $objectName in bucket $bucketName")

    }

}