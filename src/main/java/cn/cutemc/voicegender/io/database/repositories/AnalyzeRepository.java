package cn.cutemc.voicegender.io.database.repositories;

import cn.cutemc.voicegender.io.database.entities.AnalyzeLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalyzeRepository extends JpaRepository<AnalyzeLog, Long> {

}
