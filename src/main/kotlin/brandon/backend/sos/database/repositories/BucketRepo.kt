package brandon.backend.sos.database.repositories

import brandon.backend.sos.database.entities.Bucket
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BucketRepo : JpaRepository<Bucket,String> {

}