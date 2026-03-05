package pharmacie.entity;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Getter @Setter @NoArgsConstructor @RequiredArgsConstructor @ToString
public class Fournisseur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Integer id;

    @NonNull
    @NotBlank
    @Column(unique = true, length = 255)
    private String nom;

    @NonNull
    @Email
    @NotBlank
    @Column(unique = true, length = 255)
    private String email;

    @ToString.Exclude
    @JsonIgnore
    @ManyToMany(mappedBy = "fournisseurs")
    private Set<Categorie> categories = new HashSet<>();

}
