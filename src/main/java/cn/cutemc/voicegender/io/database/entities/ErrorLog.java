package cn.cutemc.voicegender.io.database.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "error_log")
@Data
public class ErrorLog {

    public ErrorLog() {
        this.path = "";
        this.uploadFileSize = "";
        this.analyzeStatus = "";
    }

    public ErrorLog(String time, String addr, String uuid, String path,  String uploadFileSize, String analyzeStatus, String exception) {
        this.time = time;
        this.addr = addr;
        this.uuid = uuid;
        this.uploadFileSize = uploadFileSize;
        this.analyzeStatus = analyzeStatus;
        this.exception = exception;
    }

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @Column(name = "time")
    private String time;

    @Column(name = "addr")
    private String addr;

    @Column(name = "uuid", nullable = false)
    private String uuid;

    @Column(name = "path")
    private String path;

    @Column(name = "upload_file_size", nullable = false)
    private String uploadFileSize;

    @Column(name = "analyze_status", nullable = false)
    private String analyzeStatus;

    @Column(name = "exception", length = 1500)
    private String exception;

}
