package cn.cutemc.voicegender.io.database.repositories;

import cn.cutemc.voicegender.io.database.entities.ErrorLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ErrorRepository extends JpaRepository<ErrorLog, Long> {
}
