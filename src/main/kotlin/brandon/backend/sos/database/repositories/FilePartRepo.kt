package brandon.backend.sos.database.repositories

import brandon.backend.sos.database.entities.FilePart
import brandon.backend.sos.database.entities.FileUploadTicket
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface FilePartRepo: JpaRepository<FilePart,String> {

    fun findAllByUploadTicket(uploadTicket: FileUploadTicket): List<FilePart>
    fun getByUploadTicketAndPartId(uploadTicket: FileUploadTicket, partId: String): Optional<FilePart>

}