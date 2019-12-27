package top.thinkin.delayhunter;

public class Kits {
    public static <T> Message<T> success(T data) {
        Message<T> sm = new Message<T>(0, null, data);
        return sm;
    }




    public static <T> Message<T> error(String msg) {
        Message<T> sm = new Message<T>(-1, msg, null);
        return sm;
    }


    public static <T> Message<T> error(int errno,String msg,T data) {
        Message<T> sm = new Message<>(errno, msg, data);
        return sm;
    }
}
