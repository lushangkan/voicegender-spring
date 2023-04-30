package cn.cutemc.voicegender.io.storages;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.UUID;

public interface StorageService {
    void init();

    void store(Path path, MultipartFile file, UUID fileUUID);

    void record(Path path, Path file, UUID fileUUID);

    Path load(Path path, UUID fileUUID);

    Resource loadAsResource(Path Path, UUID fileUUID);

    void delete(Path Path, UUID fileUUID);

    void deleteAll();
}
