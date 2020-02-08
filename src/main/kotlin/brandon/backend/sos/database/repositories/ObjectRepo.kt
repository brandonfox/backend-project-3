package brandon.backend.sos.database.repositories

import brandon.backend.sos.database.entities.FileObject
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ObjectRepo : JpaRepository<FileObject,Long> {


    //@Query("select case when count(f) > 0 then true else false end from FileObject f where f.bucket.name = :bucketName and f.name = :objName")
    fun existsByBucketNameAndName(@Param("bucketName")bucket: String, @Param("objName")name: String) : Boolean
    fun getByBucketNameAndName(bucketName: String, objectName: String): Optional<FileObject>

}