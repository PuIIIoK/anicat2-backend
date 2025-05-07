package puiiiokiq.anicat.backend.admin.Controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import puiiiokiq.anicat.backend.admin.SiteLogService;
import puiiiokiq.anicat.backend.admin.models.SiteLog;

import java.util.List;

@RestController
@RequestMapping("/api/admin/logs")
@RequiredArgsConstructor
public class AdminLogController {

    private final SiteLogService logService;

    @GetMapping
    public List<SiteLog> getFilteredLogs(
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String user,
            @RequestParam(required = false) String target
    ) {
        return logService.getFilteredLogs(limit, action, user, target);
    }
}