package brandon.backend.sos.database.entities

import com.fasterxml.jackson.annotation.JsonBackReference
import java.io.Serializable
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
        val parts: Int = 0,
        @NotNull
        val length: Long = 0,
        @OneToMany(cascade = [CascadeType.ALL])
        @JsonBackReference
        var metadata: Set<ObjectMetadataPair> = HashSet()
) : Serializable {
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        val uuid: Long? = null
}