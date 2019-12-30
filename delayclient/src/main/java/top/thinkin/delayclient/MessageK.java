package top.thinkin.delayclient;

import java.io.Serializable;

public class MessageK implements Serializable {
    private static final long serialVersionUID = 1L;

    public MessageK(){

    }

    public int errno;

    public String errmsg;

    
    public MessageK(int errno, String errmsg) {
        this.errno = errno;
        this.errmsg = errmsg;
    }


    public int getErrno() {
        return errno;
    }

    public void setErrno(int errno) {
        this.errno = errno;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }
    
}
