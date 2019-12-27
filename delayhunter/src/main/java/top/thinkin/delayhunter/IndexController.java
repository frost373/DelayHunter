package top.thinkin.delayhunter;

import cn.hutool.core.collection.CollectionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import top.thinkin.delayhunter.error.NodesException;
import top.thinkin.lightd.db.DB;
import top.thinkin.lightd.db.RMap;
import top.thinkin.lightd.db.ZSet;
import top.thinkin.lightd.exception.KitDBException;
import top.thinkin.lightd.raft.KitRaft;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class IndexController {
    private static ThreadPoolExecutor pool = new ThreadPoolExecutor(10, 10, 1000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(10000), Executors.defaultThreadFactory(),new ThreadPoolExecutor.AbortPolicy());








    @Autowired
    private DB db;

    @Autowired
    private KitRaft kitRaft;

    private void leaderCheck(){
        if(!DelayhunterApplication.isStandalone() && !kitRaft.isLeader()){
            throw new NodesException("not leader,need redirect",kitRaft.getLeaderIP());
        }
    }



    @PostMapping("/add")
    public Mono<Message> add(@RequestBody Timer timer){
        leaderCheck();
        Flux<Message> flux =  Flux.create(fluxSink-> pool.submit(() ->{
            ZSet zSet =  db.getzSet();
            RMap rmap =  db.getMap();
            try {
                db.startTran();
                zSet.add(timer.getGroup(),timer.getId().getBytes(),System.currentTimeMillis()+timer.getMillisecond());
                rmap.put(timer.getGroup(),timer.getId(),timer.getData().getBytes());
                db.commitTX();
            } catch (Exception e) {
                log.error("error",e);
                try {
                    db.rollbackTX();
                } catch (KitDBException e1) {
                    log.error("rollbackTX error",e1);
                }
                fluxSink.error(e);
                return;
            }

            fluxSink.next(Kits.success("OK"));
            fluxSink.complete();
        }));

        return flux.single();
    }

    @PostMapping("/adds")
    public Mono<Message> adds(@RequestBody List<Timer> timers){
        leaderCheck();
        Flux<Message> flux =  Flux.create(fluxSink-> pool.submit(() ->{
            ZSet zSet =  db.getzSet();
            RMap rmap =  db.getMap();
            try {
                db.startTran();
                Map<String, List<Timer>> groups =  timers.stream(). collect(Collectors.groupingBy(Timer::getGroup));
                for(String key : groups.keySet()){
                    List<ZSet.Entry> entryList = groups.get(key).stream().map(timer ->
                                    new  ZSet.Entry(timer.millisecond,timer.id.getBytes()))
                            .collect(Collectors.toList());

                    zSet.add(key,entryList);
                    Map<String, byte[]> map  =  groups.get(key).stream().collect(
                            Collectors.toMap(Timer::getId,
                                    timer -> timer.getData().getBytes(),
                                    (key1, key2) -> key2));
                    rmap.putMayTTL(key,-1,map);
                }
                db.commitTX();
            } catch (Exception e) {
                log.error("error",e);
                try {
                    db.rollbackTX();
                } catch (KitDBException e1) {
                    log.error("rollbackTX error",e1);
                }

                fluxSink.error(e);
                return;
            }

            fluxSink.next(Kits.success("OK"));
            fluxSink.complete();
        }));

        return flux.single();
    }


    @GetMapping("/pop")
    public Mono<Message> pop(String group, Integer limit) {
        leaderCheck();

        Flux<Message> flux =   Flux.create(fluxSink-> pool.submit(() ->{
            ZSet zSet =  db.getzSet();
            RMap rmap =  db.getMap();
            try {
                db.startTran();

                List<ZSet.Entry> entries =  zSet.rangeDel(group,31507200000L,System.currentTimeMillis(),limit);
                List<Timer> timers = new ArrayList<>(entries.size());
                if (resEmpty(fluxSink, entries, timers)) return;
                List<String> ids = new ArrayList<>();
                for (ZSet.Entry entry : entries) {
                    ids.add(new String(entry.getValue()));
                }
                String[] strings = new String[ids.size()];ids.toArray(strings);
                Map<String, byte[]> map =   rmap.get(group,strings);

                setList(group,entries, timers, map);
                rmap.remove(group,strings);
                db.commitTX();
                fluxSink.next(Kits.success(timers));
            } catch (Exception e) {
                log.error("error",e);
                try {
                    db.rollbackTX();
                } catch (KitDBException e1) {
                    log.error("rollbackTX error",e1);
                }
                fluxSink.error(e);
                return;
            }
            fluxSink.complete();
        }));

        return flux.single();
    }


    @GetMapping("/query")
    public Mono<Message> query(String group, Integer limit) {
        leaderCheck();

        Flux<Message> flux =   Flux.create(fluxSink-> pool.submit(() ->{
            ZSet zSet =  db.getzSet();
            RMap rmap =  db.getMap();
            try {
                db.startTran();

                List<ZSet.Entry> entries =  zSet.range(group,31507200000L,System.currentTimeMillis(),limit);
                List<Timer> timers = new ArrayList<>(entries.size());
                if (resEmpty(fluxSink, entries, timers)) return;
                List<String> ids = new ArrayList<>();
                for (ZSet.Entry entry : entries) {
                    ids.add(new String(entry.getValue()));
                }
                String[] strings = new String[ids.size()];ids.toArray(strings);
                Map<String, byte[]> map = rmap.get(group,strings);

                setList(group,entries, timers, map);
                db.commitTX();
                fluxSink.next(Kits.success(timers));
            } catch (Exception e) {
                log.error("error",e);
                try {
                    db.rollbackTX();
                } catch (KitDBException e1) {
                    log.error("rollbackTX error",e1);
                }
                fluxSink.error(e);
                return;
            }
            fluxSink.complete();
        }));

        return flux.single();
    }

    private boolean resEmpty(FluxSink<Message> fluxSink, List<ZSet.Entry> entries, List<Timer> timers) throws KitDBException {
        if(CollectionUtil.isEmpty(entries)){
            db.commitTX();
            fluxSink.next(Kits.success(timers));
            fluxSink.complete();
            return true;
        }
        return false;
    }

    private void setList(String group,List<ZSet.Entry> entries, List<Timer> timers, Map<String, byte[]> map) {
        for (ZSet.Entry entry : entries) {
            Timer timer = new Timer();
            String id =  new String(entry.getValue());
            timer.setId(new String(entry.getValue()));
            timer.setGroup(group);
            timer.setMillisecond(entry.getScore());
            byte[] bytes =  map.get(id);
            if(bytes!=null){
                timer.setData(new String(bytes));
            }
            timers.add(timer);
        }
    }


    @GetMapping("/queryTime")
    public Long score( String group,String id) throws KitDBException {
        ZSet zSet =  db.getzSet();
        return zSet.score(group,id.getBytes());
    }

    @GetMapping("/leader")
    public String leader( String group,String id) throws KitDBException {
        return kitRaft.getLeaderIP();
    }

    @GetMapping("/getNodes")
    public List<String> getNodes( String group,String id) throws KitDBException {
        leaderCheck();
        return kitRaft.getNodes();
    }

    @GetMapping("/addNode")
    public void addNode( String node) throws KitDBException {
        leaderCheck();
        kitRaft.addNode(node);
    }

    @GetMapping("/removeNode")
    public void removeNode( String node) throws KitDBException {
        leaderCheck();
        kitRaft.removeNode(node);
    }

    public static  class Timer{
        private  String group;
        private  String id;
        private  String data;
        private  Long millisecond;

        private FluxSink<Message> timerFluxSink;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }

        public String getGroup() {
            return group;
        }

        public void setGroup(String group) {
            this.group = group;
        }

        public Long getMillisecond() {
            return millisecond;
        }

        public void setMillisecond(Long millisecond) {
            this.millisecond = millisecond;
        }

        public FluxSink<Message> getTimerFluxSink() {
            return timerFluxSink;
        }

        public void setTimerFluxSink(FluxSink<Message> timerFluxSink) {
            this.timerFluxSink = timerFluxSink;
        }
    }

}
