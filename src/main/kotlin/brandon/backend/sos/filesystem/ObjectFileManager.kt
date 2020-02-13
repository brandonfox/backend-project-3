package brandon.backend.sos.filesystem

import brandon.backend.sos.database.entities.FileObject
import brandon.backend.sos.database.entities.ObjectMetadataKey
import brandon.backend.sos.database.entities.ObjectMetadataPair
import brandon.backend.sos.database.repositories.ObjectMetadataRepo
import brandon.backend.sos.database.repositories.ObjectRepo
import brandon.backend.sos.filesystem.errors.MetadataNotFoundException
import javassist.NotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class ObjectFileManager @Autowired constructor(
        val objectRepo: ObjectRepo,
        val metaDataRepo: ObjectMetadataRepo
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

    fun getMetadataKey(bucketName: String,objectName: String,key: String): ObjectMetadataPair{
        val fileObject = getObject(bucketName, objectName)
        val data = metaDataRepo.findById(ObjectMetadataKey(fileObject, key))
        return if(data.isPresent) data.get() else throw MetadataNotFoundException()
    }

    fun getAllMetadata(bucketName: String,objectName: String): List<ObjectMetadataPair>{
        val fileObject = getObject(bucketName, objectName)
        return metaDataRepo.findAllByObjectKeyPair_FileObject(fileObject)
    }

    fun getAllMetadataJson(bucketName: String, objectName: String): String {
        val data = getAllMetadata(bucketName, objectName)
        val jsonNode = objectMapper.createObjectNode()
        data.forEach {
            jsonNode.put(it.objectKeyPair!!.metadataKey,it.value)
        }
        return jsonNode.toPrettyString()
    }

    fun getObjectFileSize(bucketName: String,objectName: String): Long{
        val obj = objectRepo.getByBucketNameAndName(bucketName, objectName)
        return if(!obj.isPresent) throw NotFoundException("$bucketName/$objectName not found") else obj.get().length
    }

    fun setMetaData(bucketName: String,objectName: String,key: String, value: String){
        val fileObject = getObject(bucketName, objectName)
        val metaData = ObjectMetadataPair(fileObject,key,value)
        metaDataRepo.saveAndFlush(metaData)
    }

    fun deleteMetadataByKey(bucketName: String, objectName: String, key: String){
        val data = getMetadataKey(bucketName, objectName, key)
        metaDataRepo.delete(data)
    }

    fun deleteObject(bucketName: String,objectName: String){
        val obj = objectRepo.getByBucketNameAndName(bucketName, objectName)
        logger.info("Deleting object $bucketName/$objectName")
        if(obj.isPresent) objectRepo.delete(obj.get())
    }

}