package brandon.backend.sos.database.repositories

import brandon.backend.sos.database.entities.FileObject
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ObjectRepo : JpaRepository<FileObject,String> {
}