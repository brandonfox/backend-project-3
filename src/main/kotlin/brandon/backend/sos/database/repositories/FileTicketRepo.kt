package brandon.backend.sos.database.repositories

import brandon.backend.sos.database.entities.FileUploadTicket
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface FileTicketRepo : JpaRepository<FileUploadTicket,Long> {
    fun existsByBucketNameNameAndName(bucketName: String, objectName: String): Boolean
    fun getByBucketNameNameAndName(bucketName: String,objectName: String): Optional<FileUploadTicket>
}