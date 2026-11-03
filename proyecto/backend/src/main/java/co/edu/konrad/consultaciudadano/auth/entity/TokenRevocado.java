package co.edu.konrad.consultaciudadano.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "token_revocado")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TokenRevocado {
    @Id
    @Column(length = 36)
    private String jti;

    @Column(name = "expira_en", nullable = false)
    private OffsetDateTime expiraEn;

    public TokenRevocado(String jti, OffsetDateTime expiraEn) {
        this.jti = jti;
        this.expiraEn = expiraEn;
    }
}
