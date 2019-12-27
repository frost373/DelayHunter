package top.thinkin.delayhunter.error;


import top.thinkin.delayhunter.Message;

public class MessageException extends RuntimeException {

    private final int errno;

    public MessageException(String errmsg) {
        this(-1, errmsg);
    }

    public MessageException(int errno, String errmsg) {
        super(errmsg);
        this.errno = errno;
    }


    public int getErrno() {
        return errno;
    }

    public String getErrmsg() {
        return super.getMessage();
    }


    @SuppressWarnings("rawtypes")
    public Message toMessage() {
        return new Message(errno, super.getMessage(), null);
    }
}
