package brandon.backend.sos.database.repositories

import brandon.backend.sos.database.entities.FileObject
import brandon.backend.sos.database.entities.ObjectMetadataKey
import brandon.backend.sos.database.entities.ObjectMetadataPair
import org.springframework.data.jpa.repository.JpaRepository

interface ObjectMetadataRepo : JpaRepository<ObjectMetadataPair,ObjectMetadataKey> {

    fun findAllByObjectKeyPair_FileObject(fileObject: FileObject): List<ObjectMetadataPair>

}