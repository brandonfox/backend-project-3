package brandon.backend.sos.database.entities

import javax.persistence.Entity
import javax.persistence.Id
import javax.validation.constraints.NotNull

@Entity
class FileObject constructor(
        @Id
        val name: String? = null,
        @NotNull
        val eTag: String? = null,
        @NotNull
        val created: Long? = null,
        @NotNull
        val modified: Long? = null
) {
}