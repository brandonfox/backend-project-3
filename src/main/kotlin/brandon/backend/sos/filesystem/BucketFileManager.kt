package brandon.backend.sos.filesystem

import java.io.IOException

object BucketFileManager : FileManager() {

    fun createBucket(bucketName: String): String{
        val bucket = getFile(bucketName)
        if(bucket.exists()) throw IOException("Bucket $bucketName already exists")
        else if(!bucket.mkdir()) throw IOException("Something happened while creating bucket $bucketName")
        else{
            val time = getEpochTimestamp()
            val obj = object {
                val created = time
                val modified = time
                val name = bucketName
            }
            return setBucketMetadata(bucketName, obj)
        }
    }

    fun bucketExists(bucketName: String): Boolean{
        return getFile(bucketName).exists()
    }

    fun deleteBucket(bucketName: String){
        val bucket = getFile(bucketName)
        if(!bucket.exists()) throw IOException("Bucket $bucketName does not exist")
        if(!bucket.deleteRecursively()) throw IOException("Something went wrong while deleting the bucket")
    }

    fun setBucketMetadata(bucketName: String,data: Any): String{
        objectMapper.writeValue(getFile("$bucketName/metadata.json"),data)
        return objectMapper.writeValueAsString(data)
    }
    fun getBucketMetadata(bucketName: String): String{
        val metadata = getFile("$bucketName/metadata.json")
        if(!metadata.exists()) throw IOException("File $filePath/$bucketName/metadata.json not found")
        else return metadata.readText()
    }

}