package brandon.backend.sos.database.entities

import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.validation.constraints.NotNull

@Entity
class Bucket constructor(
        @Id
        val name: String? = null,
        @NotNull
        val created: Long? = null,
        @NotNull
        val modified: Long? = null,
        @OneToMany
        val objects: Set<FileObject> = HashSet()
) {



}