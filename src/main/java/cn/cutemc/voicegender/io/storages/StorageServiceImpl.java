package cn.cutemc.voicegender.io.storages;

import cn.cutemc.voicegender.core.configs.MainConfig;
import cn.cutemc.voicegender.io.caches.TmpFileCache;
import cn.cutemc.voicegender.io.caches.UploadFileCache;
import cn.cutemc.voicegender.io.storages.bean.StorageFile;
import jakarta.annotation.PreDestroy;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@CommonsLog
public class StorageServiceImpl implements StorageService {

    private final StorageProperties properties;

    private final File uploadDir;

    private final File tmpDir;

    private final UploadFileCache uploadFileCache;

    private final TmpFileCache tmpFileCache;

    private final ScheduledExecutorService executorService;

    private final MainConfig config;

    public StorageServiceImpl(StorageProperties properties, UploadFileCache uploadFileCache, TmpFileCache tmpFileCache, ScheduledExecutorService executorService, MainConfig config) {
        this.properties = properties;
        this.uploadDir = new File(config.getUploadPath());
        this.tmpDir = new File(config.getTmpPath());
        this.uploadFileCache = uploadFileCache;
        this.tmpFileCache = tmpFileCache;
        this.executorService = executorService;
        this.config = config;

        init();
    }

    /**
     * 初始化文件夹
     */
    @Override
    public void init() {
        //新建文件夹
        try {
            if (tmpDir.exists() || !tmpDir.isDirectory()) FileUtils.deleteDirectory(tmpDir);
            if (uploadDir.exists() || !uploadDir.isDirectory()) FileUtils.deleteDirectory(uploadDir);
            FileUtils.createParentDirectories(tmpDir);
            Files.createDirectory(tmpDir.toPath());
            FileUtils.createParentDirectories(uploadDir);
            Files.createDirectory(uploadDir.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 保存文件
     * @param multipartFile 文件
     */
    @Override
    public void store(Path path, MultipartFile multipartFile, UUID fileUUID) {
        try {
            if (multipartFile.isEmpty()) {
                log.error("Could not save file " + multipartFile.getOriginalFilename() + ", UUID: " + fileUUID);
                throw new RuntimeException();
            }

            File dir = path.toFile();
            File newFile = new File(dir, fileUUID.toString()+ "." + Objects.requireNonNull(multipartFile.getOriginalFilename()).split("\\.")[1]);

            Files.copy(multipartFile.getInputStream(), newFile.toPath());

            if (path.equals(Path.of(config.getUploadPath()))) {
                uploadFileCache.update(new StorageFile(fileUUID, newFile.getName(), newFile));
            } else if (path.equals(Path.of(config.getTmpPath()))) {
                tmpFileCache.update(new StorageFile(fileUUID, newFile.getName(), newFile));
            } else {
                log.error("Could not save file " + multipartFile.getOriginalFilename() + ", UUID: " + fileUUID);
                throw new RuntimeException();
            }

            //定时删除
            executorService.schedule(() -> {
                if (path.equals(Path.of(config.getUploadPath()))) {
                    uploadFileCache.delete(fileUUID);
                } else if (path.equals(Path.of(config.getTmpPath()))) {
                    tmpFileCache.delete(fileUUID);
                } else {
                    log.error("Could not delete file " + multipartFile.getOriginalFilename() + ", UUID: " + fileUUID);
                    throw new RuntimeException();
                }
                newFile.delete();
            }, config.getCleanInterval(), TimeUnit.MINUTES);
        } catch (IOException e) {
            log.error("Could not save file " + multipartFile.getOriginalFilename() + ", UUID: " + fileUUID, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 记录文件到Cache
     * @param path 文件夹路径
     * @param file 文件
     * @param fileUUID uuid
     */
    @Override
    public void record(Path path, Path file, UUID fileUUID) {
        if (path.equals(Path.of(config.getUploadPath()))) {
            uploadFileCache.update(new StorageFile(fileUUID, file.getFileName().toString(), file.toFile()));
        } else if (path.equals(Path.of(config.getTmpPath()))) {
            tmpFileCache.update(new StorageFile(fileUUID, file.getFileName().toString(), file.toFile()));
        } else {
            log.error("Could not record file, UUID: " + fileUUID.toString());
            throw new RuntimeException();
        }
    }

    /**
     * 加载文件
     * @param path 文件夹路径
     * @param fileUUID 文件UUID
     * @return 文件路径
     */
    @Override
    public Path load(Path path, UUID fileUUID) {
        if (path.equals(Path.of(config.getUploadPath()))) {
            if (uploadFileCache.getByKey(fileUUID) == null) {
                log.error("Could not load file, UUID: " + fileUUID.toString());
                throw new RuntimeException();
            }
            return uploadFileCache.getByKey(fileUUID).file().toPath();
        } else if (path.equals(Path.of(config.getTmpPath()))) {
            if (tmpFileCache.getByKey(fileUUID) == null) {
                log.error("Could not load file, UUID: " + fileUUID.toString());
                throw new RuntimeException();
            }
            return tmpFileCache.getByKey(fileUUID).file().toPath();
        } else {
            log.error("Could not load file, UUID: " + fileUUID.toString());
            throw new RuntimeException();
        }
    }

    /**
     * 加载文件
     * @param path 文件夹路径
     * @param fileUUID 文件UUID
     * @return 文件资源
     */
    @Override
    public Resource loadAsResource(Path path, UUID fileUUID) {
        Path filePath = load(path, fileUUID);
        return new PathResource(filePath);
    }

    /**
     * 删除文件
     * @param path 文件夹路径
     * @param fileUUID 文件UUID
     */
    @Override
    public void delete(Path path, UUID fileUUID) {
        if (path.equals(Path.of(config.getUploadPath()))) {
            if (uploadFileCache.getByKey(fileUUID) == null) {
                log.error("Unable to delete file, UUID: " + fileUUID.toString());
                throw new RuntimeException("Unable to delete file, UUID: " + fileUUID);
            }
            File file = uploadFileCache.getByKey(fileUUID).file();
            if (file.exists()) file.delete();
            uploadFileCache.delete(fileUUID);
        } else if (path.equals(Path.of(config.getTmpPath()))) {
            if (tmpFileCache.getByKey(fileUUID) == null) {
                log.error("Unable to delete file, UUID: " + fileUUID.toString());
                throw new RuntimeException();
            }
            File file = tmpFileCache.getByKey(fileUUID).file();
            if (file.exists()) file.delete();
            tmpFileCache.delete(fileUUID);
        } else {
            log.error("Unable to delete file, UUID: " + fileUUID.toString());
            throw new RuntimeException();
        }
    }

    /**
     * 删除所有文件
     */
    @Override
    @PreDestroy
    public void deleteAll() {
        uploadFileCache.deleteAll();
        tmpFileCache.deleteAll();
        Stream.of(uploadDir.listFiles(), tmpDir.listFiles()).flatMap(Stream::of).forEach(File::delete);
    }
}
