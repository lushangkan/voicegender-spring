package cn.cutemc.voicegender.io.database.repositories;

import cn.cutemc.voicegender.io.database.entities.AccessLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessRepository extends JpaRepository<AccessLog, Long> {

}
