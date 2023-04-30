package cn.cutemc.voicegender.io.storages.bean;

import lombok.NonNull;

import java.io.File;
import java.util.UUID;

public record StorageFile(@NonNull UUID analyzeUUID, @NonNull String fileName, @NonNull File file) {
}
