package brandon.backend.sos.filesystem

import brandon.backend.sos.database.entities.Bucket
import brandon.backend.sos.database.repositories.BucketRepo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.IOException
import java.lang.IllegalArgumentException

@Component
class BucketFileManager @Autowired constructor(
        val bucketRepo: BucketRepo
) : FileManager() {

    private fun bucketNameValid(bucketName: String): Boolean {
        return bucketName.matches("^[a-zA-Z0-9_\\-]*$".toRegex())
    }

    fun createBucket(bucketName: String): String{
        val bucket = getFile(bucketName)
        if(!bucketNameValid(bucketName)) throw IllegalArgumentException("$bucketName is an invalid name. Name must be alphanumeric or contain underscore or hyphens")
        if(bucket.exists()) throw IOException("Bucket $bucketName already exists")
        if(!bucket.mkdir()) throw IOException("Something happened while creating bucket $bucketName")
        val time = getEpochTimestamp()
        return setBucketMetadata(Bucket(bucketName,time,time))
    }

    fun bucketExists(bucketName: String): Boolean{
        return bucketRepo.existsById(bucketName)
    }

    fun deleteBucket(bucketName: String){
        val bucket = getFile(bucketName)
        if(!bucketExists(bucketName)) throw IOException("Bucket $bucketName does not exist")
        if(!bucket.deleteRecursively()) throw IOException("Something went wrong while deleting the bucket")
        bucketRepo.deleteById(bucketName)
    }

    fun setBucketMetadata(data: Bucket): String{
        bucketRepo.save(data)
        return objectMapper.writeValueAsString(data)
    }
    fun getBucketMetadata(bucketName: String): String{
        if(!bucketExists(bucketName)) throw IOException("Bucket $bucketName does not exist")
        val bucket = bucketRepo.findById(bucketName).get()
        return objectMapper.writeValueAsString(bucket)
    }

}