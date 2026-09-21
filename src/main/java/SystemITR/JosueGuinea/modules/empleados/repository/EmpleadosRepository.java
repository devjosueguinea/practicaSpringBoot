package SystemITR.JosueGuinea.modules.empleados.repository;

import SystemITR.JosueGuinea.modules.empleados.entity.EmpleadosEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmpleadosRepository extends JpaRepository<EmpleadosEntity, Long> {
    boolean existsByEmail(String email);
    Page<EmpleadosEntity> findAll(Pageable pageable);
}
