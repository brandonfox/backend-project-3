package brandon.backend.sos.database.entities

import javax.persistence.*
import javax.validation.constraints.NotNull

@Entity
class ObjectMetadataPair constructor(

        @NotNull
        val metadataKey: String? = null,
        @NotNull
        val value: String? = null
) {

    @Id
    @GeneratedValue
    val id: Long? = null

}