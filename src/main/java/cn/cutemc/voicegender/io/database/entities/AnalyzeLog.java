package cn.cutemc.voicegender.io.database.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "analyze_log")
@Data
public class AnalyzeLog {

    public AnalyzeLog() {
        UA = "";
    }

    public AnalyzeLog(String time, String addr, String uuid, String uploadFileSize, String status, String UA) {
        this.time = time;
        this.addr = addr;
        this.uuid = uuid;
        this.uploadFileSize = uploadFileSize;
        this.status = status;
        this.UA = UA;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "time")
    private String time;

    @Column(name = "addr")
    private String addr;

    @Column(name = "uuid")
    private String uuid;

    @Column(name = "upload_file_size")
    private String uploadFileSize;

    @Column(name = "status")
    private String status;

    @Column(name = "ua", nullable = false)
    private String UA;

}
