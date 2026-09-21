package SystemITR.JosueGuinea.modules.authentication.repository;

import SystemITR.JosueGuinea.modules.authentication.model.entity.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Long> {
    //Método personalizado para buscar usuarios
    Optional<UsuarioEntity> findByUsername(String username);
}
