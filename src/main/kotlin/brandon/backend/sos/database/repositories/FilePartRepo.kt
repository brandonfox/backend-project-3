package brandon.backend.sos.database.repositories

import brandon.backend.sos.database.entities.FilePart
import brandon.backend.sos.database.entities.FileUploadTicket
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FilePartRepo: JpaRepository<FilePart,String> {

    fun findAllByUploadTicket(uploadTicket: FileUploadTicket): List<FilePart>

}