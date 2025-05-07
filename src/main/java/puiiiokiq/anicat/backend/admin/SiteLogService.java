package puiiiokiq.anicat.backend.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import puiiiokiq.anicat.backend.admin.Repository.SiteLogRepository;
import puiiiokiq.anicat.backend.admin.models.SiteLog;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SiteLogService {

    private final SiteLogRepository logRepository;

    public void log(String action, String target, String performedBy) {
        System.out.println("⚠️ Сохраняю лог: " + action + " | " + target + " | " + performedBy);

        SiteLog log = SiteLog.builder()
                .action(action)
                .target(target)
                .performedBy(performedBy)
                .timestamp(LocalDateTime.now())
                .build();
        logRepository.save(log);
    }

    public List<SiteLog> getFilteredLogs(int limit, String action, String user, String target) {
        return logRepository.findTop1000ByOrderByTimestampDesc().stream()
                .filter(log -> action == null || log.getAction().toLowerCase().contains(action.toLowerCase()))
                .filter(log -> user == null || log.getPerformedBy().toLowerCase().contains(user.toLowerCase()))
                .filter(log -> target == null || log.getTarget().toLowerCase().contains(target.toLowerCase()))
                .limit(limit)
                .collect(Collectors.toList());
    }
}