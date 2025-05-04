package puiiiokiq.anicat.backend.anime.models;

public record ProfileSearchDto(
        Long id,
        String username,
        String nickname,
        String bio,
        String avatarId
) {}
