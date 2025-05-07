package puiiiokiq.anicat.backend.admin.Repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import puiiiokiq.anicat.backend.admin.models.SiteLog;

import java.util.List;


@Repository
public interface SiteLogRepository extends JpaRepository<SiteLog, Long> {
    List<SiteLog> findTop1000ByOrderByTimestampDesc();

}