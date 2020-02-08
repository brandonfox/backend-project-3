package brandon.backend.sos.database.entities

import java.io.Serializable
import javax.persistence.*
import javax.validation.constraints.NotNull

@Entity
class FilePart constructor(
        @Id
        val partId: String? = null,
        @NotNull
        @ManyToOne
        val uploadTicket: FileUploadTicket? = null,
        @NotNull
        val md5: String? = null,
        @NotNull
        val contentSize: Long? = null
) : Serializable {
}