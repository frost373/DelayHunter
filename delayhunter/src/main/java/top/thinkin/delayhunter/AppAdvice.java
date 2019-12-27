package top.thinkin.delayhunter;

import cn.hutool.core.util.StrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ResponseBody;
import top.thinkin.delayhunter.error.NodesException;

@ControllerAdvice
public class AppAdvice {

    private final static Logger logger = LoggerFactory.getLogger(AppAdvice.class);

    @ResponseBody
    @ExceptionHandler(NodesException.class)
    public Message nodesException( NodesException ex) {
        return Kits.error(699,ex.getMessage(),ex.getRedirect());
    }

    @ResponseBody
    @ExceptionHandler(Exception.class)
    public Message handleException( Exception ex) {
        logger.error("exhander:", ex);
        return Kits.error(ex.getMessage());
    }

    @ModelAttribute()
    public void  auth(ServerHttpRequest request){
        if(StrUtil.isEmpty(DelayhunterApplication.authKey)){
            return;
        }
       Assert.isTrue(DelayhunterApplication.authKey.equals(request.getQueryParams().getFirst("key")),"auth key ERROR");
    }

}
