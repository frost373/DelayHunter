package top.thinkin.delayhunter;

import java.io.Serializable;

public class Message<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final int ERRNO_UNDEFINED = -1;

    public int errno;

    public String errmsg;

    public T data;

    public Message(int errno, String errmsg, T data) {
        this.errno = errno;
        this.errmsg = errmsg;
        this.data = data;
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

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
