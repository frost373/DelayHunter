package top.thinkin.delayhunter;

import cn.hutool.core.lang.Assert;
import cn.hutool.setting.dialect.Props;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import top.thinkin.lightd.db.DB;
import top.thinkin.lightd.exception.KitDBException;
import top.thinkin.lightd.raft.GroupConfig;
import top.thinkin.lightd.raft.KitRaft;
import top.thinkin.lightd.raft.NodeConfig;

import java.io.File;
import java.io.IOException;

@SpringBootApplication
public class DelayhunterApplication {

    static GroupConfig groupConfig;
    static NodeConfig nodeConfig;
    static String dbDir;
    static String type;

    protected   static String authKey;

    public static void main(String[] args) {
        String configFile = args[0];
        Props props = new Props(new File(configFile));
        dbDir =  props.getStr("db_dir");
        type = props.getStr("server_type");

        String dataPath =   props.getStr("server_data");
        String serverIdStr =   props.getStr("server_self_config");
        String initConfStr =   props.getStr("server_group_config");
        authKey = props.getStr("auth_key");
        Assert.notEmpty(dbDir,"db_dir cannot be empty");
        if(!"standalone".endsWith(type)){
            Assert.notEmpty(dataPath,"server_data cannot be empty");
            Assert.notEmpty(serverIdStr,"server_self_config cannot be empty");
            Assert.notEmpty(serverIdStr,"server_group_config cannot be empty");
        }

        groupConfig = new GroupConfig();
        groupConfig.setGroup("Delayhunter");
        groupConfig.setInitNodes(initConfStr);
        groupConfig.setElectionTimeoutMs(5000);
        groupConfig.setSnapshotIntervalSecs(10*60);
        nodeConfig = new NodeConfig();
        nodeConfig.setNode(serverIdStr);
        nodeConfig.setRaftDir(dataPath);

        SpringApplication.run(DelayhunterApplication.class, args);
    }

    @Bean
    public DB db() throws KitDBException {
        return DB.buildTransactionDB(dbDir,false);
    }

    public static boolean isStandalone(){
        return "standalone".endsWith(type);
    }

    @Bean
    public KitRaft kitRaft(DB db) throws IOException {
        if(isStandalone()){
            return new KitRaft();
        }else {
            return new KitRaft(groupConfig, nodeConfig, db);
        }
    }
}
