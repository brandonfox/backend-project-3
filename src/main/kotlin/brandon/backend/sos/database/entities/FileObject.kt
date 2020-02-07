package brandon.backend.sos.database.entities

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonManagedReference
import javax.persistence.*
import javax.validation.constraints.NotNull

@Entity
class FileObject constructor(
        @NotNull
        @Column(name="file_name")
        val name: String? = null,
        @NotNull
        val eTag: String? = null,
        @NotNull
        val created: Long? = null,
        @NotNull
        val modified: Long? = null,
        @ManyToOne(fetch = FetchType.LAZY)
        @NotNull
        @JsonBackReference
        var bucket: Bucket? = null,
        @NotNull
        val uploaded: Boolean = false,
        @NotNull
        val parts: Int = 0
) {
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        val uuid: Long? = null
}