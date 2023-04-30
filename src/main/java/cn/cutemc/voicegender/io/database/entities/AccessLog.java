package cn.cutemc.voicegender.io.database.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "access_log")
@Data
public class AccessLog {

    public AccessLog() {
        UA = "";
    }

    public AccessLog(String time, String addr, String path, String UA) {
        this.time = time;
        this.addr = addr;
        this.path = path;
        this.UA = UA;
    }

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(name = "time")
    private String time;

    @Column(name = "addr")
    private String addr;

    @Column(name = "path")
    private String path;

    @Column(name = "ua", nullable = false)
    private String UA;

}
