package cn.cutemc.voicegender.dataset.records;

import java.io.File;

public record MagicDataContact(File output, String startTime, String endTime, String speaker, String gender, String contactText) {
}
