package brandon.backend.sos.database.entities

import javax.persistence.*
import javax.validation.constraints.NotNull

@Entity
class FileUploadTicket constructor(
        @NotNull
        val name: String? = null,
        @NotNull
        @ManyToOne
        val bucketName: Bucket? = null,
        @NotNull
        var noParts: Int = 0
) {

    @Id
    @GeneratedValue
    val ticketId: Long? = null

}